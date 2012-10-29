// Preferences-related functions, shared between all pages of extension.


var Prefs = {
  // Minimal interval at which extension will update data from server, in
  // seconds.
  minimal_poll_interval: 30,
  // Default values of preferences.
  default_prefs: {
    'base_url':       'http://go/hop',    // Server URL.
    'poll_interval':  120,                // How often should we poll server for updates?
  },
};

// Where should we store data?
storage = chrome.storage.local;


// Run func with current set of preferences provided.
function loadPrefsAndRun(func) {
  var keys = Object.keys(Prefs.default_prefs);
  storage.get(keys, function(items) {
    var prefs = {};
    for (var i = 0; i < keys.length; i++) {
      var key = keys[i];
      prefs[key] = items[key] || Prefs.default_prefs[key];
    };
    if (func instanceof Function) {
      func(prefs);
    };
  });
};


// Save preferences, using values from argument.
function savePrefs(prefs) {
  var items = {
    'base_url': prefs.base_url.trim().replace(/\/$/, ''),
    'poll_interval': prefs.poll_interval < Prefs.minimal_poll_interval ? Prefs.minimal_poll_interval : prefs.poll_interval,
  };
  storage.set(items);
  // Inform other extension pages that we saved (new?) prefs.
  chrome.extension.sendMessage({msg: 'prefsChanged'});
};
