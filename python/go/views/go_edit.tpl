<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<title>Go {{title}}</title>
<link rel="icon" type="image/vnd.microsoft.icon" href="/static/favicon.ico">
<link type="text/css" rel="stylesheet" href="/static/main.css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head><body>
% include header
<h1>{{message}}</h1>
<form method="GET" action="/and/add">
<input type="text" name="short" value="{{short}}" size=30></input> ➔
<input type="text" name="long" value="{{long}}" size=150></input><p>
<input type="submit" value="Go!"></input>
</form>
</body></html>
