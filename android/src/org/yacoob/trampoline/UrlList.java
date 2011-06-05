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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class UrlList extends ListActivity {
    public static final String LOGTAG = "Trampoline";
    private static final String base_url = "http://192.168.1.34:8080/hop";
    private static final String list_url = base_url + "/list?json=1";
    private static final String filename = "url_cache";
    private boolean is_offline = false;

    private class TaskRefreshList extends AsyncTask<String, Void, JSONObject> {
        private final String[] lists = {
            "stack"/* , "viewed" */
        };

        @Override
        protected JSONObject doInBackground(String... params) {
            return UrlFetch.urlToJSONObject(params[0]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (result != null) {
                for (String name : lists) {
                    try {
                        List<UrlEntry> new_url_list = new ArrayList<UrlEntry>();
                        JSONArray list = result.optJSONArray(name);
                        for (int i = 0; i < list.length(); i++) {
                            new_url_list.add(new UrlEntry(
                                    list.getJSONObject(i),
                                    name == "stack" ? base_url : null));
                        }
                        setUrlList(new_url_list);
                    } catch (JSONException e) {
                        warn("Problems parsing JSON response: "
                                + e.getMessage());
                        showComplaint(e.getMessage());
                    }
                }
                setOnline();
            } else {
                setOffline();
                showComplaint(getString(R.string.fetch_failed));
            }
        }
    }

    private class HopListAdapter extends ArrayAdapter<UrlEntry> {
        private LayoutInflater li;

        private HopListAdapter(Context context, List<UrlEntry> objects) {
            super(context, R.layout.listitem, objects);
            li = LayoutInflater.from(context);
        }

        private class ViewHolder {
            TextView first;
            TextView second;
        }

        public List<UrlEntry> getUrlList() {
            List<UrlEntry> list = new ArrayList<UrlEntry>();
            for (int i = 0; i < getCount(); i++) {
                list.add(getItem(i));
            }
            return list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;

            if (convertView == null) {
                convertView = li.inflate(R.layout.listitem, parent, false);
                holder = new ViewHolder();
                holder.first = (TextView) convertView.findViewById(R.id.first);
                holder.second = (TextView) convertView
                        .findViewById(R.id.second);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            UrlEntry item = getItem(position);
            holder.first.setText(item.getDisplayUrl());
            holder.second.setText(item.getDate());
            return convertView;
        }
    }

    private void refreshUrlList() {
        new TaskRefreshList().execute(list_url);
    }

    public void setOnline() {
        setTitle(R.string.app_name);
        is_offline = false;
        debug("I'm online now!");
    }

    public void setOffline() {
        // TODO: Should we also serialize lists here, sort of
        // "last known piece of data"?
        setTitle(getString(R.string.app_name) + " "
                + getString(R.string.offline_indicator));
        is_offline = true;
        debug("I'm offline now!");
    }

    private void setUrlList(List<UrlEntry> l) {
        /*
         * XXX: ArrayAdapter<T>.addAll got added in r11. Without that method
         * we'd need to iterate through new_url_list, and call add() one by one.
         * Unsmurfy.
         */
        setListAdapter(new HopListAdapter(this, l));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        debug("jestem w onCreate!");
        setContentView(R.layout.main);
        try {
            ObjectInputStream ois = new ObjectInputStream(
                    openFileInput(filename));
            setUrlList((List<UrlEntry>) ois.readObject());
            ois.close();
        } catch (Exception e) {
            warn("Problems during deserialization of cache: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        debug("jestem w onResume!");
        // TODO: add alarm to do this refresh on periodical basis
        refreshUrlList();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // FIXME: Is this the right place to save this to disk? As it stands,
        // it'll be called every time user pops an URL. Perhaps onDestroy is
        // better place for this.
        try {
            ObjectOutputStream oos = new ObjectOutputStream(openFileOutput(
                    filename, Context.MODE_PRIVATE));
            oos.writeObject(((HopListAdapter) getListAdapter()).getUrlList());
            oos.close();
        } catch (IOException e) {
            warn("Problems during serialization of cache: " + e.getMessage());
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        UrlEntry item = (UrlEntry) l.getItemAtPosition(position);
        String url = is_offline == true ? item.getDisplayUrl() : item.getUrl();
        showInfo(url);
        this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.main, menu);
        return true;
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

    private void showToast(String msg, int time) {
        Toast.makeText(getApplicationContext(), msg, time).show();
    }

    private void showComplaint(String complaint) {
        showToast(complaint, 3000);
    }

    private void showInfo(String info) {
        showToast(info, 1500);
    }

    public static void warn(String msg) {
        Log.w(LOGTAG, msg);
    }

    public static void debug(String msg) {
        Log.d(LOGTAG, msg);
    }
}