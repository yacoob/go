package org.yacoob.trampoline;

import java.io.IOException;
import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class UrlEntry implements Serializable {
	private static final long serialVersionUID = 7195682952024259668L;

	private static final String popUrlFragment = "/pop?id=";
	
	private String date = "<????-??-??>";
	private String url = "http://...";
	private String displayUrl = "http://...";
	private String id = "";

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
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
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
