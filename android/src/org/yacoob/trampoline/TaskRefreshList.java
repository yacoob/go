package org.yacoob.trampoline;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.yacoob.trampoline.DBHelper.DbHelperException;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * AsyncTask implementing URL list refresh.
 */
final class TaskRefreshList extends DetachableTask<String, Void, Boolean, HopListActivity> {

    /** Base URL for Trampoline server. */
    private final String url;

    /** List to update. */
    private final String listName;

    /** Database helper. */
    private final DBHelper dbhelper;

    /** Stores any exception that might have happened during the refresh. */
    private Exception netProblems;

    /**
     * Constructor. Associates freshly created task with activity that created it.
     * 
     * @param activity
     *            Activity launching this task.
     * @param list
     *            Name of the URL list to update.
     * @param databaseHelper
     *            Reference to database helper.
     */
    TaskRefreshList(final HopListActivity activity, final String list, //
            final DBHelper databaseHelper) {
        listName = list;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        final String baseUrl = prefs.getString("baseUrl", Hop.BASEURL);
        url = baseUrl + "/r/" + listName;
        dbhelper = databaseHelper;
        attach(activity);
    }

    /**
     * Extracts useful information from an JSONObject. By default it'll extract all values of an
     * JSON object an return it as a Set. If you specify arrayName, it'll extract all values of
     * named array contained in provided JSON object. Tested only for T in {JSONObject, String} :)
     * 
     * @param object
     *            {@link JSONObject} to extract data from.
     * @param <T>
     *            Type of things to extract from JSON object.
     * @param arrayName
     *            If present, constraints extraction to an JSON array present in object under this
     *            key.
     * @return Set of extracted objects (of type T).
     */
    @SuppressWarnings("unchecked")
    protected <T> Set<T> extractFromJson(final JSONObject object, final String arrayName) {
        if (object == null) {
            return null;
        } else {
            final HashSet<T> set = new HashSet<T>();
            try {
                if (arrayName != null) {
                    final JSONArray source = object.getJSONArray(arrayName);
                    for (int i = 0; i < source.length(); i++) {
                        set.add((T) source.get(i));
                    }
                } else {
                    final Iterator<String> it = object.keys();
                    while (it.hasNext()) {
                        final String key = it.next();
                        set.add((T) object.get(key));
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

        // Check for URLs newer than our latest item.
        try {
            String fetchUrl = null;
            if (dbhelper.getUrlCount(listName) == 0) {
                fetchUrl = url + "/" + URLEncoder.encode("*");
            } else {
                final String latestId = dbhelper.getNewestUrlId(listName);
                fetchUrl = url + "/" + URLEncoder.encode(">") + latestId;
            }

            JSONObject newUrlsJson = null;
            Boolean newUrlsPresent = true;
            try {
                newUrlsJson = UrlFetch.urlToJSONObject(fetchUrl);
            } catch (final HttpResponseException e) {
                // No new URLs?
                if (e.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    newUrlsPresent = false;
                } else {
                    throw (e);
                }
            }

            if (newUrlsPresent) {
                // Extract JSONObjects from newUrlsJson, add all of them to db
                final Set<JSONObject> newUrls = extractFromJson(newUrlsJson, null);
                dataChanged |= dbhelper.insertJsonObjects(listName, newUrls);
            }

            // Fetch list of all remote URL ids, compare to local one. This will
            // account for any holes Android client might have. Usually this
            // doesn't happen, but we might end up in half-updated state after a
            // crash.
            final JSONObject remoteUrlsJson = UrlFetch.urlToJSONObject(url);
            final Set<String> remoteIds = extractFromJson(remoteUrlsJson, listName);
            final Set<String> localIds = dbhelper.getUrlIds(listName);

            // Drop local-remote from db.
            final Set<String> deletedIds = new HashSet<String>(localIds);
            deletedIds.removeAll(remoteIds);
            if (deletedIds.size() != 0) {
                dataChanged |= dbhelper.removeIds(listName, deletedIds);
            }

            // Iterate over remote-local, fetch one by one.
            final Set<String> newSingleIds = new HashSet<String>(remoteIds);
            newSingleIds.removeAll(localIds);
            if (newSingleIds.size() != 0) {
                final Set<JSONObject> newObjects = new HashSet<JSONObject>();
                for (final String id : newSingleIds) {
                    final JSONObject newUrlData = UrlFetch.urlToJSONObject(url + "/" + id);
                    if (newUrlData != null) {
                        newObjects.add(newUrlData);
                    }
                }
                dataChanged |= dbhelper.insertJsonObjects(listName, newObjects);
            }
        } catch (final IOException e) {
            netProblems = e;
            return dataChanged;
        } catch (final DbHelperException e) {
            Hop.warn("Database problems during refresh: " + e.getMessage());
            dataChanged = false;
            return dataChanged;
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
        final HopListActivity parent = getParentActivity();
        if (parent != null) {
            // "Cave Johnson - we're done here." :)
            parent.refreshDone(result, netProblems);
            detach();
        } else {
            // Yes, This Should Not Happen [tm].
            Hop.warn("onPostExecute called without parent activity.");
        }
    }
}
