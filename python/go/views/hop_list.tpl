% def present_list(list, base_url_type):
%   oldday = ''
%   urls = sorted(list.keys(), reverse=True)
%   url_count = len(urls)
%   for idx, id in enumerate(urls, start=1):
%     url = list[id]
%     day = url['day']
%     if day != oldday: # start new day section
%       if oldday != '': # close previous section if needed
</table>
%       end
<h5>{{day}}</h5>
<table class="table table-striped table-bordered">
%     end
<tr><td class="timestamp">{{url['time']}}</td><td><a href="{{url[base_url_type]}}">{{url['description'] or url['url']}}</a></td></tr>
%     oldday = day
%     if idx == url_count:
</table>
%     end
%   end
% end

% if len(stack):
<h3>New URLs</h3>
% present_list(stack, 'pop_url')
% end

<p>&nbsp;</p>

% if len(viewed):
<h3>Old URLs</h3>
% present_list(viewed, 'url')
% end

% rebase base header='Trampoline', title='trampoline URLs list'
