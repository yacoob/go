// maximum number of URLs to show in the popup
var __max_items = 6;

// preferences object
var prefs = {};

// timer object
var t;


// force refresh
function poke() {
    stopUpdates();
    t = window.setTimeout(refresh, 500);
};


// stop updates
function stopUpdates() {
    window.clearTimeout(t);
};


// update extension icon's badge
function updateBadge(num) {
    if (num) {
        chrome.browserAction.setBadgeText({text: num + ''});
    } else {
        chrome.browserAction.setBadgeText({text: ''});
    };
};


function updateData(data) {
    $('div#url-list').data('urls', data);
    updateBadge(Object.keys(data).length);
    console.debug('background.html: sending updateList');
    chrome.extension.sendMessage({msg: 'updateList', chunk: data});
}


// refresh Trampoline data
function refresh(callback) {
    jQuery.getJSON(
        prefs.base_url + '/r/stack/*', '', updateData
    ).error(function() {
        updateData({});
    });

    // schedule next run
    t = window.setTimeout(refresh, prefs.poll_interval * 1000);
};


// set up infrastructure, start the refresh cycle
function init() {
    // load options
    prefs = loadPrefs();

    // add listener
    chrome.extension.onMessage.addListener(function(request, sender, sendResponse) {
        switch(request.msg) {
            case 'refreshList':
                console.debug('background.html: refreshList received, requerying list from RSS');
                poke()
                break;
            case 'reloadPrefs':
                console.debug('background.html: reloadPrefs received, reloading my prefences');
                prefs =loadPrefs();
                console.debug('background.html: sending refreshList');
                chrome.extension.sendMessage({msg: 'refreshList'});
        };
    });

    // start refresh cycle
    refresh();
};

// Hook init up.
document.addEventListener('DOMContentLoaded', init);
