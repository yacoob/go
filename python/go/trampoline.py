#!/usr/bin/env python
# coding: utf-8
# pylint: disable-msg=W0142, C0103

""" Trampoline module."""

from BeautifulSoup import BeautifulSoup
from bottle import abort, Bottle, request, template, redirect, response
from bottle.ext import sqlite as bottle_sqlite # pylint: disable-msg=F0401
from datetime import datetime
from email.utils import formatdate
from multiprocessing import Lock, Process, Event
from os import getpid
from time import time
from urllib import urlopen, urlencode
from urlparse import urlunsplit
from uuid import uuid4
import sqlite3


app = Bottle()
app.timestamp_lock = Lock()
app.fetcher_flag = Event()


def fetcher(filename, base_url, event):
    """ Fetch titles for all enqueued URls, update db."""
    print 'Fetcher process started as pid <%s>' % getpid()
    tables = ('stack', 'viewed')
    c = sqlite3.connect(filename)
    c.row_factory = sqlite3.Row
    while event.is_set():
        event.clear()
        for table in tables:
            rows = c.execute(
                 'SELECT * FROM ' +
                 table +
                 ' WHERE token IS NOT NULL').fetchall()
            for row in rows:
                url = row['url']
                try:
                    f = urlopen(url)
                    blob = f.read(2*1024*1024)
                    f.close()
                    soup = BeautifulSoup(blob,
                           convertEntities=BeautifulSoup.HTML_ENTITIES)
                    title = soup.title.string
                    describe_url = ''.join([base_url, '?', urlencode({
                        'list_id': table,
                        'token': row['token'],
                        'description': title,
                    })])
                    urlopen(describe_url)
                except: # pylint: disable-msg=W0702
                    continue
    c.close()
    exit(0)


def provisionDbs(filename):
    """ Set up database, creating tables if needed."""
    c = sqlite3.connect(filename)
    c.executescript('''
CREATE TABLE IF NOT EXISTS
stack(timestamp TEXT PRIMARY KEY, url TEXT, description TEXT, token TEXT);
CREATE TABLE IF NOT EXISTS
viewed(timestamp TEXT PRIMARY KEY, url TEXT, description TEXT, token TEXT);
''')
    c.close()
    app.db_filename = filename
    app.install(bottle_sqlite.Plugin(dbfile=filename, keyword='db'))


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
        url = url.decode('utf-8')
        app.timestamp_lock.acquire()
        timestamp = '%.3f' % time()
        app.timestamp_lock.release()
        token = uuid4().hex
        db.execute('INSERT INTO stack(timestamp, url, token) VALUES(?, ?, ?)',
                (timestamp, url, token))
        app.fetcher_flag.set()
        if not (hasattr(app, 'FetcherProcess') and
                app.FetcherProcess.is_alive()):
            db.commit()
            base_url = ''.join([
                urlunsplit(request.urlparts[0:2] + ('', '', '')),
                app.get_url('restDescribe')])
            app.FetcherProcess = Process(target=fetcher,
                args=(app.db_filename, base_url, app.fetcher_flag))
            app.FetcherProcess.start()
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


@app.route('/r/describe', name='restDescribe')
def restSetDescription(db):
    """ REST interface: set description for single url. Fetcher uses it to avoid
    writing to single SQLite db from >1 process."""
    list_id = request.params.get('list_id')
    auth_token = request.params.get('token')
    description = request.params.get('description')
    if not (auth_token and description):
        abort(404)
    db.execute('UPDATE ' + list_id + ' SET description = ?, token = NULL ' +
        'WHERE token = ?', (description.decode('utf-8'), auth_token))
    return 'OK'


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
