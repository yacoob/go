package org.yacoob.trampoline;

import java.io.IOException;
import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is used to represent a single entry on list of URLs presented by
 * Trampoline. You'll need a {@link JSONObject} to create {@link UrlEntry}.
 */
public final class UrlEntry implements Serializable {

    /** Version identifier for Serializable. */
    private static final long serialVersionUID = 7195682952024259668L;

    /** URL fragment to use for construction of Trampoline's "pop" URLs. */
    private static final String POPURLFRAGMENT = "/pop?id=";

    /**
     * {@link String} describing timestamp on which this URL was pushed to
     * Trampoline. Default value will only be visible if {@link JSONObject}
     * provided to constructor is missing this field.
     */
    private String date = "<????-??-??>";

    /**
     * {@link String} describing URL that should be opened when user clicks on
     * this entry. For URLs from stack, this is an URL pointing to Trampolina
     * itself constructed out of provided base url and {@link #POPURLFRAGMENT}.
     * Default value will only be visible if {@link JSONObject} provided to
     * constructor is missing this field.
     */
    private String url = "http://...";

    /**
     * {@link String} describing URL that should be shown to user. It's always
     * the final URL that user will end up on, possibly via Trampoline redirect.
     * Default value for will only be visible if {@link JSONObject} provided to
     * constructor is missing this field.
     */
    private String displayUrl = "http://...";

    /**
     * {@link String} describing Trampoline ID for that URL. It's essentially
     * UNIX timestamp registered at moment of push. Default value will only be
     * visible if {@link JSONObject} provided to constructor is missing this
     * field.
     */
    private String id = "-1";

    /**
     * Creates new {@link UrlEntry} out of {@link JSONObject}. This constructor
     * sets {@link #displayUrl} to same value as {@link #url}.
     * 
     * @param o
     *            {@link JSONObject} containing necessary fields (date, url,
     *            id).
     */
    public UrlEntry(final JSONObject o) {
        try {
            this.date = o.getString("date");
            this.url = o.getString("url");
            this.displayUrl = this.url;
            this.id = o.getString("id");
        } catch (JSONException e) {
            UrlList.warn("JSON object '" + o.toString()
                    + "' is unsuitable for conversion to UrlEntry: "
                    + e.getMessage());
        }
    }

    /**
     * Creates new {@link UrlEntry} out of {@link JSONObject}. This constructor
     * sets {@link #url} to point to Trampoline - visiting this entry will cause
     * the URL to be popped from Trampoline stack.
     * 
     * @param o
     *            {@link JSONObject} containing necessary fields (date, url,
     *            id).
     * @param trampolineUrl
     *            Base URL of Trampoline.
     */
    public UrlEntry(final JSONObject o, final String trampolineUrl) {
        this(o);
        if (trampolineUrl != null) {
            this.url = trampolineUrl + POPURLFRAGMENT + this.id;
        }
    }

    /**
     * Serializes {@link UrlEntry}.
     * 
     * @param out
     *            {@link ObjectOutputStream} to write {@link UrlEntry} to.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void writeObject(final java.io.ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
    }

    /**
     * Deserializes {@link UrlEntry}.
     * 
     * @param in
     *            {@link ObjectInputStream} to read {@link UrlEntry} from.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException
     *             the class not found exception
     */
    private void readObject(final java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    /**
     * Gets the target url.
     * 
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the date.
     * 
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the date.
     * 
     * @param date
     *            the new date
     */
    public void setDate(final String date) {
        this.date = date;
    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the new id
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Sets the target url.
     * 
     * @param url
     *            the new url
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * Gets the display url.
     * 
     * @return the display url
     */
    public String getDisplayUrl() {
        return displayUrl;
    }

    /**
     * Sets the display url.
     * 
     * @param displayurl
     *            the new display url
     */
    public void setDisplayUrl(final String displayurl) {
        this.displayUrl = displayurl;
    }

    /**
     * @see java.lang.Object#toString()
     * @return {@link String} representation of this {@link UrlEntry} object.
     */
    public String toString() {
        return this.displayUrl + '\n' + this.date;
    }
}
