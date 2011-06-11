'''
Bottle plugin for dict injection. Styled after official sqlite plugin. Best used with sqldict.

@author: yacoob@gmail.com
'''

import sqldict
import inspect

class dictPlugin(object):
    '''
    This plugin passes a dict into bottle callback. 
    '''
    def __init__(self, keyword='db', dictionary=None, table=None, filename=None):
        if dictionary:
            self.dict = dictionary.copy()
        else:
            self.dict = sqldict.sqldict(table, filename)
        self.keyword = keyword


    def setup(self, app):
        for plugin in app.plugins:
            if not isinstance(plugin, dictPlugin): continue
            if plugin.keyword == self.keyword:
                raise RuntimeError ("Found another dictPlugin with same keyword.")

    def apply(self, callback, context):
        args = inspect.getargspec(context['callback'])[0]
        if self.keyword not in args:
            return callback

        def wrapper(*args, **kwargs):
            kwargs[self.keyword] = self.dict

            return callback(*args, **kwargs)

        return wrapper
