#!/usr/bin/env python
# coding: utf-8
# pylint: disable-msg=C0103

""" Main module."""

from optparse import OptionParser, make_option
from pkg_resources import resource_filename # pylint: disable-msg=E0611
from sys import stderr
import bottle
import os
import redirector
import trampoline


root = bottle.Bottle()


@root.route('/')
def index():
    """ Default handler: redirector list."""
    bottle.redirect('/and/list')


@root.route('/<shortcut:re:[^&?/]+[*]?>')
def go_there(shortcut):
    """ Convenience shortcut for redirectors, to account for 'go' domain
    name."""
    # FIXME: This is suboptimal. It should be possible to route request
    # for handling to another application.
    # https://github.com/defnull/bottle/issues/168
    bottle.redirect('/and/' + shortcut)

@root.route('/img/<filename>')
@root.route('/static/<filename>')
def send_file(filename):
    """ Handler for static files."""
    return bottle.static_file(filename, root=root.static_dir)

def initFromCmdLine():
    """ Initialize application according to commandline flags."""
    app = {}
    data_dir = resource_filename(__name__, '')
    # parse command line
    parser = OptionParser()
    option_list = [
        make_option(
            '-D', '--debug',
            action='store_true', dest='debug',
            help='enable Bottle debug mode [false]',
        ),
        make_option(
            '-n', '--nofork',
            action='store_true', dest='nofork',
            help='do not daemonize at start [false]',
        ),
        make_option(
            '-d', '--db-dir',
            dest='db_dir', help='directory for dbs [/tmp]',
        ),
        make_option(
            '-a', '--data-dir',
            dest='data_dir', help='prefix for data directories [%s]' % data_dir,
        ),
        make_option(
            '-H', '--host',
            dest='host', help='hostname to bind on [localhost]',
        ),
        make_option(
            '-p', '--port', type='int',
            dest='port', help='port to bind to [8080]',
        ),
    ]
    parser.add_options(option_list)
    parser.set_defaults(
        debug=False, nofork=False,
        host='localhost', port=8080,
    )

    options = parser.parse_args()[0]
    app.update(vars(options))
    return app

def fork():
    """ Fork helper."""
    try:
        pid = os.fork()
        if pid > 0:
            exit(0)
    except OSError, e:
        print >> stderr, 'Oh dear, failed to fork: %s (%s)' % \
              (e.errno, e.strerror)
        exit(1)

def daemonize():
    """ Turn process into a daemon."""
    # fork once, to create own session
    fork()

    # start new session
    os.setsid()

    # make sure we won't get a controlling terminal, ever
    fork()
    os.chdir('/')
    os.umask(0)

    fd = os.open(os.devnull, os.O_RDWR)
    os.dup2(fd, 0)
    os.dup2(fd, 1)
    os.dup2(fd, 2)
    os.close(fd)

def run(app):
    """Setup some reasonable defaults (even if running as dummy), run
    application"""
    if not app.has_key('servertype'):
        app['servertype'] = 'auto'
    if not app.get('data_dir'):
        app['data_dir'] = resource_filename(__name__, '')
    if not app.get('db_dir'):
        app['db_dir'] = '/tmp'

    root.static_dir = app['data_dir'] + '/static/'
    bottle.TEMPLATE_PATH = [ app['data_dir'] + '/views/' ]

    if app['debug']:
        bottle.debug(True)
    if (not app['nofork']):
        daemonize()

    dbfile = app['db_dir'] + '/trampoline.db'
    trampoline.provisionDbs(dbfile)
    redirector.provisionDbs(dbfile)

    root.mount(trampoline.app, '/hop')
    root.mount(redirector.app, '/and')

    bottle.run(root, host=app['host'], port=app['port'],
            server=app['servertype'])

def go():
    """Referenced by setup.py, main point of entry for "production" use."""
    run(initFromCmdLine())


if (__name__ == '__main__'):
    # Setup full application on localhost, in debug mode, without forking.
    dummy_app = {
        'debug': True, 'nofork': True, 'host': 'localhost', 'port': '8080',
        'servertype': 'wsgiref',
    }
    run(dummy_app)
