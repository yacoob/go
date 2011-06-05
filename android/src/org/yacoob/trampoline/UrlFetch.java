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
            parsed = (JSONObject) new JSONTokener(urlToString(url)).nextValue();
        } catch (JSONException e) {
        	// FIXME: this is actually too noisy and will occur when Trampoline is down.
            e.printStackTrace();
        }
        return parsed;
    }

    protected static String urlToString(String url) {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        HttpGet r = new HttpGet(url);
        try {
            HttpResponse l = f.execute(r);
            reader = new BufferedReader(new InputStreamReader(l.getEntity().getContent()));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}