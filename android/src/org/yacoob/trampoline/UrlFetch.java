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

/**
 * Utility class for fetching JSON content from a remote server and turning it
 * into {@link JSONObject}.
 */
final class UrlFetch {

    /**
     * Dummy constructor to stop instantiations.
     */
    private UrlFetch() {
    }

    /** {@link DefaultHttpClient} used for fetching pages from outside. */
    private static final DefaultHttpClient FETCHER = new DefaultHttpClient();

    /**
     * Fetches an URL, tries to parse it into {@link JSONObject}.
     *
     * @param url URL to fetch
     * @return the resulting {@link JSONobject}
     */
    protected static JSONObject urlToJSONObject(final String url) {
        JSONObject parsed = null;
        try {
            String jsonblob = urlToString(url);
            if (jsonblob != null) {
                parsed = (JSONObject) new JSONTokener(jsonblob).nextValue();
            }
        } catch (JSONException e) {
            Hop.warn("Trampoline server response is not a valid JSON: "
                    + e.getMessage());
        }
        return parsed;
    }

    /**
     * Fetches an URL.
     *
     * @param url URL to fetch
     * @return content of the page as {@link String}
     */
    protected static String urlToString(final String url) {
        BufferedReader reader = null;
        String response = null;
        try {
            HttpGet r = new HttpGet(url);
            HttpResponse l = FETCHER.execute(r);
            reader = new BufferedReader(new InputStreamReader(l.getEntity()
                    .getContent()));

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            response = sb.toString();
        } catch (IOException e) {
            Hop.warn("Problems talking to remote server: "
                    + e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                Hop.warn("Error closing connection to remote server: "
                        + e.getMessage());
            }
        }
        return response;
    }
}
