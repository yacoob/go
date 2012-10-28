#!/usr/bin/env python
# coding: utf-8
# pylint: disable-msg=W0142, C0103

from bottle import Bottle, request, template, redirect
from bottle.ext import sqlite as bottle_sqlite # pylint: disable-msg=F0401
import sqlite3
import urllib


app = Bottle()


def provisionDbs(filename):
    """ Set up database, creating tables if needed."""
    c = sqlite3.connect(filename)
    c.executescript('''
CREATE TABLE IF NOT EXISTS
redirector(shortcut TEXT PRIMARY KEY, url TEXT);
''')
    c.close()
    app.install(bottle_sqlite.Plugin(dbfile=filename, keyword='db'))


@app.route('/')
def goToList():
    """ Default handler."""
    redirect(app.get_url('list'))


@app.route('/list', name='list')
def listShortcuts(db):
    """ Display currently defined redirects."""
    shortcuts = db.execute(
        'SELECT * FROM redirector ORDER BY shortcut').fetchall()
    kwargs = {
        'list':  shortcuts,
        'title': 'shortcuts list',
    }
    return template('go_list', **kwargs)


@app.route('/<shortcut:re:[^&?/*]+>')
def handleShortcut(shortcut, db):
    """ Redirect to target URL if entry already exists, redirect to edit form if
    it doesn't."""
    entry = db.execute('SELECT url FROM redirector WHERE shortcut = ?',
            (shortcut,)).fetchone()
    if (entry):
        # if redirect already exist, just go there
        url = entry['url']
    else:
        # if it doesn't, redirect to prefilled edit form
        url = app.get_url('add') + '?' + urllib.urlencode({'short': shortcut})
    redirect(url)


@app.route('/add', name='add')
def addShortcut(db):
    """ Set a shortcut. It's used for both adding new redirects and editing old
    ones."""
    (url, shortcut) = (
        request.params.get('long', ''),
        request.params.get('short', '')
    )
    if url and shortcut:
        db.execute('DELETE FROM redirector WHERE shortcut = ?', (shortcut,))
        db.execute('INSERT INTO redirector(shortcut, url) VALUES(?,?)',
                (shortcut, url))
        db.commit()
        redirect(app.get_url('list'))
    else:
        kwargs = {
            'message': 'Add a new shortcut',
            'title': 'add a new shortcut',
        }
        return template('go_edit', long=url, short=shortcut, **kwargs)


@app.route('/<shortcut:re:[^&?/*]+>*')
def goEditThat(shortcut):
    """ Convenience handler for edits."""
    url = app.get_url('edit') + '?' + urllib.urlencode({'short': shortcut})
    redirect(url)


@app.route('/edit', name='edit')
def editShortcut(db):
    """ Displays edit form for a shortcut."""
    short = request.params.get('short', '')
    if short:
        # edit a redirect
        entry = db.execute('SELECT url FROM redirector WHERE shortcut = ?',
                (short,)).fetchone()
        if entry:
            kwargs = {
                'message': 'Edit shortcut',
                'title': 'shortcut edit',
                'short': short,
                'long': entry['url'],
            }
        else:
            kwargs = {
                'message': 'Add a new shortcut',
                'title': 'add a shortcut',
                'short': short,
                'long': '',
            }
        return template('go_edit', **kwargs)
    else:
        # redirect to / if no name was supplied
        redirect(app.get_url('add'))


@app.route('/del')
def deleteShortcut(db):
    """ Removes shortcut from db."""
    short = request.params.get('short', '')
    entry = db.execute('SELECT url FROM redirector WHERE shortcut = ?',
            (short,)).fetchone()
    if short and entry:
        # if it exist, remove it and redirect to edit page, as a last
        # chance to save this shortcut
        kwargs = {
            'message': 'Shortcut deleted - last chance to save it',
            'title':  'shortcut removed',
            'short': short,
            'long': entry['url'],
        }
        db.execute('DELETE FROM redirector WHERE shortcut = ?', (short,))
        db.commit()
        return template('go_edit', **kwargs)
    else:
        url = app.get_url('add') + '?' + urllib.urlencode({'short': short})
        redirect(url)


if __name__ == "__main__":
    import bottle
    bottle.debug(True)
    bottle.default_app().mount(app, '/and')
    provisionDbs('/tmp/trampoline.db')
    bottle.run(reloader=True)
