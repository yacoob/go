#!/usr/bin/env python
# coding: utf-8
# pylint: disable-msg=W0142

from bottle import Bottle, request, template, redirect, response
from sqldict_plugin import sqldictPlugin
import threading
import time
from urlparse import urlunsplit

app = Bottle()
app.timestamp_lock = threading.Lock()

def provisionDbs(db, db_old):
    app.install(sqldictPlugin(keyword='db', filename=db))
    app.install(sqldictPlugin(keyword='db_old', filename=db_old))

def describe_urls(db, json=False):
    entries = db.items()
    entries.sort(reverse=True)
    descriptive = [(time.ctime(float(e[0])), e[0], e[1]) for e in entries]
    if json:
        return [{'date': x[0], 'id': x[1], 'url': x[2]} for x in descriptive]
    else:
        return descriptive

@app.route('/push')
def push_url(db):
    url = request.params.get('url', None)
    if url:
        app.timestamp_lock.acquire()
        db[str(time.time())] = url
        app.timestamp_lock.release()
        return template('hop_msg', title='- trampoline push succeeded', url=url)
    else:
        redirect('/list')

@app.route('/pop', name='pop')
def pop_url(db, db_old):
    urls_keys = db.keys()
    urls_keys.sort()
    latest_id = urls_keys[-1] if len(urls_keys) else 0
    url_id = request.params.get('id', latest_id)
    if (url_id not in urls_keys):
        redirect('/list')

    url = db[url_id]
    del db[url_id]

    # FIXME: purge old urls from db_old
    db_old[url_id] = url

    redirect(url)

@app.route('/list', name='list')
def list_url(db, db_old):
    if request.params.get('json', None):
        return {'stack': describe_urls(db, True),
                'viewed': describe_urls(db_old, True)}
    else:
        kwargs = {
            'pop_url': app.get_url('pop'),
            'stack':  describe_urls(db),
            'viewed': describe_urls(db_old),
            'title':  '- trampoline URLs list',
            }
        return template('hop_list', **kwargs)

@app.route('/rss')
def rss(db):
    base_url = urlunsplit(request.urlparts[0:2] + ('', '', ''))
    stack = describe_urls(db, True)
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

