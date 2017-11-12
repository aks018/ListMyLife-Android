package com.listmylife.avita.listmylife;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
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
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class Add_New_Friend extends AppCompatActivity {
    TextView getChatName;
    String name;
    String chat_room;
    String hasList;

    FirebaseAuth myFireBaseAuth;

    String key;

    HashMap<String,String> getUserNamesAndEmails;

    DatabaseReference databaseReferenceLoginWaitForMessage = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLoginMessages");

    ArrayList setUsers;
    DatabaseReference databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcUsers");
    private AddingFriendsForMessageAdapter adapter;

    ArrayList myCurrentFriends;

    ListView newFriends;

    DatabaseReference databaseReferenceNewFriends = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcFriends");

    ArrayList newSetUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__new__friend);
        myFireBaseAuth = FirebaseAuth.getInstance();
        if(myFireBaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        setTitle("Add Friends To Chat");

        myCurrentFriends = new ArrayList();
        adapter = new AddingFriendsForMessageAdapter(myCurrentFriends, getApplicationContext());
        //getAllFriends();
        newFriends = (ListView) findViewById(R.id.listViewNewFriends);
        newFriends.setAdapter(adapter);
        newFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {

                DataModel dataModel= (DataModel) myCurrentFriends.get(position);
                dataModel.checked = !dataModel.checked;
                adapter.notifyDataSetChanged();


            }
        });

        Intent intent = getIntent();

        setUsers = getIntent().getExtras().getStringArrayList("list_of_users");
        newSetUsers = new ArrayList();

        chat_room = intent.getStringExtra("chat_name");
        name = intent.getStringExtra("name");
        getUserNamesAndEmails= (HashMap) getIntent().getSerializableExtra("hashmap");
        hasList = getIntent().getStringExtra("has_list");
        key = getIntent().getStringExtra("value");


        getAllFriends();



    }

    public void getAllFriends()
    {
        for(int i=0;i<setUsers.size();i++)
        {
            newSetUsers.add(getUserNamesAndEmails.get(setUsers.get(i)).toString());
        }
        Log.d("SET_USERS2",newSetUsers.toString());

        databaseReferenceNewFriends.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                ArrayList getValues = new ArrayList();

                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    //Toast.makeText(getApplicationContext(),snapshot.child("user").getValue().toString(),Toast.LENGTH_LONG).show();

                    if(snapshot.child("user").getValue().toString().equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                    {
                        String value = snapshot.child("friend").getValue().toString();
                        //getValues.add(getUserNamesAndEmails.get(snapshot.child("friend").getValue().toString()));
                        String username = getUserNamesAndEmails.get(snapshot.child("friend").getValue().toString()).toString();
                        Log.d("SET_USERS3",username.toString());

                        if(!newSetUsers.contains(username))
                        {
                            getValues.add(new DataModel(username, false));
                        }

                    }
                }
                myCurrentFriends.clear();
                //getValues.removeAll(setUsers);
                Log.d("SET_USERS4",getValues.toString());

                myCurrentFriends.addAll(getValues);
                adapter.notifyDataSetChanged();
                //swiperefresh.setRefreshing(false);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Display_Users.class);
        intent.putStringArrayListExtra("list_of_users",setUsers);
        intent.putExtra("chat_name",chat_room);
        intent.putExtra("name",name);
        intent.putExtra("hashmap",getUserNamesAndEmails);
        intent.putExtra("has_list",hasList);
        intent.putExtra("value",key);

        startActivity(intent);
        finish();
    }

    public void addingNewFriends(View view) {
        ArrayList allChecked= new ArrayList();
        for(int i=0; i<myCurrentFriends.size();i++)
        {
            DataModel gotModel = (DataModel) myCurrentFriends.get(i);
            if(gotModel.checked)
            {
                allChecked.add(gotModel.name);
            }


        }
        String currentUsername = getUserNamesAndEmails.get(myFireBaseAuth.getCurrentUser().getEmail().toString());

        if(allChecked.size()==0)
        {
            Toast.makeText(getApplicationContext(),"Need to include at least one other user", Toast.LENGTH_LONG).show();
            return;
        }
        sendingMessageOfNotification(allChecked,currentUsername);
        ArrayList a = new ArrayList();
        for (int i = 0; i < allChecked.size(); i++) {
            for (Map.Entry<String, String> entry : getUserNamesAndEmails.entrySet()) {
                if (entry.getValue().equals(allChecked.get(i).toString())) {
                    a.add(entry.getKey());
                }
            }
        }
        new DoInBackground().execute(a);

        a.addAll(setUsers);
        //Toast.makeText(this, a.toString(), Toast.LENGTH_SHORT).show();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcChat").child(key);
        Map<String, Object> mapUsers2 = new HashMap<>();
        mapUsers2.put("has_list_attached", hasList);
        mapUsers2.put("users", a);
        databaseReference.updateChildren(mapUsers2);
        Intent intent = new Intent(this, Messages_SplashScreen.class);
        intent.putStringArrayListExtra("list_of_users",setUsers);
        intent.putExtra("chat_name",chat_room);
        intent.putExtra("name",name);
        intent.putExtra("hashmap",getUserNamesAndEmails);
        intent.putExtra("has_list",hasList);
        intent.putExtra("value",key);
        startActivity(intent);

        finish();

        return;
    }
    String keys;

    public void sendingMessageOfNotification(final ArrayList allNewAdded, final String currentUsername)
    {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("zxcChatMessages").child(key);

        Map<String,Object> map = new HashMap<>();

        keys = root.push().getKey();

        root.updateChildren(map);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcChat");
        final DatabaseReference message_root20 = databaseReference.child(key);
        final Map<String, Object> mapUsers2 = new HashMap<>();

        final DatabaseReference message_root21 = databaseReference.child(key);
        final Map<String, Object> mapUsers3 = new HashMap<>();

        final DatabaseReference message_root22 = databaseReference.child(key);
        final Map<String, Object> mapUsers4 = new HashMap<>();
        final DatabaseReference message_root23 = databaseReference.child(key);
        final Map<String, Object> mapUsers5 = new HashMap<>();

        final DatabaseReference message_root  = root.child(keys);
        final DatabaseReference message_root2  = root.child(keys);

        final Map<String,Object> map2 = new HashMap<>();

        final Map<String,Object> map3 = new HashMap<>();
        DatabaseReference getUserNames = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcUsers");
        getUserNames.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                final ArrayList s  = new ArrayList();
                while(i.hasNext()) {

                    DataSnapshot snapshot2 = ((DataSnapshot)i.next());
                    Log.d("TESTINGNAMES",snapshot2.child("email").getValue().toString());
                    Log.d("TESTINGNAMESSNAP",snapshot2.getKey().toString());
                    if(snapshot2.child("email").getValue().toString().equals(name))
                    {
                        Log.d("TESTINGNAMESIN",snapshot2.child("email").getValue().toString());
                        //chatMessages.add(snapshot2.child("username").getValue().toString());
                        map2.put("name",snapshot2.child("username").getValue().toString());
                        map2.put("msg", "New Friends Added: " + Arrays.toString(allNewAdded.toArray()).replace("[", "").replace("]", ""));

                        /*map3.put("name", snapshot2.child("username").getValue().toString());
                        String currentDateTimeString2 = DateFormat.getDateTimeInstance().format(new Date());
                        map3.put("time", currentDateTimeString2);*/
                        mapUsers2.put("name", allNewAdded.toString().replaceAll("\\[", "").replaceAll("\\]","")+ ", " + currentUsername);
                        mapUsers2.put("last_message",  "New Friends Added: " + Arrays.toString(allNewAdded.toArray()).replace("[", "").replace("]", ""));
                        message_root20.updateChildren(mapUsers2);

                        mapUsers3.put("name", allNewAdded.toString().replaceAll("\\[", "").replaceAll("\\]","")+ ", " + currentUsername);
                        mapUsers3.put("last_message_name", snapshot2.child("username").getValue().toString());
                        message_root21.updateChildren(mapUsers3);

                        mapUsers4.put("name", allNewAdded.toString().replaceAll("\\[", "").replaceAll("\\]","")+ ", " + currentUsername);
                        mapUsers4.put("last_message_time",  DateFormat.getDateTimeInstance().format(new Date()));
                        message_root22.updateChildren(mapUsers4);

                        mapUsers4.put("name", allNewAdded.toString().replaceAll("\\[", "").replaceAll("\\]","")+ ", " + currentUsername);
                        mapUsers4.put("last_message_time_seconds",   System.currentTimeMillis());
                        message_root22.updateChildren(mapUsers4);

                        message_root.updateChildren(map2);
                        //message_root2.updateChildren(map3);

                        //getMessage.getText().clear();

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public class DoInBackground extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>... a) {
            //Toast.makeText(getApplicationContext(),a[0].toString(),Toast.LENGTH_LONG).show();

            return a[0];
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {

            result.remove(Collections.singleton(myFireBaseAuth.getCurrentUser().getEmail().toString()));
            Log.d("TESTING_TEST21", myFireBaseAuth.getCurrentUser().getEmail().toString());

            for (int i = 0; i < result.size(); i++) {


                if (!result.get(i).toString().equals(myFireBaseAuth.getCurrentUser().getEmail().toString())) {
                    Log.d("TESTING_TEST2", result.get(i).toString());
                    final ArrayList values2 = new ArrayList();
                    final DatabaseReference databaseReferenceNewLoginUser = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLogin").child(result.get(i).toString().replace('.', ' '));
                    databaseReferenceNewLoginUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterator i = dataSnapshot.getChildren().iterator();
                            DataSnapshot previous = null;
                            while (i.hasNext()) {
                                Log.d("TESTING-FRIENDS", "INSIDE WHILE");
                                previous = (DataSnapshot) i.next();
                                //Log.i("SHOW_ERRORS", a.get(j).replace('.', ' ').toString());
                                //Log.i("SHOW_ERRORS", previous.child("is_login").getValue().toString());
                                Log.d("SHOW2", previous.getKey());
                                Log.d("SHOW2", previous.child("is_login").getValue().toString());
                                if (previous.child("is_login").getValue().toString().equals("yes")) {
                                    sendNotificationTask(previous.child("user").getValue().toString());
                                } else if (previous.child("is_login").getValue().toString().equals("no")) {
                                    pushWaitingForLoginForMessage(previous.child("user").getValue().toString());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    Log.d("SHOW_ERRORS", "HELLO: " + values2.toString());


                }
            }
        }
    }
    public void pushWaitingForLoginForMessage(final String email_value)
    {
        Map notification = new HashMap<>();
        notification.put("message_to_send", email_value);
        notification.put("accept", myFireBaseAuth.getCurrentUser().getEmail().toString());

        databaseReferenceLoginWaitForMessage.push().setValue(notification);
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
                                + "\"contents\": {\"en\": \"You have been invited to a new chat \n Go to your messages to check it out\"}"
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
