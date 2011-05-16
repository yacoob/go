package org.yacoob;

import android.app.ListActivity;
import android.widget.ArrayAdapter;
import android.os.Bundle;

public class UrlList extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                                                getResources().getStringArray(R.array.url_list)));
    }
}
