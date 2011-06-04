package org.yacoob.trampoline;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UrlList extends ListActivity {
	private static final String base_url = "http://192.168.1.34:8080/hop";
    private static final String list_url = base_url + "/list?json=1";
    
    private class TaskRefreshList extends AsyncTask<String, Void, JSONObject> {
        private final String[] lists = {"stack"/*, "viewed"*/};

        @Override
        protected JSONObject doInBackground(String... params) {
            return UrlFetch.fetchUrl(params[0]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (result != null) {
                for (String name : lists) {
                    try {
                        List<UrlEntry> new_url_list = new ArrayList<UrlEntry>();
                        JSONArray list = result.optJSONArray(name);
                        for (int i = 0; i < list.length(); i++) {
                        	new_url_list.add(new UrlEntry(list.getJSONObject(i), name == "stack" ? base_url : null));
                        }
                        setUrlList(new_url_list);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showComplaint(e.getMessage());
                    }
                }
            } else {
                showComplaint("No URLs to show");
            }
        }
    }

    private class HopListAdapter extends ArrayAdapter<UrlEntry> {
        private LayoutInflater li;
        
        private HopListAdapter(Context context, List<UrlEntry> objects) {
            super(context, R.layout.listitem, objects);
            li = LayoutInflater.from(context);
        }
        
    	private class ViewHolder {
    		TextView first;
    		TextView second;
    	}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	
        	ViewHolder holder;
        	
            if (convertView == null) {
                convertView = li.inflate(R.layout.listitem, parent, false);
                holder = new ViewHolder();
                holder.first = (TextView) convertView.findViewById(R.id.first);
                holder.second = (TextView) convertView.findViewById(R.id.second);
                convertView.setTag(holder);
            } else {
            	holder = (ViewHolder) convertView.getTag();
            }
            UrlEntry item = getItem(position);
            holder.first.setText(item.getDisplayUrl());
            holder.second.setText(item.getDate());
            return convertView;
        }
    }

    private void refreshUrlList() {
        new TaskRefreshList().execute(list_url);
    }

    private void setUrlList(List<UrlEntry> l) {
		/* XXX: ArrayAdapter<T>.addAll got added in r11.
		 * Without that method we'd need to iterate through
		 * new_url_list, and call add() one by one. Unsmurfy.
		 */
    	setListAdapter(new HopListAdapter(this, l));
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
	protected void onResume() {
		super.onResume();
        // TODO: add alarm to do this refresh on periodical basis
        // TODO: cache those results locally
        refreshUrlList();
	}
    

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String url = ((UrlEntry) l.getItemAtPosition(position)).getUrl();
        showInfo(url);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showToast(String msg, int time) {
        Toast.makeText(getApplicationContext(), msg, time).show();
    }

    private void showComplaint(String complaint) {
        showToast(complaint, 3000);
    }

    private void showInfo(String info) {
        showToast(info, 1500);
    }
}