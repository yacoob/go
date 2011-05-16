package org.yacoob;

import android.app.ListActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class UrlList extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                                                getResources().getStringArray(R.array.url_list)));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Toast.makeText(this, ((TextView) v).getText(), 1500).show();
    }
}
