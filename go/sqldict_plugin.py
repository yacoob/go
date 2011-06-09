'''
Bottle plugin for sqldict. Styled after official sqlite plugin.

@author: yacoob@gmail.com
'''

import sqldict
import inspect

class sqldictPlugin(object):
    '''
    This plugin passes an sqldict into bottle callback that takes argument named after keyword. 
    '''


    def __init__(self, keyword='db', table=None, filename=None):
        self.dict = sqldict.sqldict(table, filename)
        self.keyword = keyword


    def setup(self, app):
        for plugin in app.plugins:
            if not isinstance(plugin, sqldictPlugin): continue
            if plugin.keyword == self.keyword:
                raise RuntimeError ("Found another sqldict plugin with same keyword.")

    def apply(self, callback, context):
        args = inspect.getargspec(context['callback'])[0]
        if self.keyword not in args:
            return callback

        def wrapper(*args, **kwargs):
            kwargs[self.keyword] = self.dict

            return callback(*args, **kwargs)

        return wrapper
