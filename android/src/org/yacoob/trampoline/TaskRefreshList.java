package org.yacoob.trampoline;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;

/**
 * {@link AsyncTask} implementing URL list refresh. It's slightly smarter than
 * average {@link AsyncTask} - it uses {@link #parentActivity} to track which
 * activity it should report to. This is to account for configuration changes
 * (like screen rotation) causing activity that created this task to die while
 * {@link AsyncTask} was running. @see <a href="http://bit.ly/lWbHjZ">More
 * details</a>.
 * 
 * In short: {@link Activity} that created this task should call
 * {@link #detach()} and use some mechanism to carry over reference to running
 * {@link AsyncTask}. New instance of same activity should call
 * {@link #attach(UrlList)}.
 */
class TaskRefreshList extends AsyncTask<String, Void, JSONObject> {

    /** This variable is used to access current {@link Activity}. */
    private UrlList parentActivity = null;

    /** Contains names of lists of URLs to expect from Trampoline server. */
    private final String[] lists = {
        "stack"/* , "viewed" */
    };

    /**
     * Constructor. Associates freshly created task with activity that created
     * it.
     * 
     * @param activity
     *            Activity launching this task.
     */
    TaskRefreshList(final UrlList activity) {
        attach(activity);
    }

    /**
     * Associate task to activity. This reference will be used in
     * {@link #onPostExecute(JSONObject)} to call
     * {@link UrlList#refreshDone(List)}.
     * 
     * @param activity
     *            Activity to attach to.
     */
    void attach(final UrlList activity) {
        this.parentActivity = activity;
    }

    /**
     * Disassociate task from activity.
     */
    void detach() {
        this.parentActivity = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected JSONObject doInBackground(final String... params) {
        return UrlFetch.urlToJSONObject(params[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(final JSONObject result) {
        // In theory, this method should not be called if original activity is
        // dead. In practice, let's check this.
        if (parentActivity != null) {
            List<UrlEntry> newUrlList = null;
            if (result != null) {
                for (String name : lists) {
                    try {
                        newUrlList = new ArrayList<UrlEntry>();
                        JSONArray list = result.optJSONArray(name);
                        for (int i = 0; i < list.length(); i++) {
                            // If the list we're creating is the stack, point
                            // actual URL to Trampoline.
                            newUrlList.add(new UrlEntry(list.getJSONObject(i),
                                    name == "stack" ? UrlList.base_url : null));
                        }
                    } catch (JSONException e) {
                        UrlList.warn("Problems parsing JSON response: "
                                + e.getMessage());
                    }
                }
            }
            // "Cave Johnson - we're done here." :)
            parentActivity.refreshDone(newUrlList);
            parentActivity = null;
        } else {
            // Yes, This Should Not Happen [tm].
            UrlList.warn("onPostExecute called without parent activity.");
        }
    }
}