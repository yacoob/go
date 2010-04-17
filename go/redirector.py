#!/usr/bin/env python
# coding: utf-8

import urllib;
import bottle;


def handle_shortcut(db, shortcut):
    if (db.has_key(shortcut)):
        # if redirect already exist, just go there
        bottle.redirect(db[shortcut]);
    else:
        # if it doesn't, redirect to prefilled edit form
        url = '/and/add?' + urllib.urlencode({'short': shortcut});
        bottle.redirect(url);

def handle_command(db, cmd):
    args = {};
    # FIXME: sanitize the params here
    args['short'] = bottle.request.GET.get('short', '');
    args['long'] = bottle.request.GET.get('long', '');

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

