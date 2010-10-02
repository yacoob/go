#!/usr/bin/env python

from distutils.core import setup
from glob import glob

setup(name='go',
      description='URL redirector with a twist',
      version='0.2',
      author='Jakub Turski',
      author_email='yacoob@gmail.com',
      url='http://github.com/yacoob/go',

      requires=['bottle (>=0.8)'],

      packages=['go'],
      scripts=['go-runner.py'],
      data_files=[('share/go/static', ['static/favicon.ico', 'static/main.css']),
                  ('share/go/views', glob('views/*.tpl'))],
     );
