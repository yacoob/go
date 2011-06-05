package org.yacoob.trampoline;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class UrlList extends ListActivity {
    private class TaskRefreshList extends AsyncTask<String, Void, JSONObject> {
        private UrlList parentActivity = null;
        private final String[] lists = {
            "stack"/* , "viewed" */
        };

        protected TaskRefreshList(UrlList activity) {
            attach(activity);
        }

        private void attach(UrlList activity) {
            this.parentActivity = activity;
        }

        private void detach() {
            this.parentActivity = null;
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            return UrlFetch.urlToJSONObject(params[0]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (parentActivity != null) {
                List<UrlEntry> new_url_list = null;
                if (result != null) {
                    for (String name : lists) {
                        try {
                            new_url_list = new ArrayList<UrlEntry>();
                            JSONArray list = result.optJSONArray(name);
                            for (int i = 0; i < list.length(); i++) {
                                new_url_list.add(new UrlEntry(list
                                        .getJSONObject(i),
                                        name == "stack" ? base_url : null));
                            }
                        } catch (JSONException e) {
                            warn("Problems parsing JSON response: "
                                    + e.getMessage());
                            parentActivity.showComplaint(e.getMessage());
                        }
                    }
                }
                parentActivity.refreshDone(new_url_list);
                parentActivity = null;
            } else {
                warn("Uh. onPostExecute got called without parent activity. That's wrong.");
            }
        }
    }

    private static final String base_url = "http://192.168.1.34:8080/hop";
    private static final String filename = "url_cache";
    private static final String list_url = base_url + "/list?json=1";
    public static final String LOGTAG = "Trampoline";
    private TaskRefreshList refresh_task;

    public static void debug(String msg) {
        Log.d(LOGTAG, msg);
    }

    public static void warn(String msg) {
        Log.w(LOGTAG, msg);
    }

    private boolean is_offline = false;

    private void maybeSerializeCache() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(openFileOutput(
                    filename, Context.MODE_PRIVATE));
            oos.writeObject(((HopListAdapter) getListAdapter()).getUrlList());
            oos.close();
        } catch (IOException e) {
            warn("Problems during serialization of cache: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        try {
            ObjectInputStream ois = new ObjectInputStream(
                    openFileInput(filename));
            setUrlList((List<UrlEntry>) ois.readObject());
            ois.close();
        } catch (Exception e) {
            warn("Problems during deserialization of cache: " + e.getMessage());
        }
        // Recover background task, if there's one.
        refresh_task = (TaskRefreshList) getLastNonConfigurationInstance();
        if (refresh_task != null) {
            refresh_task.attach(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        UrlEntry item = (UrlEntry) l.getItemAtPosition(position);
        String url = is_offline == true ? item.getDisplayUrl() : item.getUrl();
        showInfo(url);
        this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refresh:
            refreshUrlList();
            return true;
        case R.id.exit:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: add alarm to do this refresh on periodical basis
        refreshUrlList();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        if (refresh_task != null) {
            refresh_task.detach();
        }
        return refresh_task;
    }

    @Override
    protected void onStop() {
        super.onStop();
        // FIXME: Is this the right place to save this to disk? As it stands,
        // it'll be called every time user pops an URL. Perhaps onDestroy is
        // better place for this.
        maybeSerializeCache();
    }

    private void refreshUrlList() {
        if (refresh_task == null) {
            refresh_task = new TaskRefreshList(this);
            refresh_task.execute(list_url);
        }
    }

    private void refreshDone(List<UrlEntry> list) {
        if (list != null) {
            setOnline();
            setUrlList(list);
        } else {
            setOffline();
            showComplaint(getString(R.string.fetch_failed));
        }
        refresh_task = null;
    }

    public void setOffline() {
        setTitle(getString(R.string.app_name) + " "
                + getString(R.string.offline_indicator));
        is_offline = true;
        maybeSerializeCache();
        debug("I'm offline now!");
    }

    public void setOnline() {
        setTitle(R.string.app_name);
        is_offline = false;
        debug("I'm online now!");
    }

    private void setUrlList(List<UrlEntry> l) {
        /*
         * XXX: ArrayAdapter<T>.addAll got added in r11. Without that method
         * we'd need to iterate through new_url_list, and call add() one by one.
         * Unsmurfy.
         */
        setListAdapter(new HopListAdapter(this, l));
    }

    private void showComplaint(String complaint) {
        showToast(complaint, 3000);
    }

    private void showInfo(String info) {
        showToast(info, 1500);
    }

    private void showToast(String msg, int time) {
        Toast.makeText(getApplicationContext(), msg, time).show();
    }
}