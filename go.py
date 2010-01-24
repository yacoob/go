#!/usr/bin/env python
# coding: utf-8

import bottle;
import shelve;
import urllib;


@bottle.route('/')
def index():
    bottle.redirect('/and/add');


@bottle.route('/:shortcut')
def go_there(shortcut):
    if (db.has_key(shortcut)):
        bottle.redirect(db[shortcut]);
    else:
        url = '/and/add?' + urllib.urlencode({'short': shortcut});
        bottle.redirect(url);


@bottle.route('/and/:cmd')
def command(cmd):
    args = {};
    args['short'] = bottle.request.GET.get('short', '');
    args['long'] = bottle.request.GET.get('long', '');
    if (cmd == 'add'):
        args['message'] = 'Add a new mapping:';
        if args['short'] and args['long']:
            db[args['short']] = args['long'];
            db.sync();
            bottle.redirect('/and/list');
        else:
            return bottle.template('edit', **args);
    elif (cmd == 'edit'):
        if args['short']:
            if db.has_key(args['short']):
                args['message'] = 'Edit:';
                args['long'] = db[args['short']];
            else:
                args['message'] = 'Add a new mapping:';
            return bottle.template('edit', **args);
        else:
            bottle.redirect('/');
    elif (cmd == 'del'):
        if args['short'] and db.has_key(args['short']):
            args['message'] = 'Old shortcut removed, but you can always add it back here:';
            args['long'] = db[args['short']];
            del db[args['short']];
            db.sync();
            return bottle.template('edit', **args);
        else:
            url = '/and/add?' + urllib.urlencode({'short': args['short']});
            bottle.redirect(url);
    elif (cmd == 'list'):
        shortcuts = db.items();
        shortcuts.sort();
        args = {
          'list': shortcuts,
        };
        return bottle.template('list', **args);
    else:
        bottle.redirect('/and/add');


@bottle.route('/and/css')
def css():
    bottle.send_file('main.css', './views/')


if (__name__ == '__main__'):
    bottle.debug(True);
    db = shelve.open('/tmp/xyz');
    bottle.run(host='localhost', port=8080, reloader=True);