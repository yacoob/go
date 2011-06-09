#!/usr/bin/env python
# coding: utf-8

import hashlib, sqlite3, threading
import cPickle as pickle
import UserDict


class sqldict(UserDict.DictMixin):
    def __init__(self, table=None, filename=None):
        if (not filename):
            filename = ':memory:'
        self.db = sqlite3.connect(filename, check_same_thread=False)
        self.filename = filename
        self.lock = threading.RLock()

        if (not table):
            table = 'dict'
        self.__execute(
            'create table if not exists %(tablename)s('
            'key TEXT primary key, key_orig TEXT, value TEXT);' %
            { 'tablename': table }
        )
        self.table = table


    def __pickle(self, object):
        return pickle.dumps(object)


    def __unpickle(self, string):
        return pickle.loads(str(string))


    def __hash_key(self, key):
        return hashlib.sha1(self.__pickle(key)).hexdigest()


    def __execute(self, sqlstring):
        return self.db.execute(sqlstring)


    def __getitem__(self, key):
        table_key = self.__hash_key(key)
        cursor = self.__execute(
            'select key, value from %(tablename)s where key = "%(key)s";' %
            { 'key': table_key, 'tablename': self.table }
        )
        rows = cursor.fetchall()
        if len(rows) == 1:
            return self.__unpickle(rows[0][1])
        elif len(rows) == 0:
            raise KeyError
        else:
            raise RuntimeError


    def __setitem__(self, key, value):
        table_key = self.__hash_key(key)
        table_key_orig = self.__pickle(key)
        table_value = self.__pickle(value)
        cursor = self.__execute(
            'select key, value from %(tablename)s where key = "%(key)s";' %
            { 'key': table_key, 'tablename': self.table }
        )
        rows = cursor.fetchall()
        self.lock.acquire()
        if len(rows) == 0:
            cursor = self.__execute(
                'insert into %(tablename)s (key, key_orig, value) '
                'values ("%(key)s", "%(key_orig)s", "%(value)s");' %
                {
                    'key': table_key,
                    'key_orig': table_key_orig,
                    'value': table_value,
                    'tablename': self.table,
                },
            )
        elif len(rows) == 1:
            cursor = self.__execute(
                'update %(tablename)s set value="%(value)s" '
                'where key = "%(key)s";' %
                {
                    'key': table_key,
                    'value': table_value,
                    'tablename': self.table,
                },
            )
        else:
            raise RuntimeError
        self.db.commit()
        self.lock.release()


    def __delitem__(self, key):
        table_key = self.__hash_key(key)
        self.lock.acquire()
        self.__execute(
            'delete from %(tablename)s where key = "%(key)s";' %
            {
                'key': table_key,
                'tablename': self.table,
            },
        )
        self.db.commit()
        self.lock.release()


    def keys(self):
        cursor = self.__execute(
            'select key_orig from %(tablename)s' %
            { 'tablename': self.table },
        )
        rows = cursor.fetchall()
        keys = map(lambda i: self.__unpickle(i[0]), rows)
        return keys

    # __contains__
    # __iter__
    # iteritems()
