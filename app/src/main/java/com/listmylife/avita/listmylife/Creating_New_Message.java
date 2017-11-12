package com.listmylife.avita.listmylife;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class Creating_New_Message extends AppCompatActivity {
    ListView listView;
    FirebaseAuth myFireBaseAuth;
    DatabaseReference databaseReferenceNewFriends = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcFriends");
    ArrayList myCurrentFriends;
    HashMap<String,String> getUserNamesAndEmails;
    DatabaseReference databaseReferenceLogin = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLogin");
    EditText editText;
    private AddingFriendsForMessageAdapter adapter;
    DatabaseReference databaseReferenceLoginWaitForMessage = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLoginMessages");
    TextView checkLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.listmylife.avita.listmylife.R.layout.activity_creating__new__message);
        myFireBaseAuth = FirebaseAuth.getInstance();
        if(myFireBaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        checkLength = (TextView) findViewById(R.id.textViewCheckTitleLength);

        getUserNamesAndEmails= (HashMap) getIntent().getSerializableExtra("hashmap");
        editText = (EditText) findViewById(com.listmylife.avita.listmylife.R.id.editText1);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
            @Override
            public void onTextChanged(CharSequence s, int i, int i2, int i3) {
                if(30 - s.length() < 0)
                {
                    checkLength.setText(String.valueOf(30 - s.length()) + " ");
                    checkLength.setTextColor(Color.RED);
                }
                else {
                    checkLength.setText("Characters Left: " + String.valueOf(30 - s.length()) + " ");
                    checkLength.setTextColor(Color.BLACK);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (null != editText.getLayout() && editText.getLayout().getLineCount() > 3) {
                    editText.getText().delete(editText.getText().length() - 1, editText.getText().length());
                }
            }
        });
        myCurrentFriends = new ArrayList();
        listView = (ListView) findViewById(com.listmylife.avita.listmylife.R.id.listview);
        //myCurrentFriends.add(new DataModel("Hey",false));
        adapter = new AddingFriendsForMessageAdapter(myCurrentFriends, getApplicationContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {

                DataModel dataModel= (DataModel) myCurrentFriends.get(position);
                dataModel.checked = !dataModel.checked;
                adapter.notifyDataSetChanged();


            }
        });
        getAllFriends();



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
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(Creating_New_Message.this);
        builderSingle.setTitle("FAQ");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Creating_New_Message.this, android.R.layout.select_dialog_singlechoice);
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
                AlertDialog.Builder builderInner = new AlertDialog.Builder(Creating_New_Message.this);
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

    public void getAllFriends()
    {
        databaseReferenceNewFriends.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                ArrayList getValues = new ArrayList();
                myCurrentFriends.clear();

                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    //Toast.makeText(getApplicationContext(),snapshot.child("user").getValue().toString(),Toast.LENGTH_LONG).show();

                    if(snapshot.child("user").getValue().toString().equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                    {
                        String value = snapshot.child("friend").getValue().toString();
                        //getValues.add(getUserNamesAndEmails.get(snapshot.child("friend").getValue().toString()));
                        String username = getUserNamesAndEmails.get(snapshot.child("friend").getValue().toString()).toString();
                        myCurrentFriends.add(new DataModel(username,false));

                    }
                }
                adapter.notifyDataSetChanged();
                //swiperefresh.setRefreshing(false);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
    String keys;
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
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
        finish();
    }
    ProgressDialog pd;

    public void addingNewRoom(View view) {

        ArrayList allChecked= new ArrayList();
        for(int i=0; i<myCurrentFriends.size();i++)
        {
            DataModel gotModel = (DataModel) myCurrentFriends.get(i);
            if(gotModel.checked)
            {
                allChecked.add(gotModel.name);
            }


        }
        if(allChecked.size()==0)
        {
            Toast.makeText(getApplicationContext(),"Need to include at least one other user", Toast.LENGTH_LONG).show();
            return;
        }

        if(editText.getText().length()>30)
        {
            Toast.makeText(getApplicationContext(),"Title Too Long", Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList a = new ArrayList();
        if(editText.getText().toString().isEmpty()) {
            for (int i = 0; i < allChecked.size(); i++) {
                for (Map.Entry<String, String> entry : getUserNamesAndEmails.entrySet()) {
                    if (entry.getValue().equals(allChecked.get(i).toString())) {
                        a.add(entry.getKey());
                    }
                }

            }
            pd = new ProgressDialog(Creating_New_Message.this);
            pd.setMessage("Creating Chat Room...");
            pd.setCancelable(false);
            pd.show();
            ArrayList answers = allChecked;
            a.add(myFireBaseAuth.getCurrentUser().getEmail().toString());
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcChat");
            DatabaseReference d2 = databaseReference.push();
            DatabaseReference message_root20 = databaseReference.child(d2.getKey());
            Map<String, Object> mapUsers2 = new HashMap<>();
            mapUsers2.put("name", answers.toString().replaceAll("\\[", "").replaceAll("\\]",""));
            mapUsers2.put("users", a);
            message_root20.updateChildren(mapUsers2);

            new DoInBackground().execute(a);
            Log.d("TESTING_TEST","HERE");
            DatabaseReference message_root21 = databaseReference.child(d2.getKey());
            Map<String, Object> mapUsers3 = new HashMap<>();
            mapUsers3.put("name", answers.toString().replaceAll("\\[", "").replaceAll("\\]",""));
            String currentDateTimeString2 = DateFormat.getDateTimeInstance().format(new Date());
            mapUsers3.put("time", currentDateTimeString2);
            message_root21.updateChildren(mapUsers3);

            DatabaseReference message_root22 = databaseReference.child(d2.getKey());
            Map<String, Object> mapUsers4 = new HashMap<>();
            mapUsers4.put("name", answers.toString().replaceAll("\\[", "").replaceAll("\\]",""));
            mapUsers4.put("nochatname", "yes");
            message_root22.updateChildren(mapUsers4);

            DatabaseReference message_root28 = databaseReference.child(d2.getKey());
            Map<String, Object> mapUsers8 = new HashMap<>();
            mapUsers8.put("name", answers.toString().replaceAll("\\[", "").replaceAll("\\]",""));
            mapUsers8.put("last_message_name", "");
            message_root28.updateChildren(mapUsers8);

            DatabaseReference message_root29 = databaseReference.child(d2.getKey());
            Map<String, Object> mapUsers9 = new HashMap<>();
            mapUsers9.put("name", answers.toString().replaceAll("\\[", "").replaceAll("\\]",""));
            mapUsers9.put("last_message_time",  DateFormat.getDateTimeInstance().format(new Date()));
            message_root29.updateChildren(mapUsers9);

            DatabaseReference message_root30 = databaseReference.child(d2.getKey());
            Map<String, Object> mapUsers10 = new HashMap<>();
            mapUsers10.put("name", answers.toString().replaceAll("\\[", "").replaceAll("\\]",""));
            mapUsers10.put("last_message", "No Last Message");
            message_root30.updateChildren(mapUsers10);

            DatabaseReference message_root31 = databaseReference.child(d2.getKey());
            Map<String, Object> mapUsers100 = new HashMap<>();
            mapUsers100.put("name", answers.toString().replaceAll("\\[", "").replaceAll("\\]",""));
            mapUsers100.put("last_message_time_seconds",   System.currentTimeMillis());
            message_root31.updateChildren(mapUsers100);

            DatabaseReference message_root32 = databaseReference.child(d2.getKey());
            Map<String, Object> mapUsers101 = new HashMap<>();
            mapUsers101.put("name", answers.toString().replaceAll("\\[", "").replaceAll("\\]",""));
            mapUsers101.put("has_list_attached", "no");
            message_root32.updateChildren(mapUsers101);
            Toast.makeText(getApplicationContext(),"New Chat Created", Toast.LENGTH_LONG).show();
            //checkIfLogin(a);

            Intent intent = new Intent(getApplicationContext(),Messages_SplashScreen.class);
            startActivity(intent);
            finish();

            return;
        }
        else
        {
            pd = new ProgressDialog(Creating_New_Message.this);
            pd.setMessage("Creating Chat Room...");
            pd.setCancelable(false);
            pd.show();
            String getTitle = editText.getText().toString();
            for (int i = 0; i < allChecked.size(); i++) {
                for (Map.Entry<String, String> entry : getUserNamesAndEmails.entrySet()) {
                    if (entry.getValue().equals(allChecked.get(i).toString())) {
                        a.add(entry.getKey());
                    }
                }

            }
            a.add(myFireBaseAuth.getCurrentUser().getEmail().toString());
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcChat");
            DatabaseReference d2 = databaseReference.push();
            DatabaseReference message_root20 = databaseReference.child(d2.getKey());

            Map<String, Object> mapUsers2 = new HashMap<>();
            mapUsers2.put("name", getTitle);
            mapUsers2.put("users", a);
            message_root20.updateChildren(mapUsers2);


            new DoInBackground().execute(a);
            Log.d("TESTING_TEST","HERE");

            DatabaseReference message_root21 = databaseReference.child(d2.getKey());
            Map<String, Object> mapUsers3 = new HashMap<>();
            mapUsers3.put("name", getTitle);
            String currentDateTimeString2 = DateFormat.getDateTimeInstance().format(new Date());
            mapUsers3.put("time", currentDateTimeString2);
            message_root21.updateChildren(mapUsers3);

            DatabaseReference message_root22 = databaseReference.child(d2.getKey());
            Map<String, Object> mapUsers4 = new HashMap<>();
            mapUsers4.put("name", getTitle);
            mapUsers4.put("nochatname", "no");
            message_root22.updateChildren(mapUsers4);

            DatabaseReference message_root28 = databaseReference.child(d2.getKey());
            Map<String, Object> mapUsers8 = new HashMap<>();
            mapUsers8.put("name", getTitle);
            mapUsers8.put("last_message_name", "");
            message_root28.updateChildren(mapUsers8);

            DatabaseReference message_root29 = databaseReference.child(d2.getKey());
            Map<String, Object> mapUsers9 = new HashMap<>();
            mapUsers9.put("name", getTitle);
            mapUsers9.put("last_message_time",  DateFormat.getDateTimeInstance().format(new Date()));
            message_root29.updateChildren(mapUsers9);

            DatabaseReference message_root30 = databaseReference.child(d2.getKey());
            Map<String, Object> mapUsers10 = new HashMap<>();
            mapUsers10.put("name", getTitle);
            mapUsers10.put("last_message", "No Last Message");
            message_root30.updateChildren(mapUsers10);

            DatabaseReference message_root31 = databaseReference.child(d2.getKey());
            Map<String, Object> mapUsers100 = new HashMap<>();
            mapUsers100.put("name", getTitle);
            mapUsers100.put("last_message_time_seconds",   System.currentTimeMillis());
            message_root31.updateChildren(mapUsers100);

            DatabaseReference message_root32 = databaseReference.child(d2.getKey());
            Map<String, Object> mapUsers101 = new HashMap<>();
            mapUsers101.put("name", getTitle);
            mapUsers101.put("has_list_attached", "no");
            message_root32.updateChildren(mapUsers101);
            Toast.makeText(getApplicationContext(),"New Chat Created", Toast.LENGTH_LONG).show();
            //checkIfLogin(a);


            Intent intent = new Intent(getApplicationContext(),Messages_SplashScreen.class);
            startActivity(intent);
            finish();

            return;

        }
    }
    public class DoInBackground extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>... a) {
            return a[0];
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {

            result.remove(myFireBaseAuth.getCurrentUser().getEmail().toString());
            for (int i = 0; i < result.size(); i++) {
                Log.d("TESTING_TEST2","HERE");

                final ArrayList values2 = new ArrayList();
                final DatabaseReference databaseReferenceNewLoginUser = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLogin").child(result.get(i).toString().replace('.',' '));
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
                            if(previous.child("is_login").getValue().toString().equals("yes"))
                            {
                                sendNotificationTask(previous.child("user").getValue().toString());
                            }
                            else if(previous.child("is_login").getValue().toString().equals("no"))
                            {
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
    public void checkIfLogin(ArrayList a) {
        a.remove(myFireBaseAuth.getCurrentUser().getEmail().toString());
        for (int i = 0; i < a.size(); i++) {
            final ArrayList values2 = new ArrayList();
            final DatabaseReference databaseReferenceNewLoginUser = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLogin").child(a.get(i).toString().replace('.',' '));
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
                        if(previous.child("is_login").getValue().toString().equals("yes"))
                        {
                            sendNotificationTask(previous.child("user").getValue().toString());
                        }
                        else if(previous.child("is_login").getValue().toString().equals("no"))
                        {
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
