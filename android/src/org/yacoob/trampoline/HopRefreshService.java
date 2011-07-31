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

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * This IntentService downloads new data from Trampoline server and updates local database with it.
 * 
 */
public final class HopRefreshService extends IntentService {

    /** Intent filter for catching new data event. */
    protected static final IntentFilter REFRESH_FILTER = new IntentFilter(
            HopRefreshService.NEWDATA_ACTION);

    /** Names of actions that this service accepts and sends out. */
    /** Automatic (scheduled) refresh action. */
    protected static final String REFRESH_ACTION = "org.yacoob.refreshData";
    /** Manual refresh action. It will always report back, even if there was no new data. */
    protected static final String REFRESH_MANUAL = "org.yacoob.refreshDataManual";
    /** Broadcast action notifying about availability of new data. */
    protected static final String NEWDATA_ACTION = "org.yacoob.newDataForList";

    /** Application object. */
    private Hop app;

    /** Database helper. */
    private DBHelper dbhelper;

    /** Base URL for REST interface. */
    private String url;

    /** User preferences. */
    private SharedPreferences prefs;

    /** Constructor. */
    public HopRefreshService() {
        super("HopRefreshService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = (Hop) getApplication();
        dbhelper = app.getDbHelper();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        url = prefs.getString("baseUrl", "") + "/r/";
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (app.onHomeNetwork()) {
            final String action = intent.getAction();
            if (action.equals(REFRESH_ACTION)) {
                checkForNewData(false);
            } else if (action.equals(REFRESH_MANUAL)) {
                checkForNewData(true);
            }
        }
    }

    /**
     * Queries server, updates local database with changed data. Broadcasts notification if there's
     * been any change.
     * 
     * @param reportNoData
     *            If true, this function will always send a broadcast with result of the refresh.
     */
    private void checkForNewData(final Boolean reportNoData) {
        Boolean gotNewData = false;
        Boolean networkProblems = false;
        try {
            gotNewData |= getNewDataForList("stack");
        } catch (final IOException e) {
            networkProblems = true;
        }
        if (gotNewData || reportNoData) {
            final Intent intent = new Intent();
            intent.setAction(NEWDATA_ACTION);
            intent.putExtra("listName", "stack");
            intent.putExtra("gotNewData", gotNewData);
            intent.putExtra("networkProblems", networkProblems);
            sendBroadcast(intent);
        }
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
            final Set<T> set = new HashSet<T>();
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

    /**
     * Queries Trampoline server about specific list.
     * 
     * @param listName
     *            Name of the list to query.
     * @return true if any data has been changed (added/removed), false otherwise.
     * @throws IOException
     *             on network problems.
     */
    Boolean getNewDataForList(final String listName) throws IOException {
        Boolean dataChanged = false;
        final String listUrl = url + listName;

        // Check for URLs newer than our latest item.
        try {
            String fetchUrl = null;
            if (dbhelper.getUrlCount(listName) == 0) {
                fetchUrl = listUrl + "/" + URLEncoder.encode("*");
            } else {
                final String latestId = dbhelper.getNewestUrlId(listName);
                fetchUrl = listUrl + "/" + URLEncoder.encode(">") + latestId;
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
            final JSONObject remoteUrlsJson = UrlFetch.urlToJSONObject(listUrl);
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
                    final JSONObject newUrlData = UrlFetch.urlToJSONObject(listUrl + "/" + id);
                    if (newUrlData != null) {
                        newObjects.add(newUrlData);
                    }
                }
                dataChanged |= dbhelper.insertJsonObjects(listName, newObjects);
            }
        } catch (final DbHelperException e) {
            Hop.warn("Database problems during refresh: " + e.getMessage());
            dataChanged = false;
            return dataChanged;
        }
        return dataChanged;
    }
}
