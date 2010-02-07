<html><head>
<title>Go</title>
<link type="text/css" rel="stylesheet" href="/static/main.css" />
</head><body>
<h1>{{message}}</h1>
<form method="GET" action="/and/add">
This short name: <input type="text" name="short" value="{{short}}"></input><br>
will redirect to: <input type="text" name="long" value="{{long}}"></input><br>
<input type="submit" value="Go!"></input>
</form>
</body></html>
