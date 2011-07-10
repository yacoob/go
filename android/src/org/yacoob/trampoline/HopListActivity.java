package org.yacoob.trampoline;

import org.yacoob.trampoline.DBHelper.DbHelperException;

import android.app.ListActivity;
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

public final class HopListActivity extends ListActivity {
    private Hop app;
    private boolean isOffline = false;
    private TaskRefreshList refreshTask;
    private DBHelper dbhelper;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        app = (Hop) getApplication();
        super.onCreate(savedInstanceState);

        // Set default values for preferences.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Open database.
        dbhelper = new DBHelper(this);
        SQLiteCursor urls = null;
        try {
            urls = dbhelper.getCursorForTable("stack");
        } catch (final DbHelperException e) {
            throw new RuntimeException("Can't open database.");
        }

        // Main list turn on :)
        setListAdapter(new HopSQLAdapter(this, urls));
        setContentView(R.layout.main);

        // Recover background task, if there's one.
        refreshTask = (TaskRefreshList) getLastNonConfigurationInstance();
        if (refreshTask != null) {
            refreshTask.attach(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: add alarm to do this refresh on periodical basis
        refreshUrlList();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        if (refreshTask != null) {
            refreshTask.detach();
        }
        return refreshTask;
    }

    @Override
    public void onListItemClick(final ListView l, final View v,
            final int position, final long id) {
        final Cursor cursor = (Cursor) getListView()
                .getItemAtPosition(position);
        // We're cheating here: if we know that Trampoline is not available,
        // there's no point in trying to open an URL pointing to it. Instead,
        // use target URL. It won't be popped from stack (it is located on
        // Trampoline itself after all), but at least user will see the URL she
        // wants and will be happy.
        final String column = isOffline ? "display_url" : "pop_url";
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
        case R.id.exit:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void refreshUrlList() {
        if (app.onHomeNetwork()) {
            if (refreshTask == null) {
                refreshTask = new TaskRefreshList(this, "stack", dbhelper);
                refreshTask.execute();
            }
        } else {
            setOffline();
        }
    }

    void refreshDone(final Boolean dataChanged, final Exception networkProblems) {
        if (networkProblems != null) {
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
        refreshTask = null;
    }

    private void setOffline() {
        setTitle(getString(R.string.activity_main) + " "
                + getString(R.string.offline_indicator));
        isOffline = true;
    }

    private void setOnline() {
        setTitle(R.string.activity_main);
        isOffline = false;
    }
}