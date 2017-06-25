package com.example.chuchiao_liao.notificationpusher;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PushNotification {
    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final String TAG = "PushNotification";

    public static final String NOTIFICATION_TITLE = "title";
    public static final String NOTIFICATION_BODY = "body";

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------
    public static void pushNotification(JSONObject content) {
        try {
            // response is not important; server error will be caught here
            sendRequest(content);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Log.w(TAG, "Failed to push notification: " + e.toString());
        }
    }

    private static JSONObject sendRequest(JSONObject content)
            throws InterruptedException, ExecutionException, TimeoutException {

        Log.d(TAG, "[sendRequest]");

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        String url = TokenRequest.getServerUrl();
        TokenRequest request = new TokenRequest(Request.Method.PUT, null, url, null, future, future);
        request.addBody("text/plain", getRequestBody(content));
        request.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(10),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
            @Override
            public void retry(VolleyError error) throws VolleyError {
                super.retry(error);
                Log.v(TAG, "Volley retry:" + error.toString());
            }
        });

        return future.get(30, TimeUnit.SECONDS);
    }

    /**
     * Transforms JSONObject config to string
     */
    private static byte[] getRequestBody(JSONObject content) {
        try {
            String contentStr = content.toString();
            Log.d(TAG, "Request body: " + contentStr);
            return contentStr.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
