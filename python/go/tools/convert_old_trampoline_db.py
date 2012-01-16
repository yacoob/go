#!/usr/bin/env python
# coding: utf-8

import os
import sqldict
import sqlite3
import sys
import trampoline
import urllib
import uuid

db = sqlite3.connect('/tmp/trampoline.db')
base_url = 'http://localhost:8080/hop/push'
for f in sys.argv[1:]:
    d = sqldict.sqldict(None, f)
    for k in d.keys():
        db.execute('INSERT INTO viewed(timestamp, url, token) VALUES(?, ?, ?)',
                (k, d[k].decode('utf-8'), uuid.uuid4().hex))
    db.commit()
