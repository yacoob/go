package org.yacoob.trampoline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

class UrlFetch {
    private static final DefaultHttpClient f = new DefaultHttpClient();

    protected static JSONObject fetchUrl(String url) {
        JSONObject parsed = null;
        try {
            String json_blob = urlToString(url);
        	if (json_blob != null) {
        		parsed = (JSONObject) new JSONTokener(json_blob).nextValue();
        	}
        } catch (JSONException e) {
        	UrlList.warn("Trampoline server response is not a valid JSON: " + e.getMessage());
        }
        return parsed;
    }

    protected static String urlToString(String url) {
        BufferedReader reader = null;
        String response = null;
        try {
            HttpGet r = new HttpGet(url);
        	HttpResponse l = f.execute(r);
            reader = new BufferedReader(new InputStreamReader(l.getEntity().getContent()));

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            response = sb.toString();
        } catch (IOException e) {
        	UrlList.warn("Problems talking to Trampoline server: " + e.getMessage());
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            	UrlList.warn("Error closing connection to Trampoline server: " + e.getMessage());
            }
        }
        return response;
    }
}