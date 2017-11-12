package com.listmylife.avita.listmylife;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Chat_Room extends AppCompatActivity{
    String name;
    String chat_room;

    ImageButton sendMessage;
    EditText getMessage;


    private StorageReference mStorage;


    String keys;

    String child_message;
    String child_user_name;

    ScrollView scrollView;

    FirebaseAuth myFireBaseAuth;
    DatabaseReference root;
    DatabaseReference root2;

    TextView displayUsers;
    ProgressDialog progress;

    TextView messages;

    boolean isMine = true;
    private ArrayList<String> chatMessages;
    private ArrayList<String> userNames;
    private  ArrayList<String> times;
    private ArrayList<Boolean> booleanResults;
    private Chat_Class adapter;
    private ListView listView;
    String finalAnswer;
    String hasList;

    ImageButton imageButton;
    //SwipeRefreshLayout swiperefresh;
    String key;
    String time;
    ArrayList getAllUsers;

    HashMap getUserNamesAndEmails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(com.listmylife.avita.listmylife.R.layout.activity_chat__room);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        name = getIntent().getExtras().get("name").toString();
        chat_room = getIntent().getExtras().get("chat").toString();
        key = getIntent().getExtras().get("value").toString();
        hasList = getIntent().getExtras().get("has_list").toString();
        getAllUsers = getIntent().getExtras().getStringArrayList("list_of_users");
        getUserNamesAndEmails= (HashMap) getIntent().getSerializableExtra("hashmap");

        Log.d("TESTING_VALUESCHAT", key);
        Log.d("TESTING_VALUESCHAT", hasList);
        Log.d("TESTING_VALUESCHAT", chat_room);
        Log.d("TESTING_VALUESCHAT", name);
        Log.d("TESTING_VALUESCHAT", getAllUsers.toString());

        progress = new ProgressDialog(this);
        displayUsers = (TextView)findViewById(com.listmylife.avita.listmylife.R.id.DisplayUsers);

        chatMessages = new ArrayList<>();
        userNames = new ArrayList<>();
        booleanResults = new ArrayList<>();

        mStorage = FirebaseStorage.getInstance().getReference();

        myFireBaseAuth = FirebaseAuth.getInstance();
        if(myFireBaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        times = new ArrayList<>();
        imageButton = (ImageButton) findViewById(com.listmylife.avita.listmylife.R.id.imageButtonGoToList);
        if(hasList.equals("yes"))
        {
            imageButton.setVisibility(View.VISIBLE);
        }
        else if(hasList.equals("no"))
        {
            imageButton.setVisibility(View.INVISIBLE);
        }
        /*swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(this);
        swiperefresh.post(new Runnable() {
                              @Override
                              public void run() {
                                  swiperefresh.setRefreshing(true);
                                  refreshAction();


                              }
                          }
        );*/
        DatabaseReference getUserNames = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcUsers");
        getUserNames.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                final ArrayList s  = new ArrayList();
                while(i.hasNext()) {

                    DataSnapshot snapshot2 = ((DataSnapshot)i.next());
                    Log.d("TESTINGNAMES",snapshot2.child("email").getValue().toString());
                    if(snapshot2.child("email").getValue().toString().equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                    {
                        Log.d("TESTINGNAMESINONCREATE",snapshot2.child("email").getValue().toString());
                        //chatMessages.add(snapshot2.child("username").getValue().toString());

                        finalAnswer = snapshot2.child("username").getValue().toString();
                        seeMessages(finalAnswer);

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        root = FirebaseDatabase.getInstance().getReference().child("zxcChatMessages").child(key);

        setTitle("Message Room");

        finalAnswer ="";




        getMessage = (EditText) findViewById(com.listmylife.avita.listmylife.R.id.editTextSendMessage);
        sendMessage = (ImageButton) findViewById(com.listmylife.avita.listmylife.R.id.buttonSendMessage);
        listView = (ListView) findViewById(com.listmylife.avita.listmylife.R.id.list_msg);

        refreshAction();
        chatMessages = new ArrayList<>();



        adapter = new Chat_Class(getApplicationContext(), userNames,chatMessages,booleanResults);
        listView.setAdapter(adapter);
        listView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listView.setSelection(adapter.getCount() - 1);
            }
        });
        pushLoginInfoYes();
    }
    DatabaseReference databaseReferenceLogin = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLogin");

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
    public void refreshAction()
    {
        //swiperefresh.setRefreshing(true);
        DatabaseReference databaseReferenceChat = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcChatMessages").child(key);
        databaseReferenceChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                ArrayList a = new ArrayList();
                String value="";
                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    int value_test;
                    try {
                        value_test = Integer.parseInt(snapshot.getKey());
                        a.add(snapshot.getValue().toString());
                    } catch (NumberFormatException e) {
                        value_test = 0;
                    }

                }
                //Toast.makeText(getApplicationContext(),a.toString(),Toast.LENGTH_LONG).show();
                for(int j=0; j<a.size();j++)
                {
                    if(j!=a.size()-1) {
                        value+=a.get(j)+", ";
                    }
                    else
                    {
                        value+=a.get(j);
                    }

                }
                displayUsers.setText(chat_room);
                //swiperefresh.setRefreshing(false);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    public void seeMessages(final String finalAnswer)
    {
        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                append_message(dataSnapshot, finalAnswer);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                append_message(dataSnapshot, finalAnswer);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void sendMessage(View view) {
        if(getMessage.getText().toString().equals(""))
        {
            Toast.makeText(getApplicationContext(),"Please Input Something :)", Toast.LENGTH_LONG).show();
            return;
        }

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
                        map2.put("msg", getMessage.getText().toString());

                        /*map3.put("name", snapshot2.child("username").getValue().toString());
                        String currentDateTimeString2 = DateFormat.getDateTimeInstance().format(new Date());
                        map3.put("time", currentDateTimeString2);*/
                        mapUsers2.put("name", chat_room);
                        mapUsers2.put("last_message", getMessage.getText().toString());
                        message_root20.updateChildren(mapUsers2);

                        mapUsers3.put("name", chat_room);
                        mapUsers3.put("last_message_name", snapshot2.child("username").getValue().toString());
                        message_root21.updateChildren(mapUsers3);

                        mapUsers4.put("name", chat_room);
                        mapUsers4.put("last_message_time",  DateFormat.getDateTimeInstance().format(new Date()));
                        message_root22.updateChildren(mapUsers4);

                        mapUsers4.put("name", chat_room);
                        mapUsers4.put("last_message_time_seconds",   System.currentTimeMillis());
                        message_root22.updateChildren(mapUsers4);

                        message_root.updateChildren(map2);
                        //message_root2.updateChildren(map3);

                        getMessage.getText().clear();

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    public void append_message(DataSnapshot dataSnapshot, String finalAnswer)
    {
        Iterator i = dataSnapshot.getChildren().iterator();
        DataSnapshot previous;
        while(i.hasNext())
        {
            child_message= (String) ((DataSnapshot)i.next()).getValue();
            child_user_name= (String) ((DataSnapshot)i.next()).getValue();
            //time = (String) ((DataSnapshot)i.next()).getValue();

            Log.i("TESTING_VALUESTESTC1",child_message.toString());
            Log.i("TESTING_VALUESTESTU1",child_user_name.toString());
            //Log.i("TESTING_VALUESTESTB1",time.toString());


            userNames.add(child_message);
            chatMessages.add(child_user_name);
            //times.add(time);
            if(child_user_name.equals(finalAnswer))
            {
                booleanResults.add(true);
            }
            else if(!child_user_name.equals(finalAnswer))
            {
                booleanResults.add(false);
            }
            Log.i("TESTING_VALUESTESTC",chatMessages.toString());
            Log.i("TESTING_VALUESTESTU",userNames.toString());

            Log.i("TESTING_VALUESTESTB",booleanResults.toString());




                //String chatMessage = child_user_name + ": " + child_message;
                //messages.append(chatMessage + "\n");

        }
        adapter.notifyDataSetChanged();
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.listmylife.avita.listmylife.R.menu.chat_room_values, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.listmylife.avita.listmylife.R.id.display_users:
                Intent intent = new Intent(this, Display_Users.class);
                intent.putExtra("chat_name", chat_room);
                intent.putExtra("name",name);
                intent.putStringArrayListExtra("list_of_users",getAllUsers);
                intent.putExtra("hashmap", getUserNamesAndEmails);
                intent.putExtra("value",key);
                intent.putExtra("has_list", hasList);

                startActivity(intent);
                return true;
            case com.listmylife.avita.listmylife.R.id.go_back:
                goBack();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    private void faq() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(Chat_Room.this);
        builderSingle.setTitle("FAQ");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Chat_Room.this, android.R.layout.select_dialog_singlechoice);
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
                AlertDialog.Builder builderInner = new AlertDialog.Builder(Chat_Room.this);
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
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        //Toast.makeText(this,"Go Home",Toast.LENGTH_LONG).show();
        finish();
    }

    public void goToMyGroups(View view) {
        //Toast.makeText(this,"My Groups",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,Private_Lists.class);
        startActivity(intent);
        finish();

    }
    int count=0;



    public void goToMessages(View view) {
        //Toast.makeText(this, "Go To Messages", Toast.LENGTH_LONG).show();
    }

    public void goBack() {
        /*progress.show();
        progress.setMessage("Returning Back To Your Messages");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(Chat_Room.this, Messages_SplashScreen.class);
                startActivity(intent);
                finish();
                progress.dismiss();
            }
        }, 300);*/
        Intent intent = new Intent(Chat_Room.this, Messages_SplashScreen.class);
        startActivity(intent);
        finish();

    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Messages_SplashScreen.class);
        startActivity(intent);
        finish();
    }

    public ScrollView getScrollView() {
        return scrollView;
    }

    public void goToList(View view) {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void uploadPicture(View view) {
        Toast.makeText(getApplicationContext(),"CAMERA CLICKED",Toast.LENGTH_LONG).show();
        return;
    }

    /*@Override
    public void onRefresh() {
        refreshAction();

    }*/
}
