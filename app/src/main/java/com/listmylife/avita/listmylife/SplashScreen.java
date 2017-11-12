package com.listmylife.avita.listmylife;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class SplashScreen extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 7310;
    FirebaseAuth myFireBaseAuth;

    DatabaseReference databaseReferenceLogin = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLogin");
    DatabaseReference databaseReferenceFriends = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcFriendRequest");
    //DatabaseReference databaseReferenceLogin = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLogin");
    DatabaseReference databaseReferenceWaitLogin = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcWaitLogin");
    DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcNotifications");

    String keys;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.listmylife.avita.listmylife.R.layout.activity_splash_screen);
        myFireBaseAuth = FirebaseAuth.getInstance();
        if(myFireBaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        pushLoginInfoYes();
        //SharedPreferences shared = getSharedPreferences("NOTIFICATION", MODE_PRIVATE);
        //String channel = (shared.getString("notification", ""));
        OneSignal.startInit(getApplicationContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .setNotificationOpenedHandler(new NotificationOpener())
                .init();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                checkForFriendRequests();
                disableNotifications();
            }
        });
         /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashScreen.this,MainActivity.class);
                SplashScreen.this.startActivity(mainIntent);
                SplashScreen.this.finish();

            }
        }, SPLASH_DISPLAY_LENGTH);
    }
    private class NotificationOpener implements OneSignal.NotificationOpenedHandler {
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            OSNotificationAction.ActionType actionType = result.action.type;
            JSONObject data = result.notification.payload.additionalData;
            String customKey;

            if (data != null) {
                customKey = data.optString("customkey", null);
                if (customKey != null)
                    Log.i("OneSignalExample", "customkey set with value: " + customKey);
            }

            if (actionType == OSNotificationAction.ActionType.ActionTaken)
                Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);

            // The following can be used to open an Activity of your choice.
            // Replace - getApplicationContext() - with any Android Context.
             Intent intent = new Intent(getApplicationContext(), ProfilePage.class);
             intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
             startActivity(intent);

            // Add the following to your AndroidManifest.xml to prevent the launching of your main Activity
            //   if you are calling startActivity above.
     /*
        <application ...>
          <meta-data android:name="com.onesignal.NotificationOpened.DEFAULT" android:value="DISABLE" />
        </application>
     */

        }
    }

    public void  disableNotifications()
    {
        root.child(myFireBaseAuth.getCurrentUser().getEmail().toString().replace('.',' ')).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                while(i.hasNext())
                {
                    DataSnapshot snapshot2 = ((DataSnapshot)i.next());
                    Log.d("CHANNELTESTING",snapshot2.child("value").toString());
                    if(snapshot2.child("value").getValue().toString().equals("no"))
                    {
                        //imageView.setBackgroundResource(R.mipmap.ic_notifications_off_black_24dp);
                        OneSignal.setSubscription(false);

                    }
                    else
                    {
                        OneSignal.setSubscription(true);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void pushLoginInfoYes()
    {
        Map<String,Object> map = new HashMap<>();

        keys = databaseReferenceLogin.push().getKey();

        databaseReferenceLogin.updateChildren(map);


        DatabaseReference message_root  = databaseReferenceLogin.child(myFireBaseAuth.getCurrentUser().getEmail().toString().replace('.',' '));

        Map<String,Object> map2 = new HashMap<>();

        map2.put("user", myFireBaseAuth.getCurrentUser().getEmail());
        map2.put("is_login", "yes");
        String email = myFireBaseAuth.getCurrentUser().getEmail().toString();
        message_root.child(email.replace('.',' ')).updateChildren(map2);
    }
    @Override
    public void onBackPressed() {

    }
    private void checkForFriendRequests() {
        databaseReferenceWaitLogin.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList s  = new ArrayList();
                Iterator i = dataSnapshot.getChildren().iterator();
                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    String value = snapshot.child("friend_to_be_requested").getValue().toString();
                    if(value.equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                    {
                        Log.d("SHOW_ERRORS", "MADE IT HERE");
                        Map notification = new HashMap<>();
                        notification.put("friend_to_be_requested", value);
                        final String test_value =  snapshot.child("requester").getValue().toString();
                        notification.put("requester", snapshot.child("requester").getValue().toString());

                        databaseReferenceFriends.push().setValue(notification);
                        Query applesQuery = databaseReferenceWaitLogin.orderByChild("friend_to_be_requested").equalTo(myFireBaseAuth.getCurrentUser().getEmail().toString());
                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    if(appleSnapshot.child("requester").getValue().equals(test_value))
                                    {
                                        Log.d("SHOW_ERRORS", "DELETION");
                                        appleSnapshot.getRef().removeValue();
                                    }
                                    //Toast.makeText(getApplicationContext(),appleSnapshot.child("requester").getValue().toString(),Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("CHECKING", "onCancelled", databaseError.toException());
                            }
                        });
                        sendNotificationTask(myFireBaseAuth.getCurrentUser().getEmail().toString());


                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void sendNotificationTask(final String email) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int SDK_INT = Build.VERSION.SDK_INT;
                if(SDK_INT>8)
                {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    String send_email = email;
                    try {
                        String jsonResponse;

                        URL url = new URL("https://onesignal.com/api/v1/notifications");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setUseCaches(false);
                        con.setDoOutput(true);
                        con.setDoInput(true);

                        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        con.setRequestProperty("Authorization", "Basic MWJjMGE2ZGEtMWExMS00Nzk0LTgxNjEtNGI1NmE5MDlkM2Y3");
                        con.setRequestMethod("POST");

                        String strJsonBody = "{"
                                + "\"app_id\": \"fd550a07-5421-4dc9-8dd1-ce704932963f\","

                                + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_Id\", \"relation\": \"=\", \"value\": \"" + send_email + "\"}],"

                                + "\"data\": {\"foo\": \"bar\"},"
                                + "\"contents\": {\"en\": \"While you were away you have some friend requests \n Go to your profile to check them out.\"}"
                                + "}";


                        System.out.println("strJsonBody:\n" + strJsonBody);

                        byte[] sendBytes = strJsonBody.getBytes("UTF-8");
                        con.setFixedLengthStreamingMode(sendBytes.length);

                        OutputStream outputStream = con.getOutputStream();
                        outputStream.write(sendBytes);

                        int httpResponse = con.getResponseCode();
                        System.out.println("httpResponse: " + httpResponse);
                        Log.d("SHOW_ERRORS", "MADE IT HERE");

                        if (httpResponse >= HttpURLConnection.HTTP_OK
                                && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                            Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                            Log.d("SHOW_ERRORS", "MADE IT HERE NO ERRORS");
                        } else {
                            Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        }
                        System.out.println("jsonResponse:\n" + jsonResponse);
                        Log.d("SHOW_ERRORS", jsonResponse);

                    } catch (Throwable t) {
                        t.printStackTrace();
                        Log.d("SHOW_ERRORS", t.toString());
                    }
                }
            }
        });
    }


}

