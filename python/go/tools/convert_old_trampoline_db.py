#!/usr/bin/env python
# coding: utf-8

# Quick and dirty migration script for go databases.
#
# - grab and.db, hop.db, hop_old.db from old installation
# - install version >=0.3
# - run it (to create new db), copy trampoline.db to /tmp
# - run this script on old databases as show below
# - copy /tmp/trampoline.db back to /var/lib/go
#
# If you need sqldict.py:
#  wget https://raw.github.com/yacoob/go/master/python/go/sqldict.py

import os
import sqldict
import sqlite3
import sys
import trampoline
import urllib
import uuid

if len(sys.argv) == 1:
    print 'Usage: %s (hop|and) files...' % sys.argv[0]
    exit(1)

acceptable_flavors = ('hop', 'and')
flavor = sys.argv[1]
if flavor not in acceptable_flavors:
    print 'Please specify valid flavor of db.'
    exit(1)

output_file = '/tmp/trampoline.db'
db = sqlite3.connect(output_file)

for f in sys.argv[2:]:
    d = sqldict.sqldict(None, f)
    for k in d.keys():
        if flavor == 'hop':
            db.execute('INSERT INTO viewed(timestamp, url, token) VALUES(?, ?, ?)',
                    (k, d[k].decode('utf-8'), uuid.uuid4().hex))
        elif flavor == 'and':
            db.execute('INSERT INTO redirector(shortcut, url) VALUES(?, ?)',
                    (k, d[k].decode('utf-8')))
    db.commit()

print 'Check out %s for results of conversion.' % output_file
