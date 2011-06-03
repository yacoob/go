package org.yacoob.trampoline;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class UrlList extends ListActivity {
    // TODO: review scopes, I might have gone wild with 'private' 8)
    private static final String list_url = "http://192.168.1.34:8080/hop/list?json=1";
    private Context ctx;

    private static class UrlFetch {
        private static final DefaultHttpClient f = new DefaultHttpClient();

        private static JSONObject fetchUrl(String url) {
            JSONObject parsed = null;
            try {
                parsed = (JSONObject) new JSONTokener(urlToString(url)).nextValue();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return parsed;
        }

        private static String urlToString(String url) {
            BufferedReader reader = null;
            StringBuilder sb = new StringBuilder();
            HttpGet r = new HttpGet(url);
            try {
                HttpResponse l = f.execute(r);
                reader = new BufferedReader(new InputStreamReader(l.getEntity().getContent()));

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }
    }

    private class TaskRefreshList extends AsyncTask<String, Void, JSONObject> {
        private final String[] lists = {"stack"/*, "viewed"*/};

        @Override
        protected JSONObject doInBackground(String... params) {
            return UrlFetch.fetchUrl(params[0]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (result != null) {
                for (String name : lists) {
                    try {
                        List<UrlEntry> new_url_list = new ArrayList<UrlEntry>();
                        JSONArray list = result.optJSONArray(name);
                        for (int i = 0; i < list.length(); i++) {
                            new_url_list.add(new UrlEntry(list.getJSONObject(i)));
                        }
						/* FIXME: ArrayAdapter<T>.addAll got added in r11.
						 * Without that method we'd need to iterate through
						 * new_url_list, and call add() one by one. Unsmurfy.
						 */
                        setListAdapter(new HopListAdapter(ctx, new_url_list));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showComplaint(e.getMessage());
                    }
                }
            } else {
                showComplaint("No URLs to show");
            }
        }
    }

    private class HopListAdapter extends ArrayAdapter<UrlEntry> {
        private final LayoutInflater li = getLayoutInflater();

        private HopListAdapter(Context context, List<UrlEntry> objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                // TODO: Use fancy view instead of simple_list_item_1, use ViewHolder
                convertView = li.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            String content = getItem(position).toString();            
            ((TextView) convertView).setText(content);
            return convertView;
        }
    }

    private void refreshUrlList() {
        new TaskRefreshList().execute(list_url);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ctx = this;
        // TODO: add alarm to do this refresh on periodical basis
        // TODO: cache those results locally
        refreshUrlList();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String url = ((UrlEntry) l.getItemAtPosition(position)).getUrl();
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showToast(String msg, int time) {
        Toast.makeText(getApplicationContext(), msg, time).show();
    }

    void showComplaint(String complaint) {
        showToast(complaint, 3000);
    }

    void showInfo(String info) {
        showToast(info, 1500);
    }
}