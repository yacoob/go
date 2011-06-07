#!/usr/bin/env python
# coding: utf-8

import json, urlparse, time;

def describe_urls(db, json=False):
    entries = db.items();
    entries.sort(reverse=True);
    descriptive = [
        (time.strftime('%a, %d of %b %Y, %H:%M',
                       time.localtime(float(e[0]))),
         e[0], e[1])
        for e in entries];
    if json:
        return [{'date': x[0], 'id': x[1], 'url': x[2]} for x in descriptive];
    else:
        return descriptive;


def push_url(app, args):
    if (args.has_key('url') and args['url']):
        db = app['hop.db'];
        app['timestamp_lock'].acquire();
        db[str(time.time())] = args['url'];
        app['timestamp_lock'].release();
        args['title'] = '- trampoline push succeeded';
        return { 'action': 'template', 'template_name': 'hop_msg',
                 'template_args': args };
    else:
        return { 'action': 'redir', 'url': '/hop/list' };


def pop_url(app, args):
    db = app['hop.db'];
    db_old = app['hop_old.db'];
    urls_keys = db.keys();
    urls_keys.sort();
    latest_id = urls_keys[-1] if len(urls_keys) else 0;
    id = args['id'] if (args.has_key('id') and args['id']) else latest_id;
    if (id not in urls_keys):
        return { 'action': 'redir', 'url': '/hop/list' };

    url = db[id];
    del db[id];

    # FIXME: purge old urls from db_old
    db_old[id] = url;

    return { 'action': 'redir', 'url': url };


def list_urls(app, args):
    db = app['hop.db'];
    db_old = app['hop_old.db'];
    if (args['json']):
        args = {
            'jsonized': json.dumps({'stack': describe_urls(db, True),
                                    'viewed': describe_urls(db_old, True)})
            };
        template = 'json';
    else:
        args = {
            'stack':  describe_urls(db),
            'viewed': describe_urls(db_old),
            'title':  '- trampoline URLs list',
            };
        template = 'hop_list';

    return { 'action': 'template', 'template_name': template,
             'template_args': args };


def rss(app, args):
    db = app['hop.db'];
    urls = db.items();
    urls.sort(reverse=True);
    describe = lambda item: {
        'url': item[1],
        'timestamp': item[0],
        'datetime': time.ctime(float(item[0])),
    };
    # Try to reconstruct base_url from requested one.
    parsed_req_url = urlparse.urlparse(args['requested_url']);
    base_url = urlparse.urlunparse(parsed_req_url[0:2] + ('', '', '', ''));
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


def rest(app, args):
    return { 'action': 'redir', 'url': '/hop/list' };


def handle_command(app, cmd, params):

    cmd_map = { 'push': push_url,
                'pop':  pop_url,
                'list': list_urls,
                'rss':  rss,
                'rest': rest };

    args = {};
    args.update(params);

    if cmd_map.has_key(cmd):
        result = cmd_map[cmd](app, args);
    else:
        result = { 'action': 'redir', 'url': '/hop/list' };

    return result;
