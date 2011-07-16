<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<link rel="alternate" type="application/rss+xml" href="/hop/rss" title="URLs feed">
<link rel="icon" type="image/vnd.microsoft.icon" href="/static/favicon.ico">
<link type="text/css" rel="stylesheet" href="/static/main.css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Go {{title}}</title>
</head><body>
% include header
% if len(stack):
<h1>New URLs</h1>
<table>
%   oldday = ''
%   for id in sorted(stack.keys(), reverse=True):
%     url = stack[id]
%     day = url['day']
%     if day != oldday:
<tr class="topborder">
<td class="timestamp" colspan=2>{{day}}</td>
</tr>
%     end
<tr><td class="timestamp">{{url['time']}}</td><td class="url"><a href="{{url['pop_url']}}">{{url['url']}}</a></td></tr>
%     oldday = day
%   end
</table>
% end


<p>&nbsp;</p>


% if len(viewed):
<h1>Old URLs</h1>
<table>
%   oldday = ''
%   for id in sorted(viewed.keys(), reverse=True):
%     url = viewed[id]
%     day = url['day']
%     if day != oldday:
<tr class="topborder">
<td class="timestamp" colspan=2>{{day}}</td>
</tr>
%     end
<tr>
<td class="timestamp">{{url['time']}}</td><td class="url"><a href="{{url['url']}}">{{url['url']}}</a></td>
</tr>
%     oldday = day
%   end
</table>
% end

</body></html>