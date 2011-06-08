#!/usr/bin/env python
# coding: utf-8

import threading, time, unittest
import trampoline


class TrampolineTest(unittest.TestCase):
    def setUp(self):
        self.app = { 'hop.db': {}, 'hop_old.db': {},
                     'host': 'localhost', 'port': 8080,
                     'timestamp_lock': threading.Lock() }

    def tearDown(self):
        del self.app

    def assertDictContainsSubset(self, expected, actual):
        # shamelessly borrowed from python 2.7
        missing = []
        mismatched = []
        for k, v in expected.iteritems():
            if k not in actual:
                missing.append(k)
            elif v != actual[k]:
                mismatched.append((k, v, actual[k]))

        if not (missing or mismatched):
            return

        msg = ''
        if missing:
            msg += 'Missing keys: ' + ', '.join(missing) + '. '

        if mismatched:
            msg += 'Invalid values for keys: ' + \
                   ', '.join('%s: %s != %s' % (x) for x in  mismatched)

        self.fail(msg)



class BasicUsageTestCase(TrampolineTest):
    def setUp(self):
        super(BasicUsageTestCase, self).setUp()
        # populate stack with some content
        self.app['hop.db'][time.time()] = 'http://www.google.com'


    def testInvalidCommand(self):
        r = { 'action': 'redir', 'url': '/hop/list' }
        p = trampoline.handle_command(self.app, 'frobnalize', {})
        self.assertDictContainsSubset(r, p)


    def testRssCommand(self):
        self.app['hop.db'][time.time()] = 'http://hell.pl'
        r = { 'action': 'template', 'template_name': 'hop_rss' }
        p = trampoline.handle_command(self.app, 'rss', {'requested_url':
            'http://}localhost:8080/hop/rss'})
        self.assertDictContainsSubset(r, p)
        self.assertEquals(len(p['template_args']['stack']), 2)


    def testPushCommand(self):
        db = self.app['hop.db']
        db_old = self.app['hop_old.db']
        params = {}

        r = { 'action': 'redir', 'url': '/hop/list' }
        p = trampoline.handle_command(self.app, 'push', params)
        self.assertEquals(r, p)

        params['url'] = 'http://hell.pl'
        r = { 'action': 'template', 'template_name': 'hop_msg' }
        p = trampoline.handle_command(self.app, 'push', params)
        self.assertDictContainsSubset(r, p)
        self.assertTrue(params['url'] in db.values())
        self.assertEquals(len(db.keys()), 2)
        self.assertEquals(len(db_old.keys()), 0)


    def testPopCommand(self):
        db = self.app['hop.db']
        db_old = self.app['hop_old.db']
        (first_timestamp, first_url) = db.items()[0]

        r = { 'action': 'redir', 'url': '/hop/list' }
        p = trampoline.handle_command(self.app, 'pop', { 'id': 999 })
        self.assertEquals(r, p)

        newurl = 'http://hell.pl'
        db[time.time()] = newurl
        r = { 'action': 'redir', 'url': newurl }
        p = trampoline.handle_command(self.app, 'pop', {})
        self.assertEquals(r, p)
        self.assertEquals(len(db.keys()), 1)
        self.assertEquals(len(db_old.keys()), 1)
        self.assertTrue(newurl in db_old.values())
        self.assertFalse(newurl in db.values())

        newurl = 'http://slashdot.org'
        db[time.time()] = newurl
        r = { 'action': 'redir', 'url': first_url }
        p = trampoline.handle_command(self.app, 'pop', {'id': first_timestamp})
        self.assertEquals(r, p)
        self.assertEquals(len(db.keys()), 1)
        self.assertEquals(len(db_old.keys()), 2)
        self.assertTrue(newurl in db.values())
        self.assertTrue(first_url in db_old.values())
        self.assertFalse(first_url in db.values())


    def testListCommand(self):
        self.app['hop.db'][time.time()] = 'http://hell.pl'
        r = { 'action': 'template', 'template_name': 'hop_list' }
        p = trampoline.handle_command(self.app, 'list', { 'json': '' })
        self.assertDictContainsSubset(r, p)
        self.assertEquals(len(p['template_args']['stack']), 2)


if __name__ == '__main__':
    unittest.main()
