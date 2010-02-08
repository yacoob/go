#!/usr/bin/env python
# coding: utf-8

import getopt, shelve, sys, time, urllib;
import bottle;


@bottle.route('/')
def index():
    bottle.redirect('/and/add');


@bottle.route('/(?P<shortcut>[^&?/*]+)\*')
def go_edit_that(shortcut):
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


@bottle.route('/and/(?P<cmd>.*)')
def go_command(cmd):
    args = {};
    # FIXME: sanitize the params here
    args['short'] = bottle.request.GET.get('short', '');
    args['long'] = bottle.request.GET.get('long', '');
    db = app['shortcuts_db'];

    if (cmd == 'add'):
        # add new redirect
        args['message'] = 'Add a new shortcut:';
        args['title'] = '- add a new shortcut';
        if args['short'] and args['long']:
            # if both short and long name are provided, add it straight away
            db[args['short']] = args['long'];
            db.sync();
            bottle.redirect('/and/list');
        else:
            # otherwise, present user with edit form
            return bottle.template('go_edit', **args);

    elif (cmd == 'edit'):
        if args['short']:
            # edit a redirect
            if db.has_key(args['short']):
                args['message'] = 'Edit:';
                args['long'] = db[args['short']];
                args['title'] = '- edit a shortcut';
            else:
                args['message'] = 'Add a new shortcut:';
                args['title'] = '- add a new shortcut';
            return bottle.template('go_edit', **args);
        else:
            # redirect to / if no name was supplied
            bottle.redirect('/and/list');

    elif (cmd == 'del'):
        # delete a redirect
        if args['short'] and db.has_key(args['short']):
            # if it exist, remove it and redirect to edit page, as a last
            # chance to save this shortcut
            args['message'] = 'Old shortcut removed, but you can always add it back here:';
            args['long'] = db[args['short']];
            args['title'] = '- last chance to save a shortcut!';
            del db[args['short']];
            db.sync();
            return bottle.template('go_edit', **args);
        else:
            url = '/and/add?' + urllib.urlencode({'short': args['short']});
            bottle.redirect(url);

    elif (cmd == 'list'):
        # show list of all redirects
        shortcuts = db.items();
        shortcuts.sort();
        args = {
          'list':  shortcuts,
          'title': '- shortcuts list',
        };
        return bottle.template('go_list', **args);

    else:
        # default endpoint
        bottle.redirect('/and/add');


@bottle.route('/hop/(?P<cmd>.*)')
def hop_command(cmd):
    args = {};
    # FIXME: sanitize the params here
    args['url'] = bottle.request.GET.get('url', '');
    args['id'] = bottle.request.GET.get('id', '');
    db = app['trampolina_db'];
    db_old = app['trampolina_old_db'];

    if (cmd == 'push'):
        if (args['url']):
            # ZOMG race condition! 8)
            db[str(time.time())] = args['url'];
            db.sync();
            args['title'] = '- trampolina push succeeded';
            return bottle.template('hop_msg', **args);
        else:
            bottle.redirect('/hop/list');

    elif (cmd == 'pop'):
        urls_keys = db.keys();
        id = int(args['id'] if args['id'] else len(urls_keys)) - 1;
        if ((id<0) or (id>len(urls_keys)-1)):
            bottle.redirect('/hop/list');
        urls_keys.sort();
        t = urls_keys[id];

        url = db[t];
        del db[t];
        db.sync();

        # FIXME: purge old urls from db_old
        db_old[t] = url;
        db_old.sync();

        bottle.redirect(url);

    elif (cmd == 'list'):
        urls = db.items();
        urls.sort(reverse=True);
        old_urls = db_old.items();
        old_urls.sort(reverse=True);
        args = {
            'stack':  urls,
            'viewed': old_urls,
            'title':  '- trampolina URLs list',
        };
        return bottle.template('hop_list', **args);

    elif (cmd == 'rss'):
        pass

    else:
        # default endpoint
        bottle.redirect('/hop/list');


@bottle.route('/static/:filename')
def static_file(filename):
    # FIXME: use proper root path here
    bottle.send_file(filename, root='./static/');


def init(app):
    # set default config values
    app.update({
        'debug':    False,
        'host':     'localhost',
        'port':     8080,
        'db_dir':   '/tmp/',
    });

    # parse command line
    # FIXME: add option to disable trampoline
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
    app['shortcuts_db'] = shelve.open(
        app['db_dir'] + 'shortcuts', writeback=True
    );
    app['trampolina_db'] = shelve.open(
        app['db_dir'] + 'trampolina', writeback=True
    );
    app['trampolina_old_db'] = shelve.open(
        app['db_dir'] + 'trampolina-old', writeback=True
    );


def run(app):
    debug, host, port = app['debug'], app['host'], app['port'];

    # run bottle
    bottle.debug(debug);
    bottle.run(host=host, port=port, reloader=debug);


if (__name__ == '__main__'):
    app = {};
    init(app);
    run(app);