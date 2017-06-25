package com.example.chuchiao_liao.notificationpusher;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TokenRequest extends JsonObjectRequest {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------
    public static class CustomCachePolicyHeaderParser {
        /**
         * See <a href="http://stackoverflow.com/questions/16781244/android-volley-jsonobjectrequest-caching/16852314#16852314">
         *     Android Volley + JSONObject Request Caching</a>
         */
        public static Cache.Entry parseIgnoreCacheHeaders(NetworkResponse response) {
            long now = System.currentTimeMillis();

            Map<String, String> headers = response.headers;
            long serverDate = 0;
            String serverEtag;
            String headerValue;

            headerValue = headers.get("Date");
            if (headerValue != null) {
                serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
            }

            serverEtag = headers.get("ETag");

            final long cacheHitButRefreshed = TimeUnit.DAYS.toMillis(30); // in 30 days cache will be hit, but also refreshed on background
            final long cacheExpired = TimeUnit.DAYS.toMillis(30); // in 30 days this cache entry expires completely
            final long softExpire = now + cacheHitButRefreshed;
            final long ttl = now + cacheExpired;

            Cache.Entry entry = new Cache.Entry();
            entry.data = response.data;
            entry.etag = serverEtag;
            entry.softTtl = softExpire;
            entry.ttl = ttl;
            entry.serverDate = serverDate;
            entry.responseHeaders = headers;

            return entry;
        }
    }

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String SERVER_STAGING = "https://HHH";
    public static final String API_PREFIX_NOTIFY = "FFF";
    private static final String DEBUG_TOKEN_VALID = "DDD";
    public static final String REQUEST_HEADER_FAKE_ACCESS_TOKEN = "FFF";

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------
    private static String getServer() {
        return SERVER_STAGING;
    }

    public static String getServerUrl(){
        return getServer() + API_PREFIX_NOTIFY;
    }

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private String mToken;
    private Map<String, String> mExtraHeaders;
    private String mBodyContentType;
    private byte[] mBody;
    private boolean mEnableCustomCachePolicy = false;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------
    public TokenRequest(int method, String token, String url, JSONObject jsonRequest,
                        Response.Listener<JSONObject> listener,
                        Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        mToken = DEBUG_TOKEN_VALID;
        mExtraHeaders = new HashMap<>();
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();

        // add extra headers
//        headers.putAll(mExtraHeaders);

        headers.put(REQUEST_HEADER_FAKE_ACCESS_TOKEN, mToken);


        return headers;
    }

    @Override
    public String getBodyContentType() {
        return mBodyContentType;
    }

    @Override
    public byte[] getBody() {
        return mBody;
    }

    /**
     * Add extra header (key, value) pair
     *
     * Note: these customized headers could be overridden by internal header definitions
     * @return: previous value associated w/ this key, or null if there is no previous mapping
     */
    public String addExtraHeader(String key, String value) {
        return mExtraHeaders.put(key, value);
    }

    /**
     * Add the body content type and raw data for the POST/PUT method
     */
    public void addBody(String type, byte[] body) {
        mBodyContentType = type;
        mBody = body;
    }

    /**
     * Set ignore server cache header and apply customized cache policy
     */
    public void setEnableCustomCachePolicy(boolean enabled) {
        mEnableCustomCachePolicy = enabled;
    }

    /**
     * Copied from JsonObjectRequest and add server cache header parsing options
     */
    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(new JSONObject(jsonString), mEnableCustomCachePolicy ?
                    CustomCachePolicyHeaderParser.parseIgnoreCacheHeaders(response) :
                    HttpHeaderParser.parseCacheHeaders(response)
            );
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}
