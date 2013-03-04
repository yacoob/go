// Timer object for scheduled refresh of server data.
var t;


// Force a refresh.
function poke() {
  stopUpdates();
  t = window.setTimeout(refresh, 500);
};


// Stop automatic refresh.
function stopUpdates() {
  window.clearTimeout(t);
};


// Update extension icon's badge.
function updateBadge(num) {
  if (num) {
    chrome.browserAction.setBadgeText({text: num + ''});
  } else {
    chrome.browserAction.setBadgeText({text: ''});
  };
};


// Update local data.
function updateData(data) {
  $('div#url-list').data('urls', data);
  updateBadge(Object.keys(data).length);
  console.debug('background.html: local data updated.');
  chrome.extension.sendMessage({msg: 'dataUpdated', chunk: data});
}


// Fetch data from remote server.
function refresh() {
  loadPrefsAndRun(function(prefs) {
    jQuery.getJSON(
      prefs.base_url + '/r/stack/*', '', updateData
    ).error(function() {
      updateData({});
    });

    // Schedule next refresh.
    t = window.setTimeout(refresh, prefs.poll_interval * 1000);
  });
};


// Set up infrastructure, start the refresh cycle.
function init() {
  // Add listeners.
  chrome.extension.onMessage.addListener(function(request, sender, sendResponse) {
    switch(request.msg) {
      case 'refreshList':
      case 'prefsChanged':
        poke()
        break;
      };
    });

    // start refresh cycle
    refresh();
};

// Run init once page loads.
$(init);
