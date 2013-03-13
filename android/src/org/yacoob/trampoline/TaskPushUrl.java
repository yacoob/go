package org.yacoob.trampoline;

import java.io.IOException;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;

/**
 * AsyncTask implementing URL pushing.
 */
final class TaskPushUrl extends DetachableTask<String, Integer, Boolean, HopPushActivity> {

    /** Value to use to set ProgressDialog to 100%. */
    private static final int PROGRESSDONE = 10000;

    /** URL of push interface of Trampoline. */
    private final String pushUrl;

    /** Informative dialog this task is using. */
    private ProgressDialog dialog;

    /** How long should a finished push announcement remain on the screen? */
    private final int ackDelay = 2000;

    /**
     * Constructor.
     * 
     * @param activity
     *            Activity launching this task.
     * @param serverUrl
     *            URL of push interface of Trampoline.
     */
    public TaskPushUrl(final HopPushActivity activity, final String serverUrl) {
        super();
        attach(activity);
        pushUrl = serverUrl;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        final Activity parent = getParentActivity();
        if (parent != null) {
            dialog = new ProgressDialog(parent);
            dialog.setCancelable(false);
            dialog.setMessage(parent.getString(R.string.push_msg_active));
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
    }

    @Override
    protected Boolean doInBackground(final String... params) {
        publishProgress(0);

        // Work out the URL to use.
        final String fetchUrl = pushUrl + URLEncoder.encode(params[0]);

        // Actually push the URL.
        Boolean pushOk = true;
        try {
            UrlFetch.urlToString(fetchUrl);
        } catch (final IOException e) {
            pushOk = false;
        }
        publishProgress(PROGRESSDONE);
        return pushOk;
    }

    @Override
    void detach() {
        if (dialog != null) {
            dialog.dismiss();
        }
        super.detach();
    }

    @Override
    protected void onProgressUpdate(final Integer... values) {
        super.onProgressUpdate(values);
        final Integer progress = values[0];
        if (progress == 0 && dialog != null) {
            dialog.show();
        }
        dialog.setProgress(progress);
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        super.onPostExecute(result);
        final HopPushActivity parent = getParentActivity();
        if (parent != null && dialog != null) {
            if (result) {
                dialog.setMessage(parent.getString(R.string.push_msg_done));
            } else {
                dialog.setMessage(parent.getString(R.string.push_msg_failed));
            }

            // Hide the dialog and finish activity after some delay.
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.cancel();
                    parent.finish();
                }
            }, ackDelay);
        }
    }
}
