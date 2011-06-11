#!/usr/bin/env python
# coding: utf-8
# pylint: disable-msg=W0142

from bottle import Bottle, request, template, redirect, response
from dict_plugin import dictPlugin
from urlparse import urlunsplit
import threading
import time


app = Bottle()
app.timestamp_lock = threading.Lock()


def provisionDbs(db, db_old):
    app.install(dictPlugin(keyword='db', filename=db))
    app.install(dictPlugin(keyword='db_old', filename=db_old))

def describeUrls(db, json=False):
    entries = db.items()
    entries.sort(reverse=True)
    descriptive = [(time.ctime(float(e[0])), e[0], e[1]) for e in entries]
    if json:
        return [{'date': x[0], 'id': x[1], 'url': x[2]} for x in descriptive]
    else:
        return descriptive

@app.route('/push')
def pushUrl(db):
    url = request.params.get('url', None)
    if url:
        app.timestamp_lock.acquire()
        db[str(time.time())] = url
        app.timestamp_lock.release()
        return template('hop_msg', title='- trampoline push succeeded', url=url)
    else:
        redirect(app.get_url('list'))

@app.route('/pop', name='pop')
def popUrl(db, db_old):
    urls_keys = db.keys()
    urls_keys.sort()
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
@app.route('/list', name='list')
def listUrl(db, db_old):
    if request.params.get('json', None):
        return {'stack': describeUrls(db, True),
                'viewed': describeUrls(db_old, True)}
    else:
        kwargs = {
            'pop_url': app.get_url('pop'),
            'stack':  describeUrls(db),
            'viewed': describeUrls(db_old),
            'title':  '- trampoline URLs list',
            }
        return template('hop_list', **kwargs)

@app.route('/rss')
def showRss(db):
    base_url = urlunsplit(request.urlparts[0:2] + ('', '', ''))
    stack = describeUrls(db, True)
    kwargs = {
       'stack': stack,
       'title': '- new trampoline URLs',
       'description': 'New URLs on trampoline',
       'timestamp': time.ctime(),
       'list_url': base_url + app.get_url('list'),
       'pop_url': base_url + app.get_url('pop') + '?id=',
    }
    response.content_type = 'text/xml'
    return template('hop_rss', **kwargs)


if __name__ == "__main__":
    import bottle
    bottle.debug(True)
    bottle.default_app().mount(app, '/hop')
    provisionDbs(None, None)
    bottle.run()
