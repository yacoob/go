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

final class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "hop.db";
    private static final int DB_VERSION = 1;
    private static final String STACK = "stack";

    private SQLiteDatabase db;

    static class DbHelperException extends Exception {
        private static final long serialVersionUID = 945002551113629770L;

        public DbHelperException(final String msg) {
            super(msg);
        }
    }

    public DBHelper(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(final SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS "
                + STACK
                + " ("
                + BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "url_id VARCHAR, display_url VARCHAR, pop_url VARCHAR, date VARCHAR)");
    }

    @Override
    public void onUpgrade(final SQLiteDatabase database, final int oldVersion,
            final int newVersion) {

    }

    public static String getTableName(final String handle)
            throws DbHelperException {
        if (handle == "stack") {
            return STACK;
        } else {
            throw new DbHelperException("There's no table assigned to: "
                    + handle);
        }
    }

    public SQLiteCursor getCursorForTable(final String table)
            throws DbHelperException {
        final String tableName = getTableName(table);
        final SQLiteCursor cursor = (SQLiteCursor) db.query(tableName, null,
                null, null, null, null, "url_id DESC");
        return cursor;
    }

    public String getNewestUrlId(final String handle) throws DbHelperException {
        final SQLiteCursor cursor = getCursorForTable(handle);
        final int columnId = cursor.getColumnIndex("url_id");
        cursor.moveToFirst();
        String newestUrlId = cursor.getString(columnId);
        cursor.close();
        return newestUrlId;
    }

    public int getUrlCount(final String handle) throws DbHelperException {
        final SQLiteCursor cursor = getCursorForTable(handle);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

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

    public Boolean insertJsonObjects(final String handle,
            final Collection<JSONObject> objects) throws DbHelperException {
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
                    Hop.warn("Problems parsing JSON response: "
                            + e.getMessage());
                }
            }
        }
        return dataChanged;
    }

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
