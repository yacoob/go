#!/usr/bin/env python
# coding: utf-8

import time;


def handle_command(app, cmd, params):
    args = {};
    args.update(params);
    db = app['hop.db'];
    db_old = app['hop_old.db'];

    if (cmd == 'push'):
        if (args['url']):
            app['timestamp_lock'].acquire();
            db[str(time.time())] = args['url'];
            app['timestamp_lock'].release();
            args['title'] = '- trampoline push succeeded';
            return { 'action': 'template', 'template_name': 'hop_msg',
                     'template_args': args };
        else:
            return { 'action': 'redir', 'url': '/hop/list' };

    elif (cmd == 'pop'):
        urls_keys = db.keys();
        urls_keys.sort();
        latest_id = urls_keys[-1] if len(urls_keys) else 0;
        id = args['id'] if args['id'] else latest_id;
        if (id not in urls_keys):
            return { 'action': 'redir', 'url': '/hop/list' };

        url = db[id];
        del db[id];

        # FIXME: purge old urls from db_old
        db_old[id] = url;

        return { 'action': 'redir', 'url': url };

    elif (cmd == 'list'):
        urls = db.items();
        urls.sort(reverse=True);
        old_urls = db_old.items();
        old_urls.sort(reverse=True);
        args = {
            'stack':  urls,
            'viewed': old_urls,
            'title':  '- trampoline URLs list',
        };
        return { 'action': 'template', 'template_name': 'hop_list',
                 'template_args': args };

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
           'title': '- new trampoline URLs',
           'description': 'New URLs on trampoline',
           'timestamp': time.ctime(),
           'list_url': base_url + '/hop/list',
           'pop_url': base_url + '/hop/pop?id=',
        };
        return { 'action': 'template', 'template_name': 'hop_rss',
                 'template_args': args, 'content_type': 'text/xml' };

    else:
        # default endpoint
        return { 'action': 'redir', 'url': '/hop/list' };
