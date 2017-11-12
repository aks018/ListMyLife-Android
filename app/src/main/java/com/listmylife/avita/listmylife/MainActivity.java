package com.listmylife.avita.listmylife;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.onesignal.OneSignal;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity  implements SwipeRefreshLayout.OnRefreshListener , ConnectivityReceiver.ConnectivityReceiverListener{
    FirebaseAuth myFireBaseAuth;


    ArrayList publicList;
    ArrayAdapter<String> publicListAdapter;
    private StorageReference mStorage;
    ListView publicListView;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLists");
    private ProgressDialog progressDialog;
    ArrayList getAllPublicList;
    DatabaseReference databaseReferenceFriends = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcFriendRequest");
    DatabaseReference databaseReferenceLogin = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLogin");
    DatabaseReference databaseReferenceWaitLogin = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcWaitLogin");
    SwipeRefreshLayout swiperefresh;
    BottomNavigationView navigationView;
    DatabaseReference databaseReferenceLoginWaitForList = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLoginList");

    DatabaseReference databaseReferenceNotification = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcEnableNotification");


    String keys;
    TextView emptyList;
    String get_email_current_user;
    ArrayList getDates;

    Main_Activity_ListView adapter;
    ArrayList getDescriptions;

    ArrayList getKeys;
    ArrayList getChatKeys;

    FloatingActionButton addFriend;

    TextView textVisible;
    private static MainActivity mInstance;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(com.listmylife.avita.listmylife.R.layout.activity_main);
        getSupportActionBar().setTitle("Public Lists");

        addFriend = (FloatingActionButton) findViewById(com.listmylife.avita.listmylife.R.id.fab);

        myFireBaseAuth = FirebaseAuth.getInstance();
        if(myFireBaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        navigationView = (BottomNavigationView) findViewById(com.listmylife.avita.listmylife.R.id.navigation);
        progressDialog = new ProgressDialog(this);
        textVisible = (TextView) findViewById(com.listmylife.avita.listmylife.R.id.textViewVisible);
        textVisible.setVisibility(View.INVISIBLE);

        //emptyList = (TextView) findViewById(R.id.empty_list_view);

        //Lets practice with some push notifications
        get_email_current_user = myFireBaseAuth.getCurrentUser().getEmail().toString();
        OneSignal.sendTag("User_Id",get_email_current_user);
        mStorage = FirebaseStorage.getInstance().getReference();
        swiperefresh = (SwipeRefreshLayout) findViewById(com.listmylife.avita.listmylife.R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(this);
        loadUpData();

        checkConnection();
        pushLoginInfoYes();

        swiperefresh.post(new Runnable() {
                              @Override
                              public void run() {
                                  swiperefresh.setRefreshing(true);

                                  //swiperefresh.setRefreshing(true);
                                  checkConnection();
                                  loadUpData();
                                  pushLoginInfoYes();
                                  disableNotifications();


                              }
                          }
        );



        publicListView = (ListView) findViewById(com.listmylife.avita.listmylife.R.id.listViewForPublic);
        //publicListView.setEmptyView( findViewById( R.id.empty_list_view ) );
        publicList = new ArrayList();
        getAllPublicList = new ArrayList();
        getDescriptions = new ArrayList();
        getDates=  new ArrayList();
        getKeys = new ArrayList();
        getChatKeys = new ArrayList();

        progressDialog = new ProgressDialog(this);



        publicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ViewListValues.class);
                intent.putExtra("list_name", getKeys.get(position).toString());
                intent.putExtra("list_name2", publicList.get(position).toString());
                intent.putExtra("key", getChatKeys.get(position).toString());
                intent.putExtra("list_description",getDescriptions);
                intent.putExtra("position",position);
                intent.putExtra("list_public",publicList);
                startActivity(intent);
                finish();
            }
        });
        navigationView = (BottomNavigationView) findViewById(com.listmylife.avita.listmylife.R.id.navigation);
        BottomNavigationViewHelper.removeShiftMode(navigationView);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // handle desired action here
                // One possibility of action is to replace the contents above the nav bar
                // return true if you want the item to be displayed as the selected item

                switch(item.getItemId())
                {
                    case com.listmylife.avita.listmylife.R.id.menu_home:
                        publicListView.setSelectionAfterHeaderView();
                        return true;
                    case com.listmylife.avita.listmylife.R.id.menu_my_lists:
                        Intent intent2 = new Intent(getApplicationContext(),Private_Lists.class);
                        startActivity(intent2);
                        return true;
                    case com.listmylife.avita.listmylife.R.id.menu_messages:
                        goToMessages();;
                        return true;
                    case com.listmylife.avita.listmylife.R.id.menu_my_profile:
                        goToFriends();;
                        return true;

                }
                return true;
            }
        });

        checkForFriendRequests();
        checkForMessageRequests();
        checkForListRequests();
        checkForAccept();
        //disableNotifications();

    }

    DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcNotifications");
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
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public static synchronized MainActivity getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            pushLoginInfoYes();
            message = "Good! Connected to Internet";
            color = Color.WHITE;
        } else {
            pushLoginInfoNo();
            message = "No Connection To Internet";
            color = Color.RED;
            Snackbar snackbar = Snackbar
                    .make(findViewById(com.listmylife.avita.listmylife.R.id.relativeLayout2), message, Snackbar.LENGTH_LONG);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }



    }
    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        MyFirebaseApp.getInstance().setConnectivityListener(this);
    }
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);

    }
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);

    }

    public void loadUpData()
    {
        swiperefresh.setRefreshing(true);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                ArrayList s = new ArrayList();
                ArrayList s2 = new ArrayList();
                ArrayList s3 = new ArrayList();
                ArrayList s4 = new ArrayList();
                ArrayList s5 = new ArrayList();

                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    Log.d("TESTING_VALUES", snapshot.toString());
                    if(snapshot.child("public_or_private").getValue().equals("public"))
                    {
                        ArrayList checkUsers = (ArrayList) snapshot.child("users").getValue();
                        if(checkUsers.contains(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                        {
                            s.add(snapshot.child("title").getValue().toString());
                            s2.add(snapshot.child("description").getValue().toString());
                            s3.add(snapshot.child("time").getValue().toString());
                            s4.add(snapshot.getKey().toString());
                            s5.add(snapshot.child("chatValue").getValue().toString());
                        }
                    }

                }
                Collections.reverse(s);
                Collections.reverse(s2);

                Collections.reverse(s3);

                Collections.reverse(s4);

                Collections.reverse(s5);

                getDescriptions.clear();
                getDescriptions.addAll(s2);

                publicList.clear();
                publicList.addAll(s);

                getDates.clear();
                getDates.addAll(s3);

                getKeys.clear();
                getKeys.addAll(s4);

                getChatKeys.clear();
                getChatKeys.addAll(s5);
                swiperefresh.setRefreshing(false);
                adapter = new Main_Activity_ListView(getApplicationContext(),publicList,getDescriptions,getDates);
                publicListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                if(getDates.size()==0)
                {
                    textVisible.setVisibility(View.VISIBLE);
                }
                else if(getDates.size()>0)
                {
                    textVisible.setVisibility(View.INVISIBLE);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //swiperefresh.setRefreshing(false);

            }
        });
    }
    DatabaseReference databaseReferenceLoginWaitForFriendAccept = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLoginWaitForFriendAccept");
    private void checkForAccept() {
        databaseReferenceLoginWaitForFriendAccept.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList s = new ArrayList();
                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext()) {
                    DataSnapshot snapshot = ((DataSnapshot) i.next());
                    String value = snapshot.child("friend_to_be_accepted").getValue().toString();
                    final String test_value = snapshot.child("accept").getValue().toString();

                    if (value.equals(myFireBaseAuth.getCurrentUser().getEmail().toString())) {
                        Query applesQuery = databaseReferenceLoginWaitForFriendAccept.orderByChild("friend_to_be_accepted").equalTo(myFireBaseAuth.getCurrentUser().getEmail().toString());
                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                                    if (appleSnapshot.child("accept").getValue().equals(test_value)) {
                                        Log.d("SHOW_ERRORS", "DELETION");
                                        appleSnapshot.getRef().removeValue();
                                        sendNotificationConfirmation(myFireBaseAuth.getCurrentUser().getEmail().toString(), test_value.toString());

                                    }
                                    //Toast.makeText(getApplicationContext(),appleSnapshot.child("requester").getValue().toString(),Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    DatabaseReference databaseReferenceLoginWaitForMessage = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLoginMessages");
    private void checkForListRequests()
    {
        databaseReferenceLoginWaitForList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList s = new ArrayList();
                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext()) {
                    DataSnapshot snapshot = ((DataSnapshot) i.next());
                    String value = snapshot.child("message_to_send").getValue().toString();
                    final String test_value = snapshot.child("accept").getValue().toString();

                    if (value.equals(myFireBaseAuth.getCurrentUser().getEmail().toString())) {
                        Query applesQuery = databaseReferenceLoginWaitForList.orderByChild("message_to_send").equalTo(myFireBaseAuth.getCurrentUser().getEmail().toString());
                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                                    if (appleSnapshot.child("accept").getValue().equals(test_value)) {
                                        Log.d("SHOW_ERRORS", "DELETION");
                                        appleSnapshot.getRef().removeValue();
                                        sendNotificationList(myFireBaseAuth.getCurrentUser().getEmail().toString());

                                    }
                                    //Toast.makeText(getApplicationContext(),appleSnapshot.child("requester").getValue().toString(),Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void checkForMessageRequests()
    {
        databaseReferenceLoginWaitForMessage.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList s = new ArrayList();
                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext()) {
                    DataSnapshot snapshot = ((DataSnapshot) i.next());
                    String value = snapshot.child("message_to_send").getValue().toString();
                    final String test_value = snapshot.child("accept").getValue().toString();

                    if (value.equals(myFireBaseAuth.getCurrentUser().getEmail().toString())) {
                        Query applesQuery = databaseReferenceLoginWaitForMessage.orderByChild("message_to_send").equalTo(myFireBaseAuth.getCurrentUser().getEmail().toString());
                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                                    if (appleSnapshot.child("accept").getValue().equals(test_value)) {
                                        Log.d("SHOW_ERRORS", "DELETION");
                                        appleSnapshot.getRef().removeValue();
                                        sendNotificationMessage(myFireBaseAuth.getCurrentUser().getEmail().toString());

                                    }
                                    //Toast.makeText(getApplicationContext(),appleSnapshot.child("requester").getValue().toString(),Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void sendNotificationList(final String email) {
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
                                + "\"contents\": {\"en\": \"While you were away you invited to a new list \nGo to your Public Lists to check it out.\"}"
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
    private void sendNotificationMessage(final String email) {
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
                                + "\"contents\": {\"en\": \"While you were away you invited to a new chat \n Go to your Messages to check it out.\"}"
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
    private void checkForFriendRequests() {
        Log.d("SHOW_ERRORS", "MADE IT TO CHECK FRIEND REQUESTS");
        databaseReferenceWaitLogin.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList s  = new ArrayList();
                Iterator i = dataSnapshot.getChildren().iterator();
                while(i.hasNext())
                {
                    Log.d("SHOW_ERRORS", "MADE IT TO CHECK FRIEND REQUESTS2");

                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    String value = snapshot.child("friend_to_be_requested").getValue().toString();
                    if(value.equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                    {
                        sendNotificationTask(myFireBaseAuth.getCurrentUser().getEmail().toString());
                        Log.d("SHOW_ERRORS2", "MADE IT HERE");
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
    public void pushLoginInfoNo()
    {
        Map<String,Object> map = new HashMap<>();

        keys = databaseReferenceLogin.push().getKey();

        databaseReferenceLogin.updateChildren(map);


        DatabaseReference message_root  = databaseReferenceLogin.child(myFireBaseAuth.getCurrentUser().getEmail().toString().replace('.',' '));

        Map<String,Object> map2 = new HashMap<>();

        map2.put("user", myFireBaseAuth.getCurrentUser().getEmail());
        map2.put("is_login", "no");
        String email = myFireBaseAuth.getCurrentUser().getEmail().toString();
        message_root.child(email.replace('.',' ')).updateChildren(map2);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.listmylife.avita.listmylife.R.menu.values, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.listmylife.avita.listmylife.R.id.search_bar:
                return true;

            case com.listmylife.avita.listmylife.R.id.action_settings:
                Intent intent = new Intent(this,Settings.class);
                startActivity(intent);
                //Toast.makeText(this, "Refresh selected", Toast.LENGTH_SHORT)
                       // .show();
                finish();
                return true;





            case com.listmylife.avita.listmylife.R.id.new_list:
                new_list_selected();

                return true;
            case com.listmylife.avita.listmylife.R.id.logout:
                pushLoginInfoNo();
                logout();
                OneSignal.setSubscription(false);

                //Toast.makeText(this, "Logout Selected", Toast.LENGTH_SHORT)
                       // .show();
                return true;
            case com.listmylife.avita.listmylife.R.id.faq:
                faq();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void faq() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
        builderSingle.setTitle("FAQ");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("At the bottom of the application there are 3 clickable icons");
        arrayAdapter.add("The home icon will take you to your home page where you can view all lists created by " +
                "other users.");
        arrayAdapter.add("The My Groups icon will allow you to look at lists you have created just for yourself");
        arrayAdapter.add("The Messages icon will allow you to message other users");
        arrayAdapter.add("At the top of the application there are more icons as well");
        arrayAdapter.add("The refresh symbol will refresh the current page you are on");
        arrayAdapter.add("The plus sign symbol will allow you to create either a public or private list");
        arrayAdapter.add("The search bar currently is not functioning.");

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(MainActivity.this);
                builderInner.setMessage(strName);
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();
    }

    private void logout() {
        myFireBaseAuth.signOut();
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();

    }

    private void new_list_selected() {

        Intent intent = new Intent(getApplicationContext(), New_List.class);
        startActivity(intent);
        finish();
    }

    private void register_person() {
        myFireBaseAuth.signOut();
        Intent intent = new Intent(this, RegisterActivity.class);

        startActivity(intent);
        finish();
    }

    private void refreshPage() {
        finish();
        startActivity(getIntent());
    }


    public void goToHome(View view) {
        //Toast.makeText(this,"Go Home",Toast.LENGTH_LONG).show();
    }

    public void goToMyGroups(View view) {
        //Toast.makeText(this,"My Groups",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,Private_Lists.class);
        startActivity(intent);
        finish();

    }


    public void searchForLists(View view) {
        //Toast.makeText(this,"Search Button",Toast.LENGTH_LONG).show();
    }

    public void goToMessages() {
        //Toast.makeText(this, "Go To Messages", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,Messages_SplashScreen.class);
        startActivity(intent);
        finish();
    }

    public void goToProfile(View view) {
        Intent intent = new Intent(this, ProfilePage.class);
        startActivity(intent);
        finish();

    }

    /*public void sendNotification(View view) {
        sendNotificationTask();
        Toast.makeText(getApplicationContext(),"HERE",Toast.LENGTH_LONG).show();
    }*/

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
                                + "\"contents\": {\"en\": \"While you were away you got a new friend request. \n Go to Friends to check them out.\"}"
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
    public void sendNotificationConfirmation(final String email, final String accepted)
    {
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
                                + "\"contents\": {\"en\": \"You have a new friend: " + accepted + " \n Go to Friends to check them out.\"}"
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
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        loadUpData();
        checkConnection();
        disableNotifications();

    }

    public void goToFriends() {
        Intent intent2 = new Intent(this, Profile_Page_SplashScreen.class);
        startActivity(intent2);
    finish();
}
    public void testingValues(View view) {
    }
    public void checkForInternet()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected())
        {
            //Do Nothing
            Toast.makeText(getApplicationContext(),"Internet Connection", Toast.LENGTH_LONG).show();

        }
        else
        {
            Toast.makeText(getApplicationContext(),"No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

}
