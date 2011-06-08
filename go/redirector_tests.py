#!/usr/bin/env python
# coding: utf-8

import unittest
import redirector


class RedirectorTest(unittest.TestCase):
    def setUp(self):
        self.app = { 'and.db': {} }

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



class BasicUsageTestCase(RedirectorTest):
    def setUp(self):
        super(BasicUsageTestCase, self).setUp()
        self.app['and.db']['g'] = 'http://www.google.com'


    def testShortcutHandling(self):
        r = { 'action': 'redir', 'url': 'http://www.google.com' }
        self.assertEquals(r, redirector.handle_shortcut(self.app, 'g'))
        r = { 'action': 'redir', 'url': '/and/add?short=nosuchthing' }
        self.assertEquals(r, redirector.handle_shortcut(self.app, 'nosuchthing'))
        r = { 'action': 'redir', 'url': '/and/add?short=bl%21p' }
        self.assertEquals(r, redirector.handle_shortcut(self.app, 'bl!p'))


    def testInvalidCommand(self):
        r = { 'action': 'redir', 'url': '/and/list' }
        self.assertEquals(r, redirector.handle_command(self.app, 'frobnalize', {}))


    def testAddCommand(self):
        params = { 'short': 'h', 'long': '' }
        r = { 'action': 'template', 'template_name': 'go_edit' }
        p = redirector.handle_command(self.app, 'add', params)
        self.assertDictContainsSubset(r, p)

        params['long'] = 'http://hell.pl'
        r = { 'action': 'redir', 'url': '/and/list' }
        self.assertEquals(r, redirector.handle_command(self.app, 'add', params))
        self.assertEquals(self.app['and.db']['h'], 'http://hell.pl')


    def testEditCommand(self):
        params = {}
        r = { 'action': 'redir', 'url': '/and/add' }
        p = redirector.handle_command(self.app, 'edit', params)
        self.assertDictContainsSubset(r, p)

        params['short'] = 'g'
        r = { 'action': 'template', 'template_name': 'go_edit' }
        p = redirector.handle_command(self.app, 'edit', params)
        self.assertDictContainsSubset(r, p)
        params['long'] = self.app['and.db'][params['short']]
        self.assertDictContainsSubset(params, p['template_args'])


    def testDelCommand(self):
        params = { 'short': 'nosuchthing' }
        r = { 'action': 'redir', 'url': '/and/add?short=nosuchthing' }
        self.assertEquals(r, redirector.handle_command(self.app, 'del', params))
        params = { 'short': 'g' }
        r = { 'action': 'template', 'template_name': 'go_edit' }
        p = redirector.handle_command(self.app, 'del', params)
        self.assertDictContainsSubset(r, p)
        self.assertDictContainsSubset(params, p['template_args'])
        self.assertTrue(not self.app['and.db'].has_key('g'))


    def testListCommand(self):
        r = { 'action': 'template', 'template_name': 'go_list' }
        p = redirector.handle_command(self.app, 'list', {})
        self.assertEquals(r['action'], p['action'])
        self.assertEquals(r['template_name'], p['template_name'])


if __name__ == '__main__':
    unittest.main()
