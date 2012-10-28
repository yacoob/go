<!DOCTYPE html>
<html><head>
<title>Go - {{title}}</title>
<link rel="icon" type="image/vnd.microsoft.icon" href="/static/favicon.ico">
<link type="text/css" rel="stylesheet" href="/static/bootstrap.min.css" />
<link type="text/css" rel="stylesheet" href="/static/go.css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head><body>

<div class="row-fluid">
<div class="span8">
<h1>{{header or 'Dummy header'}}</h1>
</div>
<div class="span4 buttons"><div class="btn-toolbar">
<div class="btn-group">
    <a class="btn btn-large" href="/and/list"><i class="icon-list"></i></a>
    <a class="btn btn-large" href="/and/add"><i class="icon-plus"></i></a>
</div>
<div class="btn-group">
    <a class="btn btn-large" href="/hop/list"><i class="icon-step-forward"></i></a>
    <a class="btn btn-large" href="/hop/pop"><i class="icon-ok"></i></a>
</div>
</div></div>
</div>

% include

</body></html>

