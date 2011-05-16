package org.yacoob;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class UrlList extends ListActivity {
    private String[] url_list;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        url_list = getResources().getStringArray(R.array.url_list);
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, url_list));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String url = url_list[position];
        Toast.makeText(this, url, 1500).show();
        this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
