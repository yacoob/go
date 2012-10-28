function load_options() {
    // load preferences
    var prefs = loadPrefs();

    // set form controls accordingly
    $('input#base-url').val(prefs.base_url);
    $('input#poll-interval').val(prefs.poll_interval);

    // load "hide preferences"
    for (var i = 0; i < __hide_prefs.length; i += 1) {
        var n = __hide_prefs[i];
        var checkbox = $('input#' + n);

        // tick checkboxes
        if (prefs[n]) {
            checkbox.attr('checked', 'checked');
        } else {
            checkbox.removeAttr('checked');
        };
    };
};

function save_options() {
    // start with an empty object
    var prefs = {};

    prefs.base_url = $('input#base-url').val();
    prefs.poll_interval = $('input#poll-interval').val();

    // save "hide preferences"
    for (var i = 0; i < __hide_prefs.length; i += 1) {
        var n = __hide_prefs[i];
        prefs[n] = $('input#' + n).attr('checked');
    };
    savePrefs(prefs);

    // inform user that his changes have been saved
    var status = $('div#status');
    status.html('Settings have been saved.').toggle('fast');
    setTimeout(function() {
        status.toggle('fast');
    }, 3000);

    // inform everyone that we've just possibly saved prefs
    chrome.extension.sendRequest({msg: 'reloadPrefs'});
};

function init() {
    load_options();
    document.getElementById('submit').addEventListener('click', save_options);
}

// Hook init up.
document.addEventListener('DOMContentLoaded', init);
