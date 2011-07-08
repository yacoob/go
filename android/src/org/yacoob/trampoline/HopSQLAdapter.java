package org.yacoob.trampoline;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

class HopSQLAdapter extends SimpleCursorAdapter {
    static final int list_item = R.layout.listitem;
    static final int[] to = { R.id.display_url, R.id.date };
    static final String[] from = {"display_url", "date"};

    public HopSQLAdapter(Context context, Cursor c) {
        super(context, list_item, c, from, to);
    }
}
