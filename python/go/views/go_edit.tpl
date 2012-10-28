<form class="form-horizontal" method="GET" action="/and/add">
<div class="control-group">
    <label class="control-label" for="inputShort">Shortcut name</label>
    <div class="controls">
    <input class="input-medium" id="inputShort" placeholder="shortcut" type="text" name="short" value="{{short}}"/>
    </div>
</div>
<div class="control-group">
    <label class="control-label" for="inputLong">URL this shortcut redirects to</label>
    <div class="controls">
    <input class="input-xxlarge" id="inputLong" placeholder="URL" type="text" name="long" value="{{long}}"/>
    </div>
</div>
<div class="control-group">
    <div class="controls">
    <button type="submit" class="btn btn-primary">Go!</button>
    </div>
</div>
</form>
% rebase base header=message, title=title
