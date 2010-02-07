#!/usr/bin/env python
# coding: utf-8

import getopt, shelve, sys, urllib;
import bottle;


@bottle.route('/')
def index():
    bottle.redirect('/and/add');


@bottle.route('/(?P<shortcut>[^&?/*]+)\*')
def edit_that(shortcut):
    # /foo* = edit shortcut 'foo'
    url = '/and/edit?' + urllib.urlencode({'short': shortcut});
    bottle.redirect(url);


@bottle.route('/(?P<shortcut>[^&?/*]+)')
def go_there(shortcut):
    db = app['shortcuts_db'];

    if (db.has_key(shortcut)):
        # if redirect already exist, just go there
        bottle.redirect(db[shortcut]);
    else:
        # if it doesn't, redirect to prefilled edit form
        url = '/and/add?' + urllib.urlencode({'short': shortcut});
        bottle.redirect(url);


@bottle.route('/and/:cmd')
def command(cmd):
    args = {};
    # FIXME: sanitize the params here
    args['short'] = bottle.request.GET.get('short', '');
    args['long'] = bottle.request.GET.get('long', '');
    db = app['shortcuts_db'];

    if (cmd == 'add'):
        # add new redirect
        args['message'] = 'Add a new redirection:';
        if args['short'] and args['long']:
            # if both short and long name are provided, add it straight away
            db[args['short']] = args['long'];
            db.sync();
            bottle.redirect('/and/list');
        else:
            # otherwise, present user with edit form
            return bottle.template('edit', **args);

    elif (cmd == 'edit'):
        if args['short']:
            # edit a redirect
            if db.has_key(args['short']):
                args['message'] = 'Edit:';
                args['long'] = db[args['short']];
            else:
                args['message'] = 'Add a new mapping:';
            return bottle.template('edit', **args);
        else:
            # redirect to / if no name was supplied
            bottle.redirect('/');

    elif (cmd == 'del'):
        # delete a redirect
        if args['short'] and db.has_key(args['short']):
            # if it exist, remove it and redirect to edit page, as a last
            # chance to
            args['message'] = 'Old shortcut removed, but you can always add it back here:';
            args['long'] = db[args['short']];
            del db[args['short']];
            db.sync();
            return bottle.template('edit', **args);
        else:
            url = '/and/add?' + urllib.urlencode({'short': args['short']});
            bottle.redirect(url);

    elif (cmd == 'list'):
        # show list of all redirects
        shortcuts = db.items();
        shortcuts.sort();
        args = {
          'list': shortcuts,
        };
        return bottle.template('list', **args);

    else:
        # default endpoint
        bottle.redirect('/and/add');


@bottle.route('/and/css')
def css():
    # FIXME: use proper path here
    bottle.send_file('main.css', './views/')


def init(app):
    # set default config values
    app.update({
        'debug':    False,
        'host':     'localhost',
        'port':     8080,
        'db_dir':   '/tmp/',
    });

    # parse command line
    try:
        opts, args = getopt.getopt(sys.argv[1:], 'Dd:h:p:');
    except getopt.GetoptError, err:
        print str(err);
        sys.exit(2);

    # set options
    for o, a in opts:
        if o == '-D':
            app['debug'] = True;
        elif o == '-d':
            app['db_dir'] = a;
        elif o == '-h':
            app['host'] = a;
        elif o == '-p':
            app['port'] = a;
        else:
            assert False, 'unhandled command line option';

    # open dbs
    app['shortcuts_db'] = shelve.open(app['db_dir'] + 'shortcuts');
    app['trampolina_db'] = shelve.open(app['db_dir'] + 'trampolina');


def run(app):
    debug, host, port = app['debug'], app['host'], app['port'];

    # run bottle
    bottle.debug(debug);
    bottle.run(host=host, port=port, reloader=debug);


if (__name__ == '__main__'):
    app = {};
    init(app);
    run(app);