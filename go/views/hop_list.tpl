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
%   for row in stack:
%     day = row[0][0:3]
%     if day != oldday:
<tr class="topborder">
<td class="timestamp" colspan=2>{{row[0][:-7]}}</td>
</tr>
%     end
<tr><td class="timestamp">{{row[0][-5:]}}</td><td class="url"><a href="{{pop_url}}?id={{row[1]}}">{{row[2]}}</a></td></tr>
%     oldday = day
%   end
</table>
% end


<p>&nbsp;</p>


% if len(viewed):
<h1>Old URLs</h1>
<table>
%   oldday = ''
%   for row in viewed:
%     day = row[0][0:3]
%     if day != oldday:
<tr class="topborder">
<td class="timestamp" colspan=2>{{row[0][:-7]}}</td>
</tr>
%     end
<tr>
<td class="timestamp">{{row[0][-5:]}}</td><td class="url"><a href="{{row[2]}}">{{row[2]}}</a></td>
</tr>
%     oldday = day
%   end
</table>
% end

</body></html>
