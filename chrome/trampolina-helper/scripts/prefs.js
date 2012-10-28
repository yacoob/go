// Preferences-related functions, shared between all pages of extension.

var __minimal_poll_interval = 30;

// default values of preferences, as stored in localStorage (strings)
var __default_prefs = {
    'base_url':         'http://go/hop',    // base URL of Trampolina
    'poll_interval':    '120',              // how often should we poll RSS?
};

// load existing preferences, set default values if there are no prefs yet
function loadPrefs() {
    prefs = {};

    // base_url
    prefs.base_url = localStorage['base_url'];
    if (!prefs.base_url) {
        prefs.base_url = __default_prefs.base_url;
        localStorage['base_url'] = prefs.base_url;
    };

    // Data poll interval.
    prefs.poll_interval = localStorage['poll_interval'];
    if (!prefs.poll_interval) {
        prefs.poll_interval = __default_prefs.poll_interval;
    };
    prefs.poll_interval = parseInt(prefs.poll_interval);

    // return preferences object;
    return prefs;
};


// save preferences, using values from argument
function savePrefs(prefs) {
    // base_url
    var tmp = prefs.base_url.trim();
    // strip '/' from the end of the URL
    if (tmp[tmp.length-1] == '/') {
      tmp = tmp.slice(0, -1);
    };
    localStorage['base_url'] = tmp;

    // Data poll interval.
    tmp = prefs.poll_interval + '';
    var tmp_int = parseInt(tmp);
    if (isNaN(tmp_int) || (tmp_int < __minimal_poll_interval)) {
        tmp = __default_prefs.poll_interval;
    };
    localStorage['poll_interval'] = tmp;
};
