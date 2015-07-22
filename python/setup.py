#!/usr/bin/env python

from setuptools import setup

setup(
    name='go',
    description='URL redirector with a twist',
    version='0.4',
    author='Jakub Turski',
    author_email='yacoob@gmail.com',
    url='http://github.com/yacoob/go',
    install_requires=[
        'bottle>=0.10, <0.11',
        'bottle-sqlite',
        'BeautifulSoup',
        'chardet',
        'daemonize'],
    packages=['go'],
    package_data={
        'go': [
            'static/*.*',
            'views/*.tpl']},
    entry_points={
        'console_scripts': ['go-runner = go.go:go'],
    })
