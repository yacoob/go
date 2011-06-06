package org.yacoob.trampoline;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class HopList extends ListActivity {
    private Hop app;
    private boolean is_offline = false;
    private TaskRefreshList refresh_task;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        app = (Hop) getApplication();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        try {
            ObjectInputStream ois = new ObjectInputStream(
                    openFileInput(Hop.CACHE_FILE));
            setUrlList((List<UrlEntry>) ois.readObject());
            ois.close();
        } catch (Exception e) {
            Hop.warn("Problems during deserialization of cache: "
                    + e.getMessage());
        }
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
    protected void onStop() {
        super.onStop();
        maybeSerializeCache();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        UrlEntry item = (UrlEntry) l.getItemAtPosition(position);
        // We're cheating here: if we know that Trampoline is not available,
        // there's no point in trying to open an URL pointing to it. Instead,
        // use target URL. It won't be popped from stack (it is located on
        // Trampoline itself after all), but at least user will see the URL she
        // wants and will be happy.
        String url = is_offline == true ? item.getDisplayUrl() : item.getUrl();
        app.showInfo(url);
        this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
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

    private void maybeSerializeCache() {
        // TODO: Make this smarter and do NOT dump file every time we're called.
        try {
            ObjectOutputStream oos = new ObjectOutputStream(openFileOutput(
                    Hop.CACHE_FILE, Context.MODE_PRIVATE));
            oos.writeObject(((HopListAdapter) getListAdapter()).getUrlList());
            oos.close();
        } catch (IOException e) {
            Hop.warn("Problems during serialization of cache: "
                    + e.getMessage());
        }
    }

    private void refreshUrlList() {
        if (refresh_task == null) {
            refresh_task = new TaskRefreshList(this);
            refresh_task.execute(Hop.LISTURL);
        }
    }

    void refreshDone(List<UrlEntry> list) {
        if (list != null) {
            setOnline();
            setUrlList(list);
        } else {
            setOffline();
            app.showComplaint(getString(R.string.fetch_failed));
        }
        refresh_task = null;
    }

    private void setUrlList(List<UrlEntry> l) {
        /*
         * XXX: ArrayAdapter<T>.addAll got added in r11. Without that method
         * we'd need to iterate through new_url_list, and call add() one by one.
         * Unsmurfy.
         */
        setListAdapter(new HopListAdapter(this, l));
    }

    private void setOffline() {
        setTitle(getString(R.string.app_name) + " "
                + getString(R.string.offline_indicator));
        is_offline = true;
        maybeSerializeCache();
    }

    private void setOnline() {
        setTitle(R.string.app_name);
        is_offline = false;
    }
}