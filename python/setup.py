#!/usr/bin/env python

from setuptools import setup, find_packages
from glob import glob

setup(
    name='go',
    description='URL redirector with a twist',
    version='0.23',
    author='Jakub Turski',
    author_email='yacoob@gmail.com',
    url='http://github.com/yacoob/go',

    install_requires=['bottle>=0.9,<0.10'],

    packages=['go'],
    package_data={
        'go': ['static/favicon.ico', 'static/main.css', 'views/*.tpl']
    },
    entry_points={
        'console_scripts': [
            'go-runner = go.go:go'
        ],
    }
);