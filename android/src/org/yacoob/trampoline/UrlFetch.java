package org.yacoob.trampoline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Utility class for fetching JSON content from a remote server.
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
     *            URL to fetch.
     * @throws IOException
     *             On network problems or non-200 responses from server.
     * @return The resulting {@link JSONobject} or null if there were problems
     *         during parsing.
     */
    protected static JSONObject urlToJSONObject(final String url)
            throws IOException {
        JSONObject parsed = null;
        try {
            final String jsonblob = urlToString(url);
            if (jsonblob != null) {
                final JSONTokener tokener = new JSONTokener(jsonblob);
                parsed = (JSONObject) tokener.nextValue();
            }
        } catch (final JSONException e) {
            Hop.warn("Trampoline server response is not a valid JSON: "
                    + e.getMessage());
        }
        return parsed;
    }

    /**
     * Boring (unexceptional :) version of urlToJSONObject.
     * 
     * @param url
     *            URL to fetch.
     * @return The resulting {@link JSONobject} or null if there were problems
     *         during fetching or parsing.
     */
    protected static JSONObject urlToJSONObjectBoring(final String url) {
        try {
            return UrlFetch.urlToJSONObject(url);
        } catch (final IOException e) {
            Hop.warn("Problems talking to Trampoline server: " + e.getMessage());
            return null;
        }
    }

    /**
     * Fetches an URL.
     * 
     * @param url
     *            URL to fetch.
     * @return Contents of the page as {@link String}.
     * @throws IOException
     *             On network problems or non-200 responses from server.
     */
    protected static String urlToString(final String url) throws IOException {
        BufferedReader reader = null;
        String content = null;
        try {
            final HttpGet request = new HttpGet(new URI(url));
            final HttpResponse response = FETCHER.execute(request);
            final StatusLine status = response.getStatusLine();
            final int responseCode = status.getStatusCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new HttpResponseException(responseCode,
                        status.getReasonPhrase());
            }
            reader = new BufferedReader(new InputStreamReader(response
                    .getEntity().getContent()));

            String line;
            final StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            content = sb.toString();
        } catch (final URISyntaxException e) {
            Hop.warn("Malformed URL: " + e.getMessage());
            throw (new IOException("Malformed URL"));
        } catch (final HttpResponseException e) {
            if (e.getStatusCode() != HttpURLConnection.HTTP_NOT_FOUND) {
                Hop.warn("Problems talking to remote server:"
                        + e.getStatusCode() + " " + e.getMessage());
            }
            throw (e);
        } catch (final IOException e) {
            Hop.warn("Problems talking to remote server: " + e.getMessage());
            throw (e);
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
        return content;
    }
}
