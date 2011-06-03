package org.yacoob.trampoline;

import org.json.JSONException;
import org.json.JSONObject;

public class UrlEntry {
	public String date = "<????-??-??>";
	public String url = "http://...";
	public String id = "";

	public UrlEntry(JSONObject o) {
		try {
			this.date = o.getString("date");
			this.url = o.getString("url");
			this.id = o.getString("id");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public String getUrl() {
		return url;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String toString() {
		return this.url + '\n' + this.date;
	}
}
