#!/usr/bin/env python
# coding: utf-8

import optparse, sys, time, urllib;
import bottle;

import sqldict;


@bottle.route('/')
def index():
    bottle.redirect('/and/list');


@bottle.route('/(?P<shortcut>[^&?/*]+)\*')
def go_edit_that(shortcut):
    # /foo* = edit shortcut 'foo'
    url = '/and/edit?' + urllib.urlencode({'short': shortcut});
    bottle.redirect(url);


@bottle.route('/(?P<shortcut>[^&?/*]+)')
def go_there(shortcut):
    db = app['shortcuts.db'];

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
    db = app['shortcuts.db'];

    if (cmd == 'add'):
        # add new redirect
        args['message'] = 'Add a new shortcut:';
        args['title'] = '- add a new shortcut';
        if args['short'] and args['long']:
            # if both short and long name are provided, add it straight away
            db[args['short']] = args['long'];
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
        bottle.redirect('/and/list');


@bottle.route('/hop/(?P<cmd>.*)')
def hop_command(cmd):
    args = {};
    # FIXME: sanitize the params here
    args['url'] = bottle.request.GET.get('url', '');
    args['id'] = bottle.request.GET.get('id', '');
    db = app['trampolina.db'];
    db_old = app['trampolina_old.db'];

    if (cmd == 'push'):
        if (args['url']):
            # ZOMG race condition! 8)
            db[str(time.time())] = args['url'];
            args['title'] = '- trampolina push succeeded';
            return bottle.template('hop_msg', **args);
        else:
            bottle.redirect('/hop/list');

    elif (cmd == 'pop'):
        urls_keys = db.keys();
        urls_keys.sort();
        latest_id = urls_keys[-1] if len(urls_keys) else 0;
        id = args['id'] if args['id'] else latest_id;
        if (id not in urls_keys):
            bottle.redirect('/hop/list');

        url = db[id];
        del db[id];

        # FIXME: purge old urls from db_old
        db_old[id] = url;

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
        urls = db.items();
        urls.sort(reverse=True);
        describe = lambda item: {
            'url': item[1],
            'timestamp': item[0],
            'datetime': time.ctime(float(item[0])),
        };
        base_url = 'http://' + app['host'] + ':' + str(app['port']);
        stack = map(describe, urls);
        args = {
           'stack': stack,
           'title': '- new trampolina URLs',
           'description': 'New URLs in trampolina',
           'timestamp': time.ctime(),
           'list_url': base_url + '/hop/list',
           'pop_url': base_url + '/hop/pop?id=',
        };
        bottle.response.content_type = 'text/xml';
        return bottle.template('hop_rss', **args);

    else:
        # default endpoint
        bottle.redirect('/hop/list');


@bottle.route('/static/:filename')
def static_file(filename):
    # FIXME: use proper root path here
    bottle.send_file(filename, root='./static/');


def init(app):
    # parse command line
    # FIXME: add option to disable trampoline
    parser = optparse.OptionParser();
    option_list = [
        optparse.make_option(
            '-D', '--debug',
            action='store_true', dest='debug',
            help='enable debug mode [off]',
        ),
        optparse.make_option(
            '-d', '--db-dir',
            dest='db_dir', help='directory for dbs [/tmp]',
        ),
        optparse.make_option(
            '-H', '--host',
            dest='host', help='hostname to bind on [localhost]',
        ),
        optparse.make_option(
            '-p', '--port', type="int",
            dest='port', help='port to bind to [8080]',
        ),
    ];
    parser.add_options(option_list);
    parser.set_defaults(
        debug=False, db_dir='/tmp',
        host='localhost', port=8080,
    );

    (options, args) = parser.parse_args();
    app.update(vars(options));

    # open dbs
    for name_of_db in ('shortcuts.db', 'trampolina.db', 'trampolina_old.db'):
        app[name_of_db] = sqldict.sqldict(filename=app['db_dir'] + '/' + name_of_db);


def run(app):
    debugmode, host, port = app['debug'], app['host'], app['port'];

    # run bottle
    bottle.debug(debugmode);
    bottle.run(host=host, port=port, reloader=debugmode);


def go():
    init(app);
    run(app);

app = {};

if (__name__ == '__main__'):
    go();
