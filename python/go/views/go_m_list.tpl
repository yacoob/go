<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<title>Go {{title}}</title>
<link rel="icon" type="image/vnd.microsoft.icon" href="/static/favicon.ico">
<link type="text/css" rel="stylesheet" href="/static/main.css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head><body>
<table>
<ul class="controls">
% for row in list:
<li>{{row[0]}}<br>
{{row[1]}}
% end
</ul>
</table>
</ul>
</body></html>
