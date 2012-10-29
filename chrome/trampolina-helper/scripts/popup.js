var PopUp = {
  // Time for which the 'push' message is displayed (OK/NOK).
  message_delay: 2500,
  // Time for which button should be highlighted after click.
  button_clicked_delay: 250,
};


// Poke background service to check for new items.
function pokeBackgroundService() {
  chrome.extension.sendMessage({msg: 'refreshList'});
};


// Display message in the popup, close it if requested.
function displayMessage(msg, close_window) {
  var status = $('div#msg');
  status.html(msg).toggle('fast');
  setTimeout(function() {
    status.toggle('fast');
    if (close_window) {
      window.close();
    };
  }, PopUp.message_delay);
};


// Open new tabs for specified urls, close the popup if requested.
function openTabs(urls, close_window, focused) {
  if (!urls instanceof Array) {
    return undefined;
  };
  for (var i = 0; i < urls.length; i++) {
    var url = urls[i];
    chrome.tabs.create({
      url: url, active: focused,
    });
  };
  pokeBackgroundService();
  if (close_window) {
    window.close();
  };
};


// Push URL of currently active tab.
function pushCurrentTab(pushUrl) {
  chrome.tabs.query({
    currentWindow: true, active: true
  }, function(tabs) {
    tab = tabs[0];
    var page_url = tab.url;
    var url = pushUrl + '?url=' + encodeURIComponent(page_url);
    jQuery.get(url, '', function() {
      displayMessage('Push OK!', true);
      pokeBackgroundService();
    }).error( function() {
      displayMessage('Uh-oh... push failed.', false);
    });
  });
};


// Update popup URL list.
function updatePopupList(data) {
  var html = '';
  var keys = Object.keys(data).sort().reverse();
  // FIXME: handle list larger than popup size.
  for (var i = 0; i < keys.length ; i++) {
    var u = data[keys[i]];
    html += '<a href="' + u.pop_url + '">' + u.description + '</a><br>';
  }
  $('div#list').html(html);
};


// Handle button click.
function buttons_handler(e) {
  // Have we actually clicked a trampolina button?
  if ($(e.target).is('.trampolina-button')) {
    // Give a visual feedback that button has been clicked.
    $(e.target).addClass('trampolina-clicked');
    setTimeout(function() {
      $(e.target).removeClass('trampolina-clicked');
    }, PopUp.button_clicked_delay);

    // Trigger some action.
    switch(e.target.id) {
      case 'push':
        pushCurrentTab(e.target.href);
        break;
      case 'pop':
        openTabs([e.target.href], true, true);
        break;
      case 'popAll':
        var urls = [];
        $('div#list a').each(function() { urls.push(this.href); });
        openTabs(urls, true, false);
        break;
      case 'list':
        openTabs([e.target.href], true, true);
        break;
    };

    // Inhibit the default click action.
    e.preventDefault();
    return false;
  };
};


// Handle URL click.
function urls_handler(e) {
  if ($(e.target).is('a')) {
    openTabs([e.target.href], true, false);
    e.preventDefault();
    return false;
  };
};


// Load options, setup handlers, load initial stack state.
function init() {
  loadPrefsAndRun(function(prefs) {
    // Setup buttons.
    var urls = ['push', 'pop', 'popAll', 'list'];
    for (var i = 0; i < urls.length; i += 1) {
      var element = 'a#' + urls[i] + '.trampolina-button';
      $(element)[0].href = prefs.base_url + '/' + urls[i];
    };

    // Setup click handlers.
    $('div#trampolina-buttons').click(buttons_handler);
    $('div#list').click(urls_handler);

    // Add messaging listener.
    chrome.extension.onMessage.addListener(function(request, sender, sendResponse) {
      switch(request.msg) {
        case 'dataUpdated':
          updatePopupList(request.chunk);
          break;
      };
    });

    // Get initial stack state.
    pokeBackgroundService();
  });
};

// Run init once page loads.
$(init);
