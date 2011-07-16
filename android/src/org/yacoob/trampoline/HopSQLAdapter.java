package org.yacoob.trampoline;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

/**
 * SQLite-backed adapter for application's ListViews.
 */
final class HopSQLAdapter extends SimpleCursorAdapter {

    /** View to use for row construction. */
    static final int LISTITEM = R.layout.listitem;

    /**
     * Those db columns are used as data source. They're mapped 1:1 to views in
     * LISTITEM.
     */
    static final String[] FROM_COLUMNS = {
            "display_url", "date"
    };

    /** List of views in LISTITEM to map columns of db to. */
    static final int[] TO_VIEWS = {
            R.id.display_url, R.id.date
    };

    /**
     * Adapter constructor.
     * 
     * @param context
     *            Android context.
     * @param cursor
     *            Source of data.
     */
    public HopSQLAdapter(final Context context, final Cursor cursor) {
        super(context, LISTITEM, cursor, FROM_COLUMNS, TO_VIEWS);
    }
}
