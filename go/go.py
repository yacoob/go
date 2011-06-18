#!/usr/bin/env python
# coding: utf-8
# pylint: disable-msg=E0611

from pkg_resources import resource_filename #@UnresolvedImport
import bottle
from dict_plugin import dictPlugin
import optparse
import os
import redirector
import sys
import trampoline
import urllib


root = bottle.Bottle()

@root.route('/')
def index():
    bottle.redirect('/and/list')

@root.route('/:shortcut#[^&?/*]+#*')
def go_edit_that(shortcut):
    url = '/and/edit' + '?' + urllib.urlencode({'short': shortcut})
    bottle.redirect(url)

@root.route('/:shortcut#[^&?/*]+#')
def go_there(shortcut):
    # FIXME: This is suboptimal. It should be possible to route request 
    # for handling to another application.
    # https://github.com/defnull/bottle/issues/168
    bottle.redirect('/and/' + shortcut)

@root.route('/static/:filename')
def static_file(filename, app):
    bottle.send_file(filename, root=app['static_dir'])

def initFromCmdLine():
    app = {}
    data_dir = resource_filename(__name__, '')
    # parse command line
    parser = optparse.OptionParser()
    option_list = [
        optparse.make_option(
            '-D', '--debug',
            action='store_true', dest='debug',
            help='enable Bottle debug mode [false]',
        ),
        optparse.make_option(
            '-n', '--nofork',
            action='store_true', dest='nofork',
            help='do not daemonize at start [false]',
        ),
        optparse.make_option(
            '-d', '--db-dir',
            dest='db_dir', help='directory for dbs [/tmp]',
        ),
        optparse.make_option(
            '-a', '--data-dir',
            dest='data_dir', help='prefix for data directories [%s]' % data_dir,
        ),
        optparse.make_option(
            '-H', '--host',
            dest='host', help='hostname to bind on [localhost]',
        ),
        optparse.make_option(
            '-p', '--port', type="int",
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
    try:
        pid = os.fork()
        if pid > 0:
            sys.exit(0)
    except OSError, e:
        print >> sys.stderr, "Oh dear, failed to fork: %s (%s)" % (e.errno, e.strerror)
        sys.exit(1)

def daemonize():
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
    """Setup some reasonable defaults (even if running as dummy), run application"""
    if not app.has_key('servertype'):
        app['servertype'] = 'auto'
    if not app.get('data_dir'):
        app['data_dir'] = resource_filename(__name__, '')
    if not app.get('db_dir'):
        app['db_dir'] = '/tmp'

    app['static_dir'] = app['data_dir'] + '/static/'
    bottle.TEMPLATE_PATH = [ app['data_dir'] + '/views/' ]

    if app['debug']:
        bottle.debug(True)
    if (not app['nofork']):
        daemonize()


    trampoline.provisionDbs(app['db_dir'] + '/hop.db', app['db_dir'] + '/hop_old.db')
    redirector.provisionDbs(app['db_dir'] + '/and.db')

    root.mount(trampoline.app, '/hop')
    root.mount(redirector.app, '/and')

    root.install(dictPlugin(keyword='app', dictionary=app))

    bottle.run(root, host=app['host'], port=app['port'], server=app['servertype'])

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
