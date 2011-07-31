package org.yacoob.trampoline;

import org.yacoob.trampoline.DBHelper.DbHelperException;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

/**
 * Main activity class. Presents user with a list of current URLs.
 */
public final class HopListActivity extends ListActivity {

    /** Application object. */
    private Hop app;

    /** Database helper. */
    private DBHelper dbhelper;

    /**
     * Data refresh handling. This BroadCastReceiver will react to broadcasts posted by service and
     * update the lists.
     */
    private class RefreshWatch extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Bundle extras = intent.getExtras();
            String action = intent.getAction();
            if (action.equals(HopRefreshService.NEWDATA_ACTION)) {
                refreshDone(extras.getBoolean("gotNewData"), extras.getBoolean("networkProblems"));
            }
        }
    }

    /** Single receiver to be reused during Activity lifetime. */
    private RefreshWatch refreshWatcher = new RefreshWatch();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        app = (Hop) getApplication();
        super.onCreate(savedInstanceState);

        // Set default values for preferences.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Open database.
        dbhelper = app.getDbHelper();
        SQLiteCursor urls = null;
        try {
            urls = dbhelper.getCursorForTable("stack");
        } catch (final DbHelperException e) {
            throw new RuntimeException("Can't open database.");
        }

        // Main list turn on :)
        setListAdapter(new HopSQLAdapter(this, urls));
        setContentView(R.layout.main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(refreshWatcher, HopRefreshService.REFRESH_FILTER);
        refreshUrlList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(refreshWatcher);
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        final Cursor cursor = (Cursor) getListView().getItemAtPosition(position);
        // We're cheating here: if we know that Trampoline is not available,
        // there's no point in trying to open an URL pointing to it. Instead,
        // use target URL. It won't be popped from stack (it is located on
        // Trampoline itself after all), but at least user will see the URL she
        // wants and will be happy.
        final String column = app.isOffline() ? "display_url" : "pop_url";
        final String url = cursor.getString(cursor.getColumnIndex(column));
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refresh:
            refreshUrlList();
            return true;
        case R.id.prefs:
            startActivity(new Intent(this, HopPreferences.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Refreshes URL list.
     */
    private void refreshUrlList() {
        if (app.onHomeNetwork()) {
            final Intent intent = new Intent(this, HopRefreshService.class);
            intent.setAction(HopRefreshService.REFRESH_MANUAL);
            startService(intent);
        } else {
            setOffline();
        }
    }

    /**
     * Callback for finishing URL list refresh.
     * 
     * @param dataChanged
     *            Has data been modified as a result of refresh? This includes inserts and removals.
     * @param networkProblems
     *            Has there been network problems during refresh?
     */
    void refreshDone(final Boolean dataChanged, final Boolean networkProblems) {
        if (networkProblems) {
            setOffline();
            app.showComplaint(getString(R.string.fetch_failed));
        } else {
            setOnline();
        }
        if (dataChanged) {
            final HopSQLAdapter adapter = (HopSQLAdapter) getListAdapter();
            final SQLiteCursor cursor = (SQLiteCursor) adapter.getCursor();
            cursor.requery();
        }
    }

    /**
     * Sets application offline.
     */
    private void setOffline() {
        setTitle(getString(R.string.activity_main) + " " + getString(R.string.offline_indicator));
        app.setOffline(true);
    }

    /**
     * Sets application online.
     */
    private void setOnline() {
        setTitle(R.string.activity_main);
        app.setOffline(false);
    }
}