package com.listmylife.avita.listmylife;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Messages extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, ConnectivityReceiver.ConnectivityReceiverListener{
    FirebaseAuth myFireBaseAuth;
    TextView myTextView;
    Button logoutButton;

    ListView listView;

    private static Messages mInstance;


    FloatingActionButton addChat;
    ArrayList<String> room_list;
    ArrayList getKeys;
    MessagesListView arrayAdapter;
    ArrayList myCurrentFriends;
    HashMap map20;
    ArrayAdapter<String> adp;
    SwipeRefreshLayout swiperefresh;
    MessagesListView adapter;
    static ArrayList title;
    ArrayList lastMessage;
    ArrayList timeOfMessage;
    ArrayList lastUsers;
    ArrayList has_list;
    String myUsername="";
    TextView textViewVisible;

    ArrayList listOfAllUsers;


    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcChat");
    DatabaseReference databaseReferenceNewFriends = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcFriends");
    DatabaseReference databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcUsers");
    HashMap<String, String> getUserNamesAndEmails;

    BottomNavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.listmylife.avita.listmylife.R.layout.activity_messages);
        myFireBaseAuth = FirebaseAuth.getInstance();
        if(myFireBaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        has_list= new ArrayList();
        listOfAllUsers = new ArrayList();
        room_list = new ArrayList<>();
        title=  new ArrayList();
        lastMessage = new ArrayList();
        timeOfMessage= new ArrayList();
        lastUsers=  new ArrayList();
        getKeys = new ArrayList();
        getUserNamesAndEmails= (HashMap) getIntent().getSerializableExtra("hashmap");
        Log.d("TESTING_HASHMAP", getUserNamesAndEmails.toString());
        myCurrentFriends = new ArrayList();
        textViewVisible = (TextView) findViewById(com.listmylife.avita.listmylife.R.id.textViewVisible);
        textViewVisible.setVisibility(View.INVISIBLE);


        adp=new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,myCurrentFriends);
        getSupportActionBar().setTitle("Messages");

        addChat = (FloatingActionButton) findViewById(com.listmylife.avita.listmylife.R.id.buttonNewRoom);
        swiperefresh = (SwipeRefreshLayout) findViewById(com.listmylife.avita.listmylife.R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(this);
        //getAllUserNames();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                getKeys();
                //getAllUserNames();

                loadUpData();
                checkConnection();
            }
        });

        swiperefresh.post(new Runnable() {
                              @Override
                              public void run() {
                                  swiperefresh.setRefreshing(true);
                                  myFireBaseAuth = FirebaseAuth.getInstance();
                                  if(myFireBaseAuth.getCurrentUser()==null)
                                  {
                                      finish();
                                      startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
                                  }
                                  title=  new ArrayList();
                                  lastMessage = new ArrayList();
                                  getKeys = new ArrayList();
                                  myCurrentFriends = new ArrayList();

                                  adp=new ArrayAdapter<String>(getApplicationContext(),
                                          android.R.layout.simple_dropdown_item_1line,myCurrentFriends);
                                  getSupportActionBar().setTitle("Messages");

                                  addChat = (FloatingActionButton) findViewById(com.listmylife.avita.listmylife.R.id.buttonNewRoom);
                                  swiperefresh = (SwipeRefreshLayout) findViewById(com.listmylife.avita.listmylife.R.id.swiperefresh);

                                  loadUpData();
                                  //listView.setAdapter(arrayAdapter);
                                  listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                      @Override
                                      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                          //Toast.makeText(getApplicationContext(),Integer.toString(position),Toast.LENGTH_SHORT).show();
                                          FirebaseUser user = myFireBaseAuth.getCurrentUser();

                                          Intent intent = new Intent(getApplicationContext(), Chat_Room.class);
                                          intent.putExtra("name",user.getEmail());
                                          intent.putExtra("chat",title.get(position).toString());
                                          intent.putExtra("value", getKeys.get(position).toString());
                                          intent.putExtra("has_list", has_list.get(position).toString());
                                          intent.putStringArrayListExtra("list_of_users", (ArrayList) listOfAllUsers.get(position));
                                          intent.putExtra("hashmap", getUserNamesAndEmails);

                                          Log.d("TESTING_VALUESCHAT", listOfAllUsers.get(position).toString());

                                          Log.d("TESTING_SHORT",listView.getItemAtPosition(position).toString());
                                          Log.d("TESTING_SHORT", getKeys.get(position).toString());

                                            checkConnection();
                                          startActivity(intent);
                                          finish();
                                      }
                                  });



                              }
                          }
        );



        //myTextView.setText("Welcome: " + user.getEmail());
        //logoutButton = (Button) findViewById(R.id.buttonLogout);

        listView = (ListView) findViewById(com.listmylife.avita.listmylife.R.id.listView);
        //arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,room_list);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ?
                        0 : listView.getChildAt(0).getTop();
                swiperefresh.setEnabled((topRowVerticalPosition >= 0));
            }
        });
        //listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),Integer.toString(position),Toast.LENGTH_SHORT).show();
                FirebaseUser user = myFireBaseAuth.getCurrentUser();

                Intent intent = new Intent(getApplicationContext(), Chat_Room.class);
                intent.putExtra("name",user.getEmail());
                intent.putExtra("chat",title.get(position).toString());
                intent.putExtra("value", getKeys.get(position).toString());
                intent.putExtra("has_list", has_list.get(position).toString());
                intent.putStringArrayListExtra("list_of_users",(ArrayList) listOfAllUsers.get(position));
                intent.putExtra("hashmap", getUserNamesAndEmails);
    
                Log.d("TESTING_VALUESCHAT2", listOfAllUsers.get(position).toString());
                Log.d("TESTING_SHORT",listView.getItemAtPosition(position).toString());
                Log.d("TESTING_SHORT", getKeys.get(position).toString());


                startActivity(intent);
                finish();
            }
        });
        //chatRoomNames();
        //getKeys();
        //onRefresh();

        navigationView = (BottomNavigationView) findViewById(com.listmylife.avita.listmylife.R.id.navigation);
        BottomNavigationViewHelper.removeShiftMode(navigationView);
        View view = navigationView.findViewById(com.listmylife.avita.listmylife.R.id.menu_messages);
        view.performClick();
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // handle desired action here
                // One possibility of action is to replace the contents above the nav bar
                // return true if you want the item to be displayed as the selected item

                switch(item.getItemId())
                {
                    case com.listmylife.avita.listmylife.R.id.menu_home:
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case com.listmylife.avita.listmylife.R.id.menu_my_lists:
                        Intent intent2 = new Intent(getApplicationContext(),Private_Lists.class);
                        startActivity(intent2);
                        finish();
                        return true;
                    case com.listmylife.avita.listmylife.R.id.menu_messages:
                        //goToMessages();;
                        listView.setSelectionAfterHeaderView();
                        return true;
                    case com.listmylife.avita.listmylife.R.id.menu_my_profile:
                        Intent intent3 = new Intent(getApplicationContext(), Profile_Page_SplashScreen.class);
                        startActivity(intent3);
                        finish();
                        return true;
                }
                return true;
            }
        });
    }
    public static synchronized Messages getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Good! Connected to Internet";
            color = Color.WHITE;
        } else {
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
    public void getKeys()
    {
        //swiperefresh.setRefreshing(true);

        databaseReference.orderByChild("last_message_time_seconds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                DataSnapshot previous;
                ArrayList a = new ArrayList();
                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    ArrayList doIExist = (ArrayList) snapshot.child("users").getValue();
                    if(doIExist.contains(myFireBaseAuth.getCurrentUser().getEmail().toString())) {
                        a.add(snapshot.getKey().toString());
                    }
                }
                Collections.reverse(a);
                getKeys.clear();
                getKeys.addAll(a);
                //swiperefresh.setRefreshing(false);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /*public void getAllUserNames()
    {
        swiperefresh.setRefreshing(true);

        databaseReferenceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    getUserNamesAndEmails.put(snapshot.child("email").getValue().toString(), snapshot.child("username").getValue().toString());
                }
                Log.d("TESTING_VALUESTEST", getUserNamesAndEmails.toString());
                swiperefresh.setRefreshing(false);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }*/

    public void addingNewRoom(View view) {
        Intent intent = new Intent(this, Creating_New_Message.class);
        intent.putExtra("hashmap",getUserNamesAndEmails);
        startActivity(intent);
        finish();
    }

    public void loadUpData()
    {

        databaseReference.orderByChild("last_message_time_seconds").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList s  = new ArrayList();
                ArrayList s1 = new ArrayList();
                ArrayList s2 = new ArrayList();
                ArrayList s3 = new ArrayList();
                ArrayList s4 = new ArrayList();
                ArrayList s5 = new ArrayList();
                Iterator i = dataSnapshot.getChildren().iterator();
                DataSnapshot previous = null;
                while(i.hasNext())
                {
                    previous = (DataSnapshot)i.next();
                    ArrayList getValues=  new ArrayList();
                    Log.d("TESTING_VALUES_ARRAYS", previous.child("users").getValue().toString());
                    getValues = (ArrayList) previous.child("users").getValue();
                    //ArrayList s = new ArrayList();

                    if(getValues.contains(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                    {
                        swiperefresh.setRefreshing(true);
                        try {
                            s1.add(previous.child("last_message").getValue().toString());
                            s2.add(previous.child("last_message_time").getValue().toString());
                            s3.add(previous.child("last_message_name").getValue().toString());
                            s4.add(previous.child("has_list_attached").getValue().toString());
                            s5.add(previous.child("users").getValue());
                        }
                        catch(Exception e)
                        {
                            s1.add("Cannot Retrieve Last Message");
                            s2.add("Cannot Retrieve Last Message Time");
                            s3.add("Unknown");
                            s4.add("Unknown");
                            ArrayList a = new ArrayList();
                            a.add("None");
                            s5.add(a);
                        }

                        String username = getUserNamesAndEmails.get(myFireBaseAuth.getCurrentUser().getEmail().toString());
                        String getChatName = previous.child("name").getValue().toString();
                        String[] arrayOfUsers = getChatName.split(",");
                        ArrayList a = new ArrayList();
                        for(int j=0; j<arrayOfUsers.length;j++)
                        {
                            a.add(arrayOfUsers[j].toString().trim());
                        }
                        Log.d("TESTING_VALUESA", a.toString());
                        if(a.contains(username))
                        {
                            a.clear();
                            for (HashMap.Entry<String, String> entry : getUserNamesAndEmails.entrySet())
                            {
                                if(getValues.contains(entry.getKey()) && !entry.getKey().equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                                {
                                    a.add(entry.getValue());
                                }
                            }
                            s.add(a.toString().replaceAll("\\[", "").replaceAll("\\]",""));
                        }
                        else {
                            s.add(previous.child("name").getValue().toString());
                        }

                    }


                }
                Collections.reverse(s);
                Collections.reverse(s1);
                Collections.reverse(s2);
                Collections.reverse(s3);
                Collections.reverse(s4);
                Collections.reverse(s5);

                room_list.clear();
                room_list.addAll(s);
                title.clear();
                title.addAll(s);

                lastMessage.clear();
                lastMessage.addAll(s1);

                timeOfMessage.clear();
                timeOfMessage.addAll(s2);


                lastUsers.clear();
                lastUsers.addAll(s3);

                has_list.clear();
                has_list.addAll(s4);

                listOfAllUsers.clear();
                listOfAllUsers.addAll(s5);
                Log.d("GETUSERNAME",myUsername);

                if(room_list.size()==0)
                {
                    textViewVisible.setVisibility(View.VISIBLE);
                }
                else if(room_list.size()>0)
                {
                    textViewVisible.setVisibility(View.INVISIBLE);
                }


                Log.i("TESTINGLISTS1",s1.toString());
                Log.i("TESTINGLISTS0",s.toString());
                Log.i("TESTINGLISTS2",s2.toString());
                Log.i("TESTINGLISTS3",s3.toString());
                Log.i("TESTINGLISTS4",getUserNamesAndEmails.toString());
                Log.i("TESTINGLISTS5",s5.toString());

                arrayAdapter = new MessagesListView(getApplicationContext(),title,lastMessage,timeOfMessage,lastUsers);
                listView.setAdapter(arrayAdapter);
                arrayAdapter.notifyDataSetChanged();
                //getAllUserNames();
                getKeys();;
                swiperefresh.setRefreshing(false);


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
                //Toast.makeText(this, "Refresh selected", Toast.LENGTH_SHORT)
                       // .show();
                return true;


            case com.listmylife.avita.listmylife.R.id.new_list:
                new_list_selected();
                //Toast.makeText(this, "New List Selected", Toast.LENGTH_SHORT)
                       // .show();
                return true;
            case com.listmylife.avita.listmylife.R.id.logout:
                pushLoginInfoNo();
                logout();
                OneSignal.setSubscription(false);
               // Toast.makeText(this, "Logout Selected", Toast.LENGTH_SHORT)
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
    private void faq() {
        android.app.AlertDialog.Builder builderSingle = new android.app.AlertDialog.Builder(Messages.this);
        builderSingle.setTitle("FAQ");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Messages.this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("At the bottom of the application there are 3 clickable icons");
        arrayAdapter.add("The home icon will take you to your home page where you can view all lists created by " +
                "other users.");
        arrayAdapter.add("The My Groups icon will allow you to look at lists you have created just for yourself");
        arrayAdapter.add("The Messages icon will allow you to message other users");
        arrayAdapter.add("At the top of the application there are more icons as well");
        arrayAdapter.add("The refresh symbol will refresh the current page you are on");
        arrayAdapter.add("The plus sign symbol will allow you to create either a public or private list");
        arrayAdapter.add("Click on the profile picture to go to your profile page");
        arrayAdapter.add("On your profile page you can view your current friends and search for other users and add them as friends");

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
                android.app.AlertDialog.Builder builderInner = new android.app.AlertDialog.Builder(Messages.this);
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
        //Toast.makeText(this,"Go Home",Toast.LENGTH_LONG).show();
        finish();
    }

    public void goToMyGroups(View view) {
       // Toast.makeText(this,"My Groups",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,Private_Lists.class);
        startActivity(intent);
        finish();

    }


    public void searchForLists(View view) {
       // Toast.makeText(this,"Search Button",Toast.LENGTH_LONG).show();
    }

    public void goToMessages() {
       // Toast.makeText(this, "Go To Messages", Toast.LENGTH_LONG).show();
        //Intent intent = new Intent(this,.class);
        //startActivity(intent);
        //finish();
        listView.setSelectionAfterHeaderView();

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRefresh() {

        getKeys();
        //getAllUserNames();

        loadUpData();


    }
    private class MyAsyncTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... getUsers) {

            databaseReferenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterator i = dataSnapshot.getChildren().iterator();
                    while(i.hasNext())
                    {
                        DataSnapshot snapshot = ((DataSnapshot)i.next());
                        getUserNamesAndEmails.put(snapshot.child("email").getValue().toString(), snapshot.child("username").getValue().toString());
                    }
                    Log.d("TESTING_VALUESTEST", getUserNamesAndEmails.toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            Log.d("TESTING_VALUESEXCEPT", result.toString() );
        }
    }
}
