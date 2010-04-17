#!/usr/bin/env python
# coding: utf-8

import urllib;


def handle_shortcut(app, shortcut):
    db = app['shortcuts.db'];
    if (db.has_key(shortcut)):
        # if redirect already exist, just go there
        url = db[shortcut];
    else:
        # if it doesn't, redirect to prefilled edit form
        url = '/and/add?' + urllib.urlencode({'short': shortcut});
    return { 'action': 'redir', 'url': url };


def handle_command(app, cmd, params):
    args = {};
    args.update(params);
    db = app['shortcuts.db'];

    if (cmd == 'add'):
        # add new redirect
        args['message'] = 'Add a new shortcut:';
        args['title'] = '- add a new shortcut';
        if args['short'] and args['long']:
            # if both short and long name are provided, add it straight away
            db[args['short']] = args['long'];
            return { 'action': 'redir', 'url': '/and/list' };
        else:
            # otherwise, present user with edit form
            return { 'action': 'template', 'template_name': 'go_edit',
                     'template_args': args };

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
            return { 'action': 'template', 'template_name': 'go_edit',
                     'template_args': args };
        else:
            # redirect to / if no name was supplied
            return { 'action': 'redir', 'url': '/and/list' };

    elif (cmd == 'del'):
        # delete a redirect
        if args['short'] and db.has_key(args['short']):
            # if it exist, remove it and redirect to edit page, as a last
            # chance to save this shortcut
            args['message'] = 'Old shortcut removed, but you can always add it back here:';
            args['long'] = db[args['short']];
            args['title'] = '- last chance to save a shortcut!';
            del db[args['short']];
            return { 'action': 'template', 'template_name': 'go_edit',
                     'template_args': args };
        else:
            url = '/and/add?' + urllib.urlencode({'short': args['short']});
            return { 'action': 'redir', 'url': url };

    elif (cmd == 'list'):
        # show list of all redirects
        shortcuts = db.items();
        shortcuts.sort();
        args = {
          'list':  shortcuts,
          'title': '- shortcuts list',
        };
        return { 'action': 'template', 'template_name': 'go_list',
                 'template_args': args };

    else:
        # default endpoint
        return { 'action': 'redir', 'url': '/and/list' };
