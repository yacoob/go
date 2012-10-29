function loadOptions() {
  loadPrefsAndRun(function(prefs) {
    $('input#base-url').val(prefs.base_url);
    $('input#poll-interval').val(prefs.poll_interval);
    $('#submit').click(saveOptions);
  });
};


function saveOptions() {
  savePrefs({
    base_url: $('input#base-url').val(),
    poll_interval: parseInt($('input#poll-interval').val()) || 0,
  });

  // Inform user that his changes have been saved.
  var status = $('div#status');
  status.html('Settings have been saved.').toggle('fast');
  setTimeout(function() {
    status.toggle('fast');
  }, 3000);
};


// Load options on page load.
$(loadOptions);
