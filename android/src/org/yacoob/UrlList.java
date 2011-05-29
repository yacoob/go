package org.yacoob;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private class RefreshList extends AsyncTask<String, Void, JSONObject> {
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
                        List<JSONObject> new_url_list = new ArrayList<JSONObject>();
                        JSONArray list = result.optJSONArray(name);
                        for (int i = 0; i < list.length(); i++) {
                            // TODO: consider using separate class for this data instead of JSONObject
                            new_url_list.add(list.getJSONObject(i));
                        }
                        // TODO: just change the list instead of recreating whole adapter
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

    private class HopListAdapter extends ArrayAdapter<JSONObject> {
        private final LayoutInflater li = getLayoutInflater();

        private HopListAdapter(Context context, List<JSONObject> objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                // TODO: Use fancy view instead of simple_list_item_1, use ViewHolder
                convertView = li.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            String content;
            try {
                content = getItem(position).getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
                // TODO: yeah, right; take care of things like that during JSON parsing
                content = "<MALFORMED>";
            }
            ((TextView) convertView).setText(content);
            return convertView;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // TODO: check if this is safe/sane
        ctx = this;
        // TODO: add a refresh button, and alarm to do this refresh on periodical basis
        // TODO: cache those results locally
        new RefreshList().execute(list_url);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String url = ((TextView) v).getText().toString();
        showInfo(url);
        this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
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