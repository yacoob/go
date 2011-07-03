package org.yacoob.trampoline;

import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.yacoob.trampoline.DBHelper.DbHelperException;

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
 * {@link #attach(HopListActivity)}.
 */
class TaskRefreshList extends AsyncTask<String, Void, Boolean> {

    /** This variable is used to access current {@link Activity}. */
    private HopListActivity parentActivity = null;

    private final String url;
    private final String listName;
    private final DBHelper dbhelper;

    private Exception netProblems;

    /**
     * Constructor. Associates freshly created task with activity that created
     * it.
     * 
     * @param activity
     *            Activity launching this task.
     */
    TaskRefreshList(final HopListActivity activity, String listName, DBHelper dbhelper) {
        this.listName = listName;
        this.url = Hop.RESTURL + "/" + this.listName;
        this.dbhelper = dbhelper;
        attach(activity);
    }

    /**
     * Associate task to activity. This reference will be used in
     * {@link #onPostExecute(JSONObject)} to call
     * {@link HopListActivity#refreshDone(List)}.
     * 
     * @param activity
     *            Activity to attach to.
     */
    void attach(final HopListActivity activity) {
        this.parentActivity = activity;
    }

    /**
     * Disassociate task from activity.
     */
    void detach() {
        this.parentActivity = null;
    }

    @SuppressWarnings("unchecked")
    protected <T> Set<T> extractFromJson(JSONObject o, String arrayName) {
        if (o == null) {
            return null;
        } else {
            final HashSet<T> set = new HashSet<T>();
            try {
                if (arrayName != null) {
                    final JSONArray source = o.getJSONArray(arrayName);
                    for (int i=0; i<source.length(); i++) {
                        set.add((T) source.get(i));
                    }
                } else {
                    final Iterator<String> it = o.keys();
                    while (it.hasNext()) {
                        final String key = it.next();
                        set.add((T) o.get(key));
                    }
                }
            } catch (final ClassCastException e) {
                Hop.warn("Problems converting JSON: " + e.getMessage());
            } catch (final JSONException e) {
                Hop.warn("Problems parsing JSON: " + e.getMessage());
            }
            return set;
        }
    }
    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Boolean doInBackground(final String... params) {
        Boolean dataChanged = false;

        // Fetch all URLs newest than our latest item.
        try {
            String fetchUrl = null;
            if (dbhelper.getUrlCount(listName) == 0) {
                fetchUrl = url + "/" + URLEncoder.encode("*");
            } else {
                final String latestId = dbhelper.getNewestUrlId(listName);
                fetchUrl = url + "/" + URLEncoder.encode(">") + latestId;
            }

            JSONObject newUrlsJson = null;
            try {
                newUrlsJson = UrlFetch.urlToJSONObject(fetchUrl);
            } catch (final ClientProtocolException e) {
                // No new URLs
                return false;
            }

            // Extract JSONObjects from newUrlsJson, add all of them to db
            final Set<JSONObject> newUrls = extractFromJson(newUrlsJson, null);
            dataChanged |= dbhelper.insertJsonObjects(listName, newUrls);

            // Fetch list of all remote URL ids.
            final JSONObject remoteUrlsJson = UrlFetch.urlToJSONObject(url);
            final Set<String> remoteIds = extractFromJson(remoteUrlsJson, listName);
            final Set<String> localIds = dbhelper.getUrlIds(listName);

            // Drop local-remote from db.
            final Set<String> deletedIds = new HashSet<String>(localIds);
            deletedIds.removeAll(remoteIds);
            dataChanged |= dbhelper.removeIds(listName, deletedIds);

            // Iterate over remote-local, fetch one by one.
            final Set<String> newSingleIds = new HashSet<String>(remoteIds);
            newSingleIds.removeAll(localIds);
            final Iterator<String> it = newSingleIds.iterator();
            final Set<JSONObject> newObjects = new HashSet<JSONObject>();
            while (it.hasNext()) {
                final String id = it.next();
                final JSONObject newUrlData = UrlFetch.urlToJSONObject(url + "/" + id);
                if (newUrlData != null) {
                    newObjects.add(newUrlData);
                }
            }
            dataChanged |= dbhelper.insertJsonObjects(listName, newObjects);
        } catch (final ClientProtocolException e) {
            Hop.warn("Network problems while fetching data from Trampoline: " + e.getMessage());
            netProblems = e;
        } catch (final DbHelperException e) {
            Hop.warn("Database problems during refresh: " + e.getMessage());
            dataChanged = false;
        }
        return dataChanged;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(final Boolean result) {
        // In theory, this method should not be called if original activity is
        // dead. In practice, let's check this.
        if (parentActivity != null) {
            // "Cave Johnson - we're done here." :)
            parentActivity.refreshDone(result, netProblems);
            detach();
        } else {
            // Yes, This Should Not Happen [tm].
            Hop.warn("onPostExecute called without parent activity.");
        }
    }
}