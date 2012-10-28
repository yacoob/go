function load_options() {
    // load preferences
    var prefs = loadPrefs();

    // set form controls accordingly
    $('input#base-url').val(prefs.base_url);
    $('input#poll-interval').val(prefs.poll_interval);
};

function save_options() {
    // start with an empty object
    var prefs = {};

    prefs.base_url = $('input#base-url').val();
    prefs.poll_interval = $('input#poll-interval').val();
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
