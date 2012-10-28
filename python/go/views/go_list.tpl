<table class="table table-striped">
<thead><tr><td></td><td class="center"><b>Shortcut</b></td><td><b>URL</b></td></tr></thead>
% for row in list:
<tr>
<td class="controls center">
<a class="btn btn-mini" href="/and/del?short={{row[0]}}"><i class="icon-remove"></i></a>
<a class="btn btn-mini" href="/and/edit?short={{row[0]}}"><i class="icon-pencil"></i></a></td>
<td class="center">{{row[0]}}</td>
<td><a href="{{row[1]}}">{{row[1]}}</a></td>
</tr>
% end
</table>
% rebase base title='shortcuts list', header='Existing shortcuts'
