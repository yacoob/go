#!/usr/bin/env python
# coding: utf-8

from bottle import Bottle, request, template, redirect
from sqldict_plugin import sqldictPlugin
import urllib


app = Bottle()


def provisionDbs(db, db_old):
    app.install(sqldictPlugin(keyword='db', filename=db))

@app.route('/')
@app.route('/list', name='list')
def listShortcuts(db):
        shortcuts = db.items()
        shortcuts.sort()
        kwargs = {
            'list':  shortcuts,
            'title': '- shortcuts list',
        }
        return template('go_list', **kwargs)

@app.route('/:shortcut#[^&?/*]+#')
def handleShortcut(shortcut, db):
    if (db.has_key(shortcut)):
        # if redirect already exist, just go there
        url = db[shortcut]
    else:
        # if it doesn't, redirect to prefilled edit form
        url = app.get_url('add') + '?' + urllib.urlencode({'short': shortcut})
    redirect(url)

@app.route('/add', name='add')
def addShortcut(db):
    (long, short) = (
        request.params.get('long', ''),
        request.params.get('short', '')
    )
    if long and short:
        db[short] = long
        redirect(app.get_url('list'))
    else:
        kwargs = {
            'message': 'Add a new shortcut:',
            'title': '- add a new shortcut',
        }
        return template('go_edit', long=long, short=short, **kwargs)

@app.route('/edit')
def editShortcut(db):
    short = request.params.get('short', '')
    if short:
        # edit a redirect
        if db.has_key(short):
            kwargs = {
                'message': 'Edit:',
                'title': '- edit a shortcut',
                'short': short,
                'long': db[short],
            }
        else:
            kwargs = {
                'message': 'Add a new shortcut:',
                'title': '- add a new shortcut',
            }
        return template ('go_edit', **kwargs)
    else:
        # redirect to / if no name was supplied
        redirect(app.get_url('add'))

@app.route('/del')
def deleteShortcut(db):
    short = request.params.get('short', '')
    if short and db.has_key(short):
        # if it exist, remove it and redirect to edit page, as a last
        # chance to save this shortcut
        kwargs = {
            'message': 'Old shortcut removed, but you can always add it back here:',
            'title':  '- last chance to save a shortcut!',
            'short': short,
            'long': db[short],
        }
        del db[short]
        return template('go_edit', **kwargs)
    else:
        url = app.get_url('add') + '?' + urllib.urlencode({'short': short})
        redirect(url)


if __name__ == "__main__":
    import bottle
    bottle.debug(True)
    bottle.default_app().mount(app, '/and')
    provisionDbs(None, None)
    bottle.run()
