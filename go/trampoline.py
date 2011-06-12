#!/usr/bin/env python
# coding: utf-8
# pylint: disable-msg=W0142

from bottle import abort, Bottle, request, template, redirect, response
from datetime import datetime
from dict_plugin import dictPlugin
from email.utils import formatdate
from time import time
from urlparse import urlunsplit
import threading


app = Bottle()
app.timestamp_lock = threading.Lock()


def provisionDbs(db, db_old):
    app.install(dictPlugin(keyword='db', filename=db))
    app.install(dictPlugin(keyword='db_old', filename=db_old))

def describeUrl(db, url_id, base_url, rfc822=False, pop_url=True):
    if not db.has_key(url_id):
        return None
    date = datetime.fromtimestamp(float(url_id))
    description = {
            'date': formatdate(float(url_id)) if rfc822 else date.ctime(),
            'day': date.strftime("%A, %B %d, %Y"),
            'time': date.strftime("%H:%M"),
            'url': db[url_id],
            'id': url_id,
    }
    if pop_url:
        description.update({
            'pop_url': base_url + app.get_url('pop') + '?id=' + url_id
        })
    return description

def describeUrls(db, base_url, rfc822=False, pop_url=True):
    urls = sorted(db.keys(), reverse=True)
    return [
        describeUrl(db, u, base_url, rfc822, pop_url)
        for u in urls
    ]

@app.route('/push')
def pushUrl(db):
    url = request.params.get('url', None)
    if url:
        app.timestamp_lock.acquire()
        db[str(time())] = url
        app.timestamp_lock.release()
        return template('hop_msg', title='- trampoline push succeeded', url=url)
    else:
        redirect(app.get_url('list'))

@app.route('/pop', name='pop')
def popUrl(db, db_old):
    urls_keys = sorted(db.keys())
    latest_id = urls_keys[-1] if len(urls_keys) else 0
    url_id = request.params.get('id', latest_id)
    if (url_id not in urls_keys):
        redirect(app.get_url('list'))

    url = db[url_id]
    del db[url_id]

    # FIXME: purge old urls from db_old
    db_old[url_id] = url

    redirect(url)

@app.route('/')
def goToList():
    redirect(app.get_url('list'))

@app.route('/list', name='list')
def listUrls(db, db_old):
    base_url = urlunsplit(request.urlparts[0:2] + ('', '', ''))
    kwargs = {
        'pop_url': app.get_url('pop'),
        'stack':  describeUrls(db, base_url),
        'viewed': describeUrls(db_old, base_url, pop_url=False),
        'title':  '- trampoline URLs list',
    }
    return template('hop_list', **kwargs)

@app.route('/rss')
def showRss(db):
    base_url = urlunsplit(request.urlparts[0:2] + ('', '', ''))
    stack = describeUrls(db, base_url, rfc822=True)
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

@app.route('/r/:list_id#(?:stack|viewed)#')
def restShowList(list_id, db, db_old):
    if list_id == 'stack':
        return {'stack': sorted(db.keys(), reverse=True) }
    elif list_id == 'viewed':
        return {'viewed': sorted(db_old.keys(), reverse=True) }
    else:
        abort(404, 'No such list.')

@app.route('/r/:list_id#(?:stack|viewed)#/:url_id#[0-9.]+#')
def restShowListEntry(list_id, url_id, db, db_old):
    base_url = urlunsplit(request.urlparts[0:2] + ('', '', ''))
    if list_id == 'stack':
        description = describeUrl(db, url_id, base_url)
    elif list_id == 'viewed':
        description = describeUrl(db_old, url_id, base_url, pop_url=False)
    else:
        abort(404, 'No such list or id.')
    if not description:
        abort(404, 'No such list or id.')
    else:
        return description

if __name__ == "__main__":
    import bottle
    bottle.debug(True)
    bottle.default_app().mount(app, '/hop')
    provisionDbs('/tmp/hop.db', '/tmp/hop_old.db')
    bottle.run()
