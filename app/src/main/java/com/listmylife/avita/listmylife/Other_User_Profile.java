package com.listmylife.avita.listmylife;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.onesignal.OneSignal;
import com.squareup.picasso.Picasso;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class Other_User_Profile extends AppCompatActivity {
    FirebaseAuth myFireBaseAuth;
    ListView friends;
    TextView profileName;
    TextView otherUsers;
    ArrayList listOfFriends;
    ArrayAdapter adapter;
    AutoCompleteTextView autoCompleteTextView;
    Bundle bundle;
    String email;
    ImageButton addNewFriendButton;
    DatabaseReference databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcUsers");
    DatabaseReference databaseReferenceFriends = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcFriendRequest");
    DatabaseReference databaseReferenceNewFriends = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcFriends");
    DatabaseReference databaseReferenceNewLogin = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLogin");

    DatabaseReference databaseReferenceWaitForLogin = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcWaitLogin");
    ArrayList myCurrentFriends;
    HashMap getEmailAndUsername;

    private StorageReference mStorage;
    ImageView imageViewProfile;
    HashMap<String,String> getUserNamesAndEmails;

    TextView friendStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.listmylife.avita.listmylife.R.layout.activity_other__user__profile);
        myFireBaseAuth = FirebaseAuth.getInstance();
        if(myFireBaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        friendStatus = (TextView) findViewById(R.id.textViewAddFriend);
        addNewFriendButton = (ImageButton) findViewById(com.listmylife.avita.listmylife.R.id.addAsFriend);
        bundle = getIntent().getExtras();
        myCurrentFriends = new ArrayList();
        profileName= (TextView) findViewById(com.listmylife.avita.listmylife.R.id.ProfileName2);
        email = bundle.getString("email");
        getSupportActionBar().setTitle(email +"'s Account");
        //Toast.makeText(this, email, Toast.LENGTH_LONG).show();

        mStorage = FirebaseStorage.getInstance().getReference();

        imageViewProfile = (ImageView) findViewById(com.listmylife.avita.listmylife.R.id.imageView9);
        new LongOperation().execute(email);
        getUserNamesAndEmails= (HashMap) getIntent().getSerializableExtra("hashmap");
        String getUsername = getUserNamesAndEmails.get(email);
        profileName.setText(getUsername);
        listOfFriends = new ArrayList();
        friends = (ListView) findViewById(com.listmylife.avita.listmylife.R.id.friendsListView);
        populateFriends();
        getEmailAndUsername =(HashMap) getIntent().getSerializableExtra("hashmap");

        adapter = new
                ArrayAdapter(this,android.R.layout.simple_list_item_1,myCurrentFriends);
        friends.setAdapter(adapter);
        friends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String email = parent.getItemAtPosition(position).toString();
                //Toast.makeText(getApplicationContext(),email,Toast.LENGTH_LONG).show();
                if(email.equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                {
                    Intent intent = new Intent(getApplicationContext(), ProfilePage.class);
                    intent.putExtra("hashmap",getEmailAndUsername);
                    //Toast.makeText(getApplicationContext(),getEmailAndUsername.toString(),Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    finish();
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), Other_User_Profile.class);
                    intent.putExtra("email", email);
                    intent.putExtra("hashmap",getEmailAndUsername);
                    startActivity(intent);
                    finish();
                }
            }
        });
        setTextForButton();
        pushLoginInfoYes();
    }
    public void sendNotificationToUser(final String user, final String message) {
        final ArrayList values2 = new ArrayList();
        DatabaseReference databaseReferenceNewLoginUser = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLogin").child(user.replace('.', ' '));
        databaseReferenceNewLoginUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                DataSnapshot previous = null;
                while(i.hasNext())
                {
                    Log.d("TESTING-FRIENDS","INSIDE WHILE");
                    previous = (DataSnapshot) i.next();
                    Log.i("SHOW_ERRORS", user.replace('.', ' ').toString());
                    Log.i("SHOW_ERRORS", previous.child("is_login").getValue().toString());
                    if(previous.child("is_login").getValue().toString().equals("yes")){

                            Log.d("SHOW_ERRORS","MADE IT NO ISSUES");
                            Map notification = new HashMap<>();
                            notification.put("friend_to_be_requested", user);
                            notification.put("requester", message);

                            databaseReferenceFriends.push().setValue(notification);
                            sendNotificationTask(user);
                            setTextForButton();
                            return;

                    }
                    else
                    {
                        databaseReferenceWaitForLogin.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Iterator i = dataSnapshot.getChildren().iterator();
                                boolean checks = false;
                                while(i.hasNext())
                                {
                                    DataSnapshot snapshot = ((DataSnapshot)i.next());

                                    Log.d("SHOW_ERRORS", "HELLO: " +  snapshot.child("requester").getValue().toString());
                                    if(snapshot.child("requester").getValue().toString().equals(myFireBaseAuth.getCurrentUser().getEmail().toString())){
                                        if(snapshot.child("friend_to_be_requested").getValue().toString().equals(user))
                                        {
                                            Log.d("SHOW_ERRORS", "HELLO: " +  snapshot.child("friend_to_be_requested").getValue().toString());
                                            values2.add("used");
                                            checks = true;
                                            Toast.makeText(getApplicationContext(),"Request Alreday Sent", Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                    }

                                }
                                if(checks==false)
                                {
                                    pushWaitingForLogin(user);
                                    setTextForButton();
                                    Toast.makeText(getApplicationContext(), "Request Sent", Toast.LENGTH_LONG).show();
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
        Log.d("SHOW_ERRORS", "HELLO: " +  values2.toString());




    }
    /*public void pushWaitingForLogin(final String email_value)
    {
        Map notification = new HashMap<>();
        notification.put("friend_to_be_requested", email_value);
        notification.put("requester", myFireBaseAuth.getCurrentUser().getEmail().toString());

        databaseReferenceWaitForLogin.push().setValue(notification);
    }*/
    public void populateFriends()
    {
        databaseReferenceNewFriends.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                ArrayList getValues = new ArrayList();
                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    //Toast.makeText(getApplicationContext(),snapshot.child("user").getValue().toString(),Toast.LENGTH_LONG).show();

                    if(snapshot.child("user").getValue().toString().equals(email))
                    {
                        myCurrentFriends.add(snapshot.child("friend").getValue().toString());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void setTextForButton()
    {
        databaseReferenceFriends.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                ArrayList getValues = new ArrayList();
                Log.d("TESTING-FRIENDS","IN LOOP FOR ADDNEWFRIEND");
                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    HashMap getFriends = (HashMap) snapshot.getValue();
                    ArrayList values = new ArrayList();
                    ArrayList values2 = new ArrayList();
                    //Toast.makeText(getApplicationContext(), getFriends.values().toString(), Toast.LENGTH_LONG).show();
                    for (Object value : getFriends.values()) {

                        values.add(value.toString());
                        values2.add(value.toString());
                    }
                    //Toast.makeText(getApplicationContext(),values.toString(),Toast.LENGTH_LONG).show();
                    getValues.add(values);

                    Log.d("SHOW_ERRORS", values.toString());

                    //getValues.add(swap(values2));
                    //Log.d("SHOW_ERRORS", "SWAP: " + swap(values2).toString());
                    getValues.add(swap(values2));



                }
                ArrayList newAdd = new ArrayList();
                newAdd.add(email);
                newAdd.add(myFireBaseAuth.getCurrentUser().getEmail().toString());
                for(int a=0; a<getValues.size();a++)
                {
                    if(getValues.get(a).equals(newAdd))
                    {
                        //Toast.makeText(getApplicationContext(),"Friend Request Already Sent", Toast.LENGTH_LONG).show();
                        addNewFriendButton.setBackgroundResource(com.listmylife.avita.listmylife.R.drawable.ic_check_blue);
                        friendStatus.setText("Friend Request Sent");
                        return;
                    }
                }
                databaseReferenceNewFriends.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean value = false;
                        Iterator i = dataSnapshot.getChildren().iterator();
                        while(i.hasNext())
                        {
                            DataSnapshot snapshot = ((DataSnapshot)i.next());
                            if(snapshot.child("user").getValue().toString().equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                            {
                                if(snapshot.child("friend").getValue().toString().equals(email))
                                {
                                    value = true;
                                    //Toast.makeText(getApplicationContext(),"Already Friends", Toast.LENGTH_LONG).show();
                                    addNewFriendButton.setBackgroundResource(com.listmylife.avita.listmylife.R.drawable.ic_happy);
                                    friendStatus.setText("Friends");
                                    return;
                                }
                            }
                        }
                        Log.d("TESTING-FRIENDS","SENDING");

                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                //Toast.makeText(getApplicationContext(),"WE SAFE", Toast.LENGTH_LONG).show();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public ArrayList swap( ArrayList list )
    {
        Object temp = list.get(0) ;  // Save "elk"
        list.set(0, list.get( 1) ) ; // Put "ape" at index 4
        list.set(1, temp ) ; // Put "elk" at index 0
        return list;
    }
    public void addNewFriend(View view) {
        Log.d("SHOW_ERRORS","STARTING");
        databaseReferenceFriends.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                ArrayList<ArrayList> getValues = new ArrayList<ArrayList>();
                Log.d("TESTING-FRIENDS","IN LOOP FOR ADDNEWFRIEND");
                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    HashMap getFriends = (HashMap) snapshot.getValue();
                    ArrayList values = new ArrayList();
                    ArrayList values2 = new ArrayList();
                    ArrayList swapValues = new ArrayList();
                    //Toast.makeText(getApplicationContext(), getFriends.values().toString(), Toast.LENGTH_LONG).show();
                    for (Object value : getFriends.values()) {

                        values.add(value.toString());
                        values2.add(value.toString());
                        //Toast.makeText(getApplicationContext(),getSplit.toString(), Toast.LENGTH_LONG).show();
                    }

                    //Toast.makeText(getApplicationContext(),values.toString(),Toast.LENGTH_LONG).show();
                    getValues.add(values);

                    Log.d("SHOW_ERRORS", values.toString());

                    //getValues.add(swap(values2));
                    //Log.d("SHOW_ERRORS", "SWAP: " + swap(values2).toString());
                    getValues.add(swap(values2));
                }
                Log.d("SHOW_ERRORS", "SWAP: " + getValues.toString());
                ArrayList newAdd = new ArrayList();
                newAdd.add(email);
                newAdd.add(myFireBaseAuth.getCurrentUser().getEmail().toString());

                for(int a=0; a<getValues.size();a++)
                {
                    if(getValues.get(a).equals(newAdd))
                    {
                        Toast.makeText(getApplicationContext(),"Friend Request Already Sent", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                databaseReferenceNewFriends.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean value = false;
                        Iterator i = dataSnapshot.getChildren().iterator();
                        while(i.hasNext())
                        {
                            DataSnapshot snapshot = ((DataSnapshot)i.next());
                            if(snapshot.child("user").getValue().toString().equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                            {
                                if(snapshot.child("friend").getValue().toString().equals(email))
                                {
                                    value = true;
                                    //Toast.makeText(getApplicationContext(),"Already Friends", Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }
                        }
                        Log.d("TESTING-FRIENDS","SENDING");
                        sendNotificationToUser(email, myFireBaseAuth.getCurrentUser().getEmail().toString());
                        setTextForButton();
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                //Toast.makeText(getApplicationContext(),"WE SAFE", Toast.LENGTH_LONG).show();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        setTextForButton();


    }
    public void pushWaitingForLogin(final String email_value)
    {
        Map notification = new HashMap<>();
        notification.put("friend_to_be_requested", email_value);
        notification.put("requester", myFireBaseAuth.getCurrentUser().getEmail().toString());

        databaseReferenceWaitForLogin.push().setValue(notification);
    }





    public void loadUpData()
    {

        databaseReferenceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList s  = new ArrayList();
                Iterator i = dataSnapshot.getChildren().iterator();
                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    HashMap getEmail = (HashMap) snapshot.getValue();
                    for (Object value : getEmail.values()) {
                        if(value.toString()!="")
                        {
                            s.add(value.toString());
                        }

                    }


                }

                listOfFriends.clear();
                listOfFriends.addAll(s);
                ///Toast.makeText(getApplicationContext(),listOfFriends.toString(),Toast.LENGTH_LONG).show();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

            case com.listmylife.avita.listmylife.R.id.action_settings:
                Intent intent = new Intent(this,Settings.class);
                startActivity(intent);
                //Toast.makeText(this, "Refresh selected", Toast.LENGTH_SHORT)
                       //.show();
                finish();
                return true;

            case com.listmylife.avita.listmylife.R.id.action_favorite:
                refreshPage();

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
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(Other_User_Profile.this);
        builderSingle.setTitle("FAQ");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Other_User_Profile.this, android.R.layout.select_dialog_singlechoice);
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
                AlertDialog.Builder builderInner = new AlertDialog.Builder(Other_User_Profile.this);
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
        finish();
        startActivity(new Intent(this,LoginActivity.class));
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

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        //Toast.makeText(this,"Go Home",Toast.LENGTH_LONG).show();
    }

    public void goToMyGroups(View view) {
       // Toast.makeText(this,"My Groups",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,Private_Lists.class);
        startActivity(intent);
        finish();

    }


    public void searchForLists(View view) {
        //Toast.makeText(this,"Search Button",Toast.LENGTH_LONG).show();
    }

    public void goToMessages(View view) {
       // Toast.makeText(this, "Go To Messages", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,Messages_SplashScreen.class);
        startActivity(intent);
        finish();
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
                                + "\"contents\": {\"en\": \"You have a new friend request \n Go to your profile to check it out\"}"
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
    String keys;
    DatabaseReference databaseReferenceLogin = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLogin");

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
        Intent intent = new Intent(this, Profile_Page_SplashScreen.class);
        //intent.putExtra("hashmap",getEmailAndUsername);
        startActivity(intent);
        finish();
    }

    private class LongOperation extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... strings) {
            String getString = strings[0];
            return getString;

        }

        @Override
        protected void onPostExecute(String s) {
            //final ProgressDialog progressDialog = new ProgressDialog(getApplicationContext());
            //progressDialog.show();
            StorageReference childRef = mStorage.child("Profile_Pictures").child(s);
            childRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(getApplicationContext()).load(uri).fit().centerCrop().into(imageViewProfile);
                    //progressDialog.dismiss();
                }
            });
        }
    }

}
