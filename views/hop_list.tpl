<html><head>
<link rel="alternate" type="application/rss+xml" href="/hop/rss" title="URLs feed">
<link rel="icon" type="image/vnd.microsoft.icon" href="/static/favicon.ico">
<link type="text/css" rel="stylesheet" href="/static/main.css" />
<title>Go {{title}}</title>
</head><body>
<h1>New URLs:</h1>
<ol>
% i = len(stack)
% for row in stack:
<li value="{{i}}"><a href="/hop/pop?id={{++i}}">{{row[1]}}</a></li>
% i = i - 1
% end
</ol>
<hr>
<h1>Old URLs:</h1>
<ul>
% for row in viewed:
<li><a href="{{row[1]}}">{{row[1]}}</a></li>
% end
</ul>
</body></html>
