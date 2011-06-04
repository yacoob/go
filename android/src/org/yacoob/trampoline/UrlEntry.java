package org.yacoob.trampoline;

import org.json.JSONException;
import org.json.JSONObject;

public class UrlEntry {
	private static final String popUrlFragment = "/pop?id=";
	
	public String date = "<????-??-??>";
	public String url = "http://...";
	public String displayUrl = "http://...";
	public String id = "";

	public UrlEntry(JSONObject o) {
		try {
			this.date = o.getString("date");
			this.url = o.getString("url");
			this.displayUrl = this.url;
			this.id = o.getString("id");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public UrlEntry(JSONObject o, String trampolineUrl) {
		this(o);
		if (trampolineUrl != null) {
			this.url = trampolineUrl + popUrlFragment + this.id;
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

	public String getDisplayUrl() {
		return displayUrl;
	}

	public void setDisplayUrl(String displayUrl) {
		this.displayUrl = displayUrl;
	}

	public String toString() {
		return this.displayUrl + '\n' + this.date;
	}
}
