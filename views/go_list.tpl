<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<title>Go {{title}}</title>
<link rel="icon" type="image/vnd.microsoft.icon" href="/static/favicon.ico">
<link type="text/css" rel="stylesheet" href="/static/main.css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head><body>
% include header
<h1>Existing shortcuts</h1>
<small><a href="/and/add">(add a new one)</a></small>
<ul class="controls">
% for row in list:
<li>
<a class="controls" href="/and/del?short={{row[0]}}">✖</a>
<a class="controls" href="/and/edit?short={{row[0]}}">✎</a>
&nbsp; <code>{{row[0]}}</code> ➔ <code>{{row[1]}}</code></li>
% end
</ul>
</body></html>
