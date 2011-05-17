package org.yacoob;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
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
import java.io.InputStream;
import java.io.InputStreamReader;

public class UrlList extends ListActivity {
    private static String list_url = "http://go/hop/list?json=1";
    private String[] url_list;
    private DefaultHttpClient f = new DefaultHttpClient();
    private HttpGet r = new HttpGet(list_url);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        url_list = getResources().getStringArray(R.array.url_list);
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, url_list));
        // TODO:Use AsyncTask for this and future updates
        refreshListFromUrl();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String url = url_list[position];
        Toast.makeText(this, url, 1500).show();
        this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void refreshListFromUrl() {
        try {
            HttpResponse l = f.execute(r);
            InputStream is = l.getEntity().getContent();
            // TODO: actually use fetched data
            Toast.makeText(this, convertStreamToString(is), 30000).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String convertStreamToString(InputStream is) {
        // TODO: replace with private class converting directly to String[]
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
