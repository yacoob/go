<html><head>
<title>Go {{title}}</title>
<link type="text/css" rel="stylesheet" href="/static/main.css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head><body>
<h1>Existing shortcuts</h1>
<ul>
% for row in list:
<li>
<a href="/and/del?short={{row[0]}}">[X]</a>
<a href="/and/edit?short={{row[0]}}">[E]</a>
: {{row[0]}} ➔ {{row[1]}}</li>
% end
</ul>
</body></html>
