package org.yacoob.trampoline;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

final class HopSQLAdapter extends SimpleCursorAdapter {
    static final int LISTITEM = R.layout.listitem;
    static final int[] TO_VIEWS = {
            R.id.display_url, R.id.date
    };
    static final String[] FROM_COLUMNS = {
            "display_url", "date"
    };

    public HopSQLAdapter(final Context context, final Cursor c) {
        super(context, LISTITEM, c, FROM_COLUMNS, TO_VIEWS);
    }
}
