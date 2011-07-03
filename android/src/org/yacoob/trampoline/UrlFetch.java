package org.yacoob.trampoline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
     * @param url
     *            URL to fetch
     * @return the resulting {@link JSONobject}
     * @throws ClientProtocolException
     */
    protected static JSONObject urlToJSONObject(final String url)
            throws ClientProtocolException {
        JSONObject parsed = null;
        try {
            final String jsonblob = urlToString(url);
            if (jsonblob != null) {
                // FIXME: this parsing fails on non-JSON response
                //        and on non-200 responses (including "nothing new" from server)
                parsed = (JSONObject) new JSONTokener(jsonblob).nextValue();
            }
        } catch (final JSONException e) {
            Hop.warn("Trampoline server response is not a valid JSON: "
                    + e.getMessage());
        }
        return parsed;
    }

    protected static JSONObject urlToJSONObjectBoring(final String url) {
        try {
            return UrlFetch.urlToJSONObject(url);
        } catch (final ClientProtocolException e) {
            Hop.warn("Problems talking to Trampoline server: " + e.getMessage());
            return null;
        }
    }

    /**
     * Fetches an URL.
     * 
     * @param url
     *            URL to fetch
     * @return content of the page as {@link String}
     * @throws ClientProtocolException
     */
    protected static String urlToString(final String url) throws ClientProtocolException {
        BufferedReader reader = null;
        String response = null;
        try {
            final URI u = new URI(url);
            final HttpGet r = new HttpGet(u);
            final HttpResponse l = FETCHER.execute(r);
            reader = new BufferedReader(new InputStreamReader(l.getEntity()
                    .getContent()));

            String line;
            final StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            response = sb.toString();
        } catch (final URISyntaxException e) {
            Hop.warn("Malformed URL: " + e.getMessage());
        } catch (final ClientProtocolException e) {
            throw e;
        } catch (final IOException e) {
            Hop.warn("Problems talking to remote server: " + e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (final IOException e) {
                Hop.warn("Error closing connection to remote server: "
                        + e.getMessage());
            }
        }
        return response;
    }
}
