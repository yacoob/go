package org.yacoob.trampoline;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "hopdb";
    private static final int DB_VERSION = 1;
    private static final String STACK = "stack";

    static class DbHelperException extends Exception {
        private static final long serialVersionUID = 945002551113629770L;

        public DbHelperException(String msg) {
            super(msg);
        }
    }

    public DBHelper(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + STACK + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "url_id VARCHAR, display_url VARCHAR, pop_url VARCHAR, date VARCHAR)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static String getTableName(final String handle) throws DbHelperException {
        if (handle == "stack") {
            return STACK;
        } else {
            throw new DbHelperException("There's no table assigned to: " + handle);
        }
    }

    public SQLiteCursor getReadableCursor(final String table) throws DbHelperException {
        final String tableName = getTableName(table);
        return (SQLiteCursor) getReadableDatabase().query(
                tableName,
                null, null, null,
                null, null, "url_id DESC");
    }

    public SQLiteCursor getWriteableCursor(final String table) throws DbHelperException {
        final String tableName = getTableName(table);
        return (SQLiteCursor) getWritableDatabase().query(
                tableName,
                null, null, null,
                null, null, "url_id DESC");
    }

    public String getNewestUrlId(final String handle) throws DbHelperException {
        final SQLiteCursor cursor = getReadableCursor(handle);
        if (cursor != null) {
            final int columnId = cursor.getColumnIndex("url_id");
            cursor.moveToFirst();
            return cursor.getString(columnId);
        } else {
            return null;
        }
    }

    public int getUrlCount(final String handle) throws DbHelperException {
        final SQLiteCursor cursor = getReadableCursor(handle);
        if (cursor != null) {
            return cursor.getCount();
        } else {
            return 0;
        }
    }

    public Set<String> getUrlIds(final String handle) throws DbHelperException {
        final SQLiteCursor cursor = getReadableCursor(handle);
        final int columnId = cursor.getColumnIndex("url_id");
        final HashSet<String> set = new HashSet<String>();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            set.add(cursor.getString(columnId));
            cursor.moveToNext();
        }
        return set;
    }

    public Boolean insertJsonObjects(final String handle, final Collection<JSONObject> objects) throws DbHelperException {
        Boolean dataChanged = false;
        if (objects.size() != 0) {
            final SQLiteDatabase db = getWritableDatabase();
            final Iterator<JSONObject> it = objects.iterator();
            while (it.hasNext()) {
                final JSONObject data = it.next();
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
            db.close();
        }
        return dataChanged;
    }

    public Boolean removeIds(final String handle, final Collection<String> ids) throws DbHelperException {
        Boolean dataChanged = false;
        if (ids.size() != 0) {
            final SQLiteDatabase db = getWritableDatabase();
            final Iterator<String> it = ids.iterator();
            while (it.hasNext()) {
                final String id[] = {it.next()};
                db.delete(getTableName(handle), "url_id=?", id);
                dataChanged = true;
            }
            db.close();
        }
        return dataChanged;
    }
}
