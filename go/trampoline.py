#!/usr/bin/env python
# coding: utf-8

import threading, time, urllib;
import bottle;

def handle_command(db, db_old, cmd, timestamp_lock):
    args = {};
    # FIXME: sanitize the params here
    args['url'] = bottle.request.GET.get('url', '');
    args['id'] = bottle.request.GET.get('id', '');

    if (cmd == 'push'):
        if (args['url']):
            timestamp_lock.acquire();
            db[str(time.time())] = args['url'];
            timestamp_lock.release();
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
