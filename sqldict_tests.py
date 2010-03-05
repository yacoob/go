#!/usr/bin/env python
# coding: utf-8

import os, sqlite3, unittest;
import sqldict;


class SqlDictTest(unittest.TestCase):
    def setUp(self):
        pass

    def tearDown(self):
        pass


class BasicUsageTestCase(SqlDictTest):
    def testSimpleSetGet(self):
        b = sqldict.sqldict();
        # store int
        b[1] = 42;
        # store string
        b[2] = 'fnord';
        # store dict
        b[3] = {'hail': 2, 'eris': 3 };

        self.assertEquals(b[1], 42);
        self.assertEquals(b[2], 'fnord');
        self.assertEquals(b[3], {'hail': 2, 'eris': 3});


    def testNonTrivialKeys(self):
        b = sqldict.sqldict();
        # key is a string
        b['23skidoo'] = 23;
        # key is an object
        x = object();
        b[x] = 42;

        self.assertEquals(b['23skidoo'], 23);
        self.assertEquals(b[x], 42);


    def testEqualObjectsAsKeys(self):
        b = sqldict.sqldict();
        index_one = { 1: 2, 3: 4 };
        index_two = { 1: 2, 3: 4 };
        self.assertEquals(index_one is index_two, False);
        self.assertEquals(index_one, index_two);
        # test whether using equal keys sets the same entry in sqldict
        b[index_one] = 42;
        self.assertEquals(b[index_one], b[index_two]);
        b[index_two] = 23;
        self.assertEquals(b[index_one], 23);

    def testNamedTable(self):
        b = sqldict.sqldict('blam');
        c = sqldict.sqldict(filename=b.filename);
        # test whether b is using specified table name
        b[1] = 2;
        db = sqlite3.connect(b.filename);
        cursor = db.execute('select * from blam');
        rows = cursor.fetchall();
        self.assertNotEqual(len(rows), 0);
        # test whether b and c are using different tables
        c[1] = 3;
        self.assertNotEqual(b[1], c[1]);


    def testNamedFile(self):
        b = sqldict.sqldict(filename='/tmp/bla');
        c = sqldict.sqldict(filename='/tmp/bla');
        b[1] = 2;
        # test whether b is using specified file name
        db = sqlite3.connect('/tmp/bla');
        os.unlink('/tmp/bla');
        cursor = db.execute('select * from dict');
        rows = cursor.fetchall();
        self.assertNotEqual(len(rows), 0);
        # test whether b and c are using the same file and table
        self.assertEquals(c[1], 2);


if __name__ == '__main__':
    unittest.main();
