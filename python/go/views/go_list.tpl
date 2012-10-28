<div class="row-fluid"><div class="span10 offset1 main-content">
<table class="table table-striped">
<thead><tr><td></td><td><b>Shortcut</b></td><td><b>URL</b></td></tr></thead>
% for row in list:
<tr>
<td class="controls">
<a class="btn btn-mini" href="/and/del?short={{row[0]}}"><i class="icon-remove"></i></a>
<a class="btn btn-mini" href="/and/edit?short={{row[0]}}"><i class="icon-pencil"></i></a></td>
<td>{{row[0]}}</td>
<td><a href="{{row[1]}}">{{row[1]}}</a></td>
</tr>
% end
</table>
</div></div>
% args = { 'header': 'Existing shortcuts', 'title': get('title') }
% rebase base **args
