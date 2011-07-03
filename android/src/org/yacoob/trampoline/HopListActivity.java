package org.yacoob.trampoline;

import org.yacoob.trampoline.DBHelper.DbHelperException;

import android.app.ListActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteCursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class HopListActivity extends ListActivity {
    private Hop app;
    private boolean is_offline = false;
    private TaskRefreshList refresh_task;
    private DBHelper dbhelper;
    private SQLiteCursor urls;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        app = (Hop) getApplication();
        super.onCreate(savedInstanceState);
        dbhelper = new DBHelper(this);
        try {
            urls = dbhelper.getWriteableCursor("stack");
        } catch (final DbHelperException e) {
            throw new RuntimeException("Can't open database.");
        }
        setListAdapter(new HopSQLAdapter(this, urls));
        setContentView(R.layout.main);
        // Recover background task, if there's one.
        refresh_task = (TaskRefreshList) getLastNonConfigurationInstance();
        if (refresh_task != null) {
            refresh_task.attach(this);
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
        if (refresh_task != null) {
            refresh_task.detach();
        }
        return refresh_task;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final UrlEntry item = (UrlEntry) l.getItemAtPosition(position);
        // We're cheating here: if we know that Trampoline is not available,
        // there's no point in trying to open an URL pointing to it. Instead,
        // use target URL. It won't be popped from stack (it is located on
        // Trampoline itself after all), but at least user will see the URL she
        // wants and will be happy.
        final String url = is_offline == true ? item.getDisplayUrl() : item.getUrl();
        app.showInfo(url);
        this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refresh:
            refreshUrlList();
            return true;
        case R.id.exit:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void refreshUrlList() {
        if (refresh_task == null) {
            refresh_task = new TaskRefreshList(this, "stack", dbhelper);
            refresh_task.execute();
        }
    }

    void refreshDone(Boolean gotNewData, Exception networkProblems) {
        if (networkProblems != null) {
            setOffline();
            app.showComplaint(getString(R.string.fetch_failed));
        } else {
            setOnline();
        }
        if (gotNewData) {
            urls.requery();
        }
        refresh_task = null;
    }

    private void setOffline() {
        setTitle(getString(R.string.app_name) + " "
                + getString(R.string.offline_indicator));
        is_offline = true;
    }

    private void setOnline() {
        setTitle(R.string.app_name);
        is_offline = false;
    }
}