// preferences-related functions

// iterator helper
var __hide_prefs = [ 'hide_push', 'hide_pop', 'hide_list' ];

var __minimal_poll_interval = 30;

// default values of preferences, as stored in localStorage (strings)
var __default_prefs = {
    'base_url':         'http://v:9099',    // base URL of Trampolina
    'poll_interval':    '600',              // how often should we poll RSS?
    'hide_push':        '1',                // hide popup after 'push' button was clicked?
    'hide_pop':         '1',                // hide popup after 'pop' button was clicked?
    'hide_list':        '0'                 // hide popup after URL on a list was clicked?
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

    // RSS poll interval
    prefs.poll_interval = localStorage['poll_interval'];
    if (!prefs.poll_interval) {
        prefs.poll_interval = __default_prefs.poll_interval;
    };
    prefs.poll_interval = parseInt(prefs.poll_interval);

    // "hide preferences"
    // localStorage can only store strings, so some massaging is needed
    for (var i = 0; i < __hide_prefs.length; i += 1) {
        var n = __hide_prefs[i];
        var tmp = localStorage[n];

        if (!tmp) {
            tmp = __default_prefs[n];
            localStorage[n] = tmp;
        };

        // set value in prefs
        prefs[n] = (tmp == '1') ? true : false;
    };

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

    // RSS poll interval
    tmp = prefs.poll_interval + '';
    var tmp_int = parseInt(tmp);
    if (isNaN(tmp_int) || (tmp_int < __minimal_poll_interval)) {
        tmp = __default_prefs.poll_interval;
    };
    localStorage['poll_interval'] = tmp;


    // "hide preferences"
    for (var i = 0; i < __hide_prefs.length; i += 1) {
        var n = __hide_prefs[i];
        tmp = (prefs[n]) ? 1 : 0;
        localStorage[n] = tmp;
    };
};