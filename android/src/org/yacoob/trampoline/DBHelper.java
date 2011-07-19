package org.yacoob.trampoline;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Database helper class.
 */
final class DBHelper extends SQLiteOpenHelper {

    /** Name of the sqlite file to keep data in. */
    private static final String DB_NAME = "hop.db";

    /** Database version used for db migration during updates. */
    private static final int DB_VERSION = 1;

    /** Name of the table containing Trampoline stack of urls.. */
    private static final String STACK = "stack";

    /** Actual database object. */
    private SQLiteDatabase db;

    /**
     * This exception is thrown upon application-specific problems with the database.
     */
    static class DbHelperException extends Exception {

        /** For serialization. */
        private static final long serialVersionUID = 945002551113629770L;

        /**
         * Dummy constructor.
         * 
         * @param msg
         *            Informative message describing the exception.
         */
        public DbHelperException(final String msg) {
            super(msg);
        }
    }

    /**
     * Constructor for {@link DBHelper}.
     * 
     * @param context
     *            Android context.
     */
    public DBHelper(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(final SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS " + STACK + " (" + BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "url_id VARCHAR, display_url VARCHAR, pop_url VARCHAR, date VARCHAR)");
    }

    @Override
    public void onUpgrade(final SQLiteDatabase database, final int oldVersion, final int newVersion) {

    }

    /**
     * Gets the actual table name. This allows decoupling actual names of tables in sqlite db from
     * the names used by other classes to access the data.
     * 
     * @param handle
     *            Common name of the requested table.
     * @return The actual SQL table name
     * @throws DbHelperException
     *             When it's unable to find requested table.
     */
    public static String getTableName(final String handle) throws DbHelperException {
        if (handle == "stack") {
            return STACK;
        } else {
            throw new DbHelperException("There's no table assigned to: " + handle);
        }
    }

    /**
     * Returns a cursor for specific table.
     * 
     * @param table
     *            handle for the table we want to get cursor to.
     * @return the cursor for table.
     * @throws DbHelperException
     *             when it's unable to find requested table.
     */
    public SQLiteCursor getCursorForTable(final String table) throws DbHelperException {
        final String tableName = getTableName(table);
        final SQLiteCursor cursor = (SQLiteCursor) db.query(tableName, null, null, null, null,
                null, "url_id DESC");
        return cursor;
    }

    /**
     * Find the id of newest entry in given table.
     * 
     * @param handle
     *            Handle for the table to query.
     * @return URL id.
     * @throws DbHelperException
     *             When it's unable to find requested table.
     */
    public String getNewestUrlId(final String handle) throws DbHelperException {
        final SQLiteCursor cursor = getCursorForTable(handle);
        final int columnId = cursor.getColumnIndex("url_id");
        cursor.moveToFirst();
        String newestUrlId = cursor.getString(columnId);
        cursor.close();
        return newestUrlId;
    }

    /**
     * Returns the url count for given table.
     * 
     * @param handle
     *            Handle for the table to query.
     * @return Number of URLs in given table.
     * @throws DbHelperException
     *             When it's unable to find requested table.
     */
    public int getUrlCount(final String handle) throws DbHelperException {
        final SQLiteCursor cursor = getCursorForTable(handle);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * Returns a Set of all URL ids from given table.
     * 
     * @param handle
     *            Handle for the table to query.
     * @return All ids in that table.
     * @throws DbHelperException
     *             When it's unable to find requested table.
     */
    public Set<String> getUrlIds(final String handle) throws DbHelperException {
        final SQLiteCursor cursor = getCursorForTable(handle);
        final int columnId = cursor.getColumnIndex("url_id");
        final HashSet<String> set = new HashSet<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            set.add(cursor.getString(columnId));
            cursor.moveToNext();
        }
        cursor.close();
        return set;
    }

    /**
     * Adds new entries to the table.
     * 
     * @param handle
     *            Handle for the table to insert new entries to.
     * @param objects
     *            Collection of new entries.
     * @return True if db has been modified, false otherwise.
     * @throws DbHelperException
     *             When it's unable to find requested table.
     */
    public Boolean insertJsonObjects(final String handle, final Collection<JSONObject> objects)
            throws DbHelperException {
        Boolean dataChanged = false;
        if (objects != null) {
            for (JSONObject data : objects) {
                final ContentValues values = new ContentValues();
                try {
                    values.put("url_id", data.getString("id"));
                    values.put("display_url", data.getString("url"));
                    values.put("pop_url", data.getString("pop_url"));
                    values.put("date", data.getString("date"));
                    db.insert(getTableName(handle), null, values);
                    dataChanged = true;
                } catch (final JSONException e) {
                    Hop.warn("Problems parsing JSON response: " + e.getMessage());
                }
            }
        }
        return dataChanged;
    }

    /**
     * Removes a number of entries from the table.
     * 
     * @param handle
     *            Handle for the table to drop entries from.
     * @param ids
     *            Collection of URL ids to remove.
     * @return True if db has been modified, false otherwise.
     * @throws DbHelperException
     *             When it's unable to find requested table.
     */
    public Boolean removeIds(final String handle, final Collection<String> ids)
            throws DbHelperException {
        Boolean dataChanged = false;
        if (ids != null) {
            for (String id : ids) {
                db.delete(getTableName(handle), "url_id=?", new String[] {
                    id
                });
                dataChanged = true;
            }
        }
        return dataChanged;
    }
}
