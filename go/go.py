#!/usr/bin/env python
# coding: utf-8

import optparse, threading, urllib;
import bottle;

import redirector;
import sqldict;
import trampoline;


def handle_response(r):
    if not r.has_key('action'):
        raise bottle.HTTPError;
    w = r['action'];
    if (w == 'redir'):
        return bottle.redirect(r['url']);
    elif (w == 'template'):
        bottle.response.content_type = r['content_type'] if r.has_key('content_type') else 'text/html';
        return bottle.template(r['template_name'], **r['template_args']);


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
    return handle_response(redirector.handle_shortcut(app, shortcut));


@bottle.route('/and/(?P<cmd>.*)')
def go_command(cmd):
    params = { 'short': bottle.request.GET.get('short', ''),
               'long':  bottle.request.GET.get('long', '') };
    return handle_response(redirector.handle_command(app, cmd, params));


@bottle.route('/hop/(?P<cmd>.*)')
def hop_command(cmd):
    params = { 'url': bottle.request.GET.get('url', ''),
               'id':  bottle.request.GET.get('id', '') };
    return handle_response(trampoline.handle_command(app, cmd, params));


@bottle.route('/static/:filename')
def static_file(filename):
    bottle.send_file(filename, root=app['data_dir'] + '/static/');


def init(app):
    # parse command line
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
            '-a', '--data-dir',
            dest='data_dir', help='prefix for data directories [/usr/share/go]',
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
        debug=False, db_dir='/tmp', data_dir='/usr/share/go',
        host='localhost', port=8080,
    );

    (options, args) = parser.parse_args();
    app.update(vars(options));

    # open dbs
    for name_of_db in ('and.db', 'hop.db', 'hop_old.db'):
        app[name_of_db] = sqldict.sqldict(filename=app['db_dir'] + '/' + name_of_db);

    bottle.TEMPLATE_PATH = [ app['data_dir'] + '/views/' ];
    app['timestamp_lock'] = threading.Lock();


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
