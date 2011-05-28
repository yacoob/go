package org.yacoob;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UrlList extends ListActivity {
    private static String list_url = "http://go/hop/list?json=1";
    private String[] url_list;

    private static class UrlFetch {
        private static DefaultHttpClient f = new DefaultHttpClient();

        private static String fetchUrl(String url) {
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

    private class RefreshList extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return UrlFetch.fetchUrl(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result, 30000).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        url_list = getResources().getStringArray(R.array.url_list);
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, url_list));
        new RefreshList().execute(list_url);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String url = url_list[position];
        Toast.makeText(this, url, 1500).show();
        this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
