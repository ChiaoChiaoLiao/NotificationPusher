package com.example.chuchiao_liao.notificationpusher;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Pusher";
    private static final String API_KEY = "XXX";
    // TODO: change the strings
    // TARGET could be "/topics/<target topic name>" or FirebaseInstanceId.getInstance().getToken() in iReport of the device
    private static final String TARGET = "YYY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button subscribeButton = (Button) findViewById(R.id.subscribeButton);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // [START subscribe_topics]
                FirebaseMessaging.getInstance().subscribeToTopic("test");
                // [END subscribe_topics]

                // Log and toast
                String msg = "subscribed";
                Log.d(TAG, msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
        final Button pushInvite = (Button) findViewById(R.id.pushInviteNotification);
        pushInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Log and toast
                String msg = FirebaseInstanceId.getInstance().getToken();
                Log.d(TAG, msg);
                Toast.makeText(MainActivity.this, "invite", Toast.LENGTH_SHORT).show();

                notifyToDeviceOwn("invite");
            }
        });
    }

    private void notifyToDeviceOwn(final String type) {
        Log.w(TAG, "by app");
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        new AsyncTask<Void,Void,String>(){
            String finalResponse;
            JSONObject json;
            @Override
            protected String doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    JSONObject bodyObject = new JSONObject();
                    JSONArray targetUsers;
                    try {
                        switch (type) {
                            case "invite":
                                bodyObject.put("inviter", SENDER);
                                bodyObject.put("invitee", RECEIVER);
                                bodyObject.put("is_display", true);
                                break;
                        }
                    } catch (JSONException e) {
                        Log.w(TAG, e.toString());
                        return null;
                    }

                    try {
                        json = new JSONObject();
                        JSONObject dataJson = new JSONObject();
                        dataJson.put("body", bodyObject);
                        dataJson.put("title", "notification test");
                        json.put("data", dataJson);
                        String topicStr = "/topics/AAA";
                        json.put("to", TARGET);
                    } catch (JSONException e) {
                        Log.w(TAG, e.toString());
                        return null;
                    }
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Request request = new Request.Builder()
                            .header("Authorization","key=" + API_KEY)
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    finalResponse = response.body().string();
                }catch (Exception e){
                    Log.w(TAG, e.getMessage());
                }
                return finalResponse;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.w(TAG, "post: " + s);
            }
        }.execute();
    }

    private void notifyToDeviceByAPI() {
        Log.w(TAG, "by CCC API");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                PushNotification.pushNotification(sendRequest());
                return null;
            }
        }.execute();
    }

    private JSONObject sendRequest() {
        JSONObject content = new JSONObject();
        JSONObject bodyObject = new JSONObject();
        try {
            bodyObject.put("uid", String.valueOf(new Date().getTime()));
            bodyObject.put("sender", SENDER);
        } catch (JSONException e) {
            Log.w(TAG, e.toString());
            return null;
        }
        String topicStr = "/topics/AAA";

        try {
            content.put(PushNotification.NOTIFICATION_TITLE, "CCC test");
            content.put(PushNotification.NOTIFICATION_BODY, bodyObject);
            content.put(PushNotification.NOTIFICATION_TO_TOPIC, TARGET);
        } catch (JSONException e) {
            Log.w(TAG, e.toString());
            return null;
        }
        return content;
    }
}
