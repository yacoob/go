#!/usr/bin/env python
# coding: utf-8
# pylint: disable-msg=W0142, C0103

""" Trampoline module."""

from BeautifulSoup import BeautifulSoup
from bottle import abort, Bottle, request, template, redirect, response
from bottle.ext import sqlite as bottle_sqlite
from datetime import datetime
from email.utils import formatdate
from multiprocessing import Lock, Process, Queue
from os import getpid
from sqlite3 import connect as sqlite3_connect
from time import time
from urllib import urlopen
from urlparse import urlunsplit

app = Bottle()
app.timestamp_lock = Lock()
app.queue = Queue()


def fetcher(queue, filename):
    """ Run in loop, listen for new URLs, fetch them, work out the title, update
    db."""
    print 'Fetcher process started as pid <%s>' % getpid()
    while True:
        (timestamp, url) = queue.get()
        try:
            soup = BeautifulSoup(urlopen(url), convertEntities=BeautifulSoup.HTML_ENTITIES)
            title = soup.title.string
        except AttributeError:
            continue
        c = sqlite3_connect(filename)
        c.execute('UPDATE stack SET description = ? WHERE timestamp = ?',
                (title, timestamp))
        c.commit()
        c.close()


def provisionDbs(filename):
    """ Set up database, creating tables if needed."""
    c = sqlite3_connect(filename)
    c.executescript('''
CREATE TABLE IF NOT EXISTS
stack(timestamp TEXT PRIMARY KEY, url TEXT, description TEXT);
CREATE TABLE IF NOT EXISTS
viewed(timestamp TEXT PRIMARY KEY, url TEXT, description TEXT);
''')
    c.close()
    app.install(bottle_sqlite.Plugin(dbfile=filename, keyword='db'))
    if not hasattr(app, 'FetcherProcess'):
        app.FetcherProcess = Process(target=fetcher, args=(app.queue, filename))
        app.FetcherProcess.start()


def _describeUrl(row, pop_url_base=None, rss=False):
    """ Describe single URL. Returns a dict with detailed data.

    Arguments:
        row - row to describe. Could be a dict, as it's used as such.
        pop_url_base - if present, description will contain 'pop_url' keyword
            pointing out trampoline URL to pop this URL directly.
        rss - if set, format of 'date' will be suitable for RSS"""
    url_id = str(row['timestamp'])
    date = datetime.fromtimestamp(float(url_id))
    description = {
        'date': formatdate(float(url_id)) if rss else date.ctime(),
        'day': date.strftime('%A, %B %d, %Y'),
        'time': date.strftime('%H:%M'),
        'url': row['url'],
        'id': url_id,
        'description': row['description'] or '',
    }
    if pop_url_base:
        description.update({
            'pop_url': pop_url_base + app.get_url('pop') + '?id=' + url_id
        })
    return (url_id, description)


def describeUrls(cursor, **kwargs):
    """ Describe all URLs from cursor."""
    if (not cursor) or (not cursor.rowcount):
        return None
    urls = cursor.fetchall()
    described = [_describeUrl(u, **kwargs) for u in urls]
    return dict(described)


def describeUrlsFromTable(db, tablename, **kwargs):
    """ Describe all URLs from table."""
    cursor = db.execute('SELECT * FROM ' + tablename)
    return describeUrls(cursor, **kwargs)


@app.route('/push')
def pushUrl(db):
    """ Accept new URL, place it on top of stack."""
    url = request.params.get('url', None)
    if url:
        app.timestamp_lock.acquire()
        timestamp = '%.3f' % time()
        app.timestamp_lock.release()
        db.execute('INSERT INTO stack(timestamp, url) VALUES(?, ?)',
                (timestamp, url))
        app.queue.put((timestamp, url))
        return template('hop_msg', title='- trampoline push succeeded', url=url)
    else:
        redirect(app.get_url('list'))


