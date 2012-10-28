// animation delays
// time for which the 'push' message is displayed (OK/NOK)
var message_delay = 2500;
// time for which button should be highlighted after click
var button_clicked_delay = 250;
// preferences object
var prefs = {};


// open a new tab with specified url, close the popup if requested
function __open_tab(url, close_window) {
    if (!url) {
        return undefined;
    };
    chrome.tabs.create({url: url});
    if (close_window) {
        window.close();
    };
};


// poke background service to check for new items
function __poke_background_service() {
    console.debug('popup.html: sending refreshList');
    chrome.extension.sendMessage({msg: 'refreshList'});
};


// display message in the popup, close it if requested
function display_message(msg, close_window) {
    var status = $('div#msg');
    status.html(msg).toggle('fast');
    setTimeout(function() {
        status.toggle('fast');
        if (close_window) {
            window.close();
        };
    }, message_delay);
};


// handle click on one of the buttons
function buttons_handler(e) {
    // have we actually clicked a trampolina button?
    if ($(e.target).is('.trampolina-button')) {

        // give visual feedback that button has been clicked
        $(e.target).addClass('trampolina-clicked');
        setTimeout(function() {
          $(e.target).removeClass('trampolina-clicked');
        }, button_clicked_delay);

        // check which button has been pressed
        switch(e.target.id) {
            case 'push':
                // construct url, then follow the link
                chrome.tabs.getSelected(null, function(tab) {
                    var page_url = tab.url;
                    var url = e.target.href + '?url=' + encodeURIComponent(page_url);
                    var xhr = new XMLHttpRequest();

                    xhr.open('GET', url, false);
                    try {
                        xhr.send();
                    } catch(e) {
                        // if base_url was broken, this may happen :)
                        xhr.status = 999;
                    };

                    if (xhr.status == 200) {
                        display_message('Push OK!', prefs.hide_push);
                        __poke_background_service();
                    } else {
                        display_message('Uh-oh... push failed.', false);
                    };
                });
                break;
            case 'pop':
                __open_tab(e.target.href, prefs.hide_pop);
                __poke_background_service();
                break;
            case 'popAll':
                $('div#list a').each(function(index) {
                    __open_tab(this.href, false);
                    });
                __poke_background_service();
                break;
            case 'list':
                __open_tab(e.target.href, true);
                break;
        };

        // inhibit the default click action
        e.preventDefault();
        return false;
    };
};


// handle clicks on one of the URLs below
function urls_handler(e) {
    if ($(e.target).is('a')) {
        // open a new tab with clicked URL
        __open_tab(e.target.href, prefs.hide_list);

        // poke background task to refresh the list
        __poke_background_service();

        // inhibit the default click action
        e.preventDefault();
        return false;
    };
};


// update popup URL list
function updatePopupList(data) {
    var html = '';
    for (var prop in data) {
      var u = data[prop];
      html += '<a href="' + u['pop_url'] + '">' + u['description'] + '</a><br>';
    }
    $('div#list').html(html);
}


// load options, setup handlers, load list of URLs
function init() {
    $(document).ready(function() {
        // load options
        prefs = loadPrefs();

        // setup hrefs of the buttons
        var urls = ['push', 'pop', 'popAll', 'list'];
        for (var i = 0; i < urls.length; i += 1) {
            var element = 'a#' + urls[i] + '.trampolina-button';
            $(element)[0].href = prefs.base_url + '/' + urls[i];
        };

        // set click handlers
        $('div#trampolina-buttons').click(buttons_handler);
        $('div#list').click(urls_handler);

        // add listener
        chrome.extension.onMessage.addListener(function(request, sender, sendResponse) {
            switch(request.msg) {
                case 'updateList':
                    console.debug('popup.html: got updateList, received a new URL list');
                    updatePopupList(request.chunk);
                    break;
                case 'reloadPrefs':
                    console.debug('popup.html: got reloadPrefs, reloading my preferences');
                    prefs = loadPrefs();
                    break;
            };
        });

        // get the current list of URLs
        __poke_background_service();
    });
};

// Hook init up.
document.addEventListener('DOMContentLoaded', init);