@app.route('/pop', name='pop')
def popUrl(db):
    """ Pop specified or latest URL from the stack."""
    url_id = request.params.get('id')
    if url_id:
        r = db.execute('SELECT * FROM stack WHERE timestamp = ? LIMIT 1',
                (url_id,)).fetchone()
    else:
        r = db.execute(
            'SELECT * FROM stack ORDER BY timestamp DESC LIMIT 1').fetchone()
    if not r:
        redirect(app.get_url('list'))

    url = r['url']
    ts = r['timestamp']
    db.execute('INSERT INTO viewed SELECT * FROM stack WHERE timestamp = ?',
            (ts,))
    db.execute('DELETE FROM stack WHERE timestamp = ?', (ts,))
    db.commit()   # https://github.com/defnull/bottle/issues/270 :(
    redirect(url)


@app.route('/')
def goToList():
    """ Default handler is 'list'."""
    redirect(app.get_url('list'))


@app.route('/list', name='list')
def listUrls(db):
    """ Display current stack of URLs, plus all viewed ones."""
    base_url = urlunsplit(request.urlparts[0:2] + ('', '', ''))
    kwargs = {
        'pop_url': app.get_url('pop'),
        'stack':  describeUrlsFromTable(db, 'stack', pop_url_base=base_url),
        'viewed': describeUrlsFromTable(db, 'viewed'),
        'title':  '- trampoline URLs list',
    }
    return template('hop_list', **kwargs)


@app.route('/rss')
def showRss(db):
    """ Display RSS with current content of the stack."""
    base_url = urlunsplit(request.urlparts[0:2] + ('', '', ''))
    stack = describeUrlsFromTable(db, 'stack', pop_url_base=base_url, rss=True)
    kwargs = {
        'stack': stack,
        'title': '- new trampoline URLs',
        'description': 'New URLs on trampoline',
        'timestamp': formatdate(time()),
        'list_url': base_url + app.get_url('list'),
        'pop_url': base_url + app.get_url('pop') + '?id=',
    }
    response.content_type = 'text/xml'
    return template('hop_rss', **kwargs)


@app.route('/r/<list_id:re:(?:stack|viewed)>')
def restShowList(list_id, db):
    """ REST interface: display ids of URLs from given list."""
    ids = db.execute('SELECT timestamp FROM ' + list_id
        + ' ORDER BY timestamp ASC').fetchall()
    ids = [x[0] for x in ids]
    return {list_id: ids}


@app.route('/r/<list_id:re:(?:stack|viewed)>/<magic_star:re:\*>')
@app.route('/r/<list_id:re:(?:stack|viewed)>/<url_id:re:[0-9.]+>')
@app.route('/r/<list_id:re:(?:stack|viewed)>/><start:re:[0-9.]+>')
def restShowListEntries(db, list_id, magic_star=None, url_id=None, start=None):
    """ REST interface: display details for set of URLs.

    Arguments:
        list_id - list of URLs to present
        start - if present, all URLs older than this timestamp will be
            presented
        url_id - if present, only this URL will be presented
        magic_star - if present, all URLs from given list will be presented"""
    kwargs = {}
    base_url = urlunsplit(request.urlparts[0:2] + ('', '', ''))
    if list_id == 'stack':
        kwargs['pop_url_base'] = base_url

    if start:
        cursor = db.execute('SELECT * FROM ' + list_id + ' WHERE timestamp > ?',
                (start,))
    elif url_id:
        cursor = db.execute('SELECT * FROM ' + list_id + ' WHERE timestamp = ?',
                (url_id,))
    elif magic_star:
        cursor = db.execute('SELECT * FROM ' + list_id)
    else:
        assert True, 'I got confused with your request: %s' % request.url()

    description = describeUrls(cursor, **kwargs)
    if not description:
        abort(404, 'No such id(s).')
    else:
        return description


if __name__ == '__main__':
    import bottle
    bottle.debug(True)
    bottle.default_app().mount(app, '/hop')
    provisionDbs('/tmp/trampoline.db')
    bottle.run(reloader=False)
