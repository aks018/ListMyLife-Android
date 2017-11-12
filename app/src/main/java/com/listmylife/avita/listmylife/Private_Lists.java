package com.listmylife.avita.listmylife;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
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

import com.google.firebase.auth.FirebaseAuth;
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

public class Private_Lists extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, ConnectivityReceiver.ConnectivityReceiverListener{
    FirebaseAuth myFireBaseAuth;
    ArrayList publicList;
    ListView publicListView;
    DatabaseReference databaseReference;

    ArrayList getAllPublicList;
    ArrayList getDescriptions;
    ArrayList getDates;

    Main_Activity_ListView adapter;
    ArrayList getKeys;
    SwipeRefreshLayout swiperefresh;

    BottomNavigationView navigationView;

    TextView textVisible;

    private static Private_Lists mInstance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(com.listmylife.avita.listmylife.R.layout.activity_private__lists);
        getSupportActionBar().setTitle("My Lists");

         databaseReference = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLists");
        myFireBaseAuth = FirebaseAuth.getInstance();
        if(myFireBaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        textVisible = (TextView) findViewById(com.listmylife.avita.listmylife.R.id.textViewVisible);
        textVisible.setVisibility(View.INVISIBLE);

        pushLoginInfoYes();

        checkConnection();
                publicListView = (ListView) findViewById(com.listmylife.avita.listmylife.R.id.listViewForPublic);
        publicList = new ArrayList();
        getAllPublicList = new ArrayList();
        getDescriptions = new ArrayList();
        getKeys = new ArrayList();
        getDates = new ArrayList();
        swiperefresh = (SwipeRefreshLayout) findViewById(com.listmylife.avita.listmylife.R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(this);
        swiperefresh.post(new Runnable() {
                              @Override
                              public void run() {
                                  swiperefresh.setRefreshing(true);
                                  checkConnection();

                                  loadUpData();

                              }
                          }
        );


        loadUpData();


        publicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                   /* AlertDialog.Builder builder;

                    builder = new AlertDialog.Builder(getApplicationContext());

                    builder.setTitle(publicList.get(position).toString())
                            .setMessage("Choose to view list items or delete list")
                            .setCancelable(true)
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Iterator i = dataSnapshot.getChildren().iterator();
                                            while(i.hasNext())
                                            {
                                                DataSnapshot previous = (DataSnapshot)i.next();
                                                if(previous.getValue().equals(getKeys.get(position).toString()))
                                                {
                                                    dataSnapshot.getRef().removeValue();
                                                }
                                            }
                                            Intent intent = new Intent(getApplicationContext(),Private_Lists.class);
                                            finish();
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
                            })
                            .setNegativeButton("View Items", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(getApplicationContext(), VistListValuesPrivate.class);
                                    intent.putExtra("list_name", publicList.get(position).toString());
                                    intent.putExtra("list_name2", getKeys.get(position).toString());
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .show();*/
                /*AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        Private_Lists.this );

                // set title
                alertDialogBuilder.setTitle("Choose Font Size");

                // set dialog message
                alertDialogBuilder
                        .setTitle(publicList.get(position).toString())
                        .setMessage("Choose to either delete list or view items")
                        .setCancelable(true)
                        .setPositiveButton("Delete",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                Query applesQuery = databaseReference.orderByChild("title").equalTo(publicList.get(position).toString());
                                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                            if(appleSnapshot.child("title").getValue().equals(publicList.get(position).toString()) && appleSnapshot.child("name").getValue().equals(myFireBaseAuth.getCurrentUser().getEmail()))
                                            {
                                                Log.d("SHOW_ERRORS", "DELETION");
                                                appleSnapshot.getRef().removeValue();
                                                Toast.makeText(getApplicationContext(),"List Deleted", Toast.LENGTH_SHORT).show();
                                                try
                                                {
                                                    Intent intent = new Intent(getApplicationContext(), Notification_Reciever.class);
                                                    PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), publicList.get(position).toString().hashCode()+getDescriptions.get(position).toString().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                                    alarmManager.cancel(sender);
                                                    Log.d("NO_NOTIFICATION", Integer.toString(publicList.get(position).toString().hashCode()+getDescriptions.get(position).toString().hashCode()));
                                                }catch (Exception e)
                                                {
                                                    Log.d("NO_NOTIFICATION","NO NOTIFICATION SET");
                                                }

                                                Intent intent = getIntent();
                                                startActivity(intent);
                                                finish();
                                            }
                                            //Toast.makeText(getApplicationContext(),appleSnapshot.child("requester").getValue().toString(),Toast.LENGTH_LONG).show();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        })
                        .setNegativeButton("View Items", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(getApplicationContext(), VistListValuesPrivate.class);
                                intent.putExtra("list_name", publicList.get(position).toString());
                                intent.putExtra("list_name2", getKeys.get(position).toString());
                                startActivity(intent);
                                finish();

                            }
                        });


                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();*/


                Intent intent = new Intent(getApplicationContext(), VistListValuesPrivate.class);
                intent.putExtra("list_name", publicList.get(position).toString());
                intent.putExtra("list_name2", getKeys.get(position).toString());
                intent.putExtra("position",position);
                intent.putExtra("list_public",publicList);
                intent.putExtra("list_description",getDescriptions);
                startActivity(intent);
                finish();

            }
        });



        navigationView = (BottomNavigationView) findViewById(com.listmylife.avita.listmylife.R.id.navigation);
        BottomNavigationViewHelper.removeShiftMode(navigationView);
        View view = navigationView.findViewById(com.listmylife.avita.listmylife.R.id.menu_my_lists);
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
                        return true;
                    case com.listmylife.avita.listmylife.R.id.menu_my_lists:
                        publicListView.setSelectionAfterHeaderView();
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
    }
    public void goToFriends() {
        Intent intent2 = new Intent(this, Profile_Page_SplashScreen.class);
        startActivity(intent2);
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
    public static synchronized Private_Lists getInstance() {
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

    public void goToMessages() {
        //Toast.makeText(this, "Go To Messages", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,Messages_SplashScreen.class);
        startActivity(intent);
        finish();
    }

    public void loadUpData()
    {
        swiperefresh.setRefreshing(true);


        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList s = new ArrayList();
                ArrayList s2 = new ArrayList();
                ArrayList s3 = new ArrayList();
                ArrayList s4 = new ArrayList();

                Iterator i = dataSnapshot.getChildren().iterator();
                while(i.hasNext())
                {

                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    Log.d("TESTING_VALUES", snapshot.getValue().toString());
                    Log.d("TESTING_VALUES", snapshot.child("public_or_private").getValue().toString());
                    if(snapshot.child("public_or_private").getValue().toString().equals("private") && snapshot.child("name").getValue().toString().equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                    {
                        Log.d("TESTING_VALUES", "HERE");
                        s.add(snapshot.child("title").getValue().toString());
                        s2.add(snapshot.child("description").getValue().toString());
                        s3.add(snapshot.child("time").getValue().toString());
                        s4.add(snapshot.getKey().toString());
                    }
                }

                Collections.reverse(s);
                Collections.reverse(s2);

                Collections.reverse(s3);
                Collections.reverse(s4);


                getDescriptions.clear();
                getDescriptions.addAll(s2);

                publicList.clear();
                publicList.addAll(s);

                getDates.clear();
                getDates.addAll(s3);

                getKeys.clear();
                getKeys.addAll(s4);


                adapter = new Main_Activity_ListView(getApplicationContext(),publicList,getDescriptions,getDates);
                publicListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                swiperefresh.setRefreshing(false);

                if(getDates.size()==0)
                {
                    textVisible.setVisibility(View.VISIBLE);
                }
                else if(getDates.size()>0)
                {
                    textVisible.setVisibility(View.INVISIBLE);
                }


                //adapter.notifyDataSetChanged();
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
               // Toast.makeText(this, "Refresh selected", Toast.LENGTH_SHORT)
                       // .show();
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
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(Private_Lists.this);
        builderSingle.setTitle("FAQ");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Private_Lists.this, android.R.layout.select_dialog_singlechoice);
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
                AlertDialog.Builder builderInner = new AlertDialog.Builder(Private_Lists.this);
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
        finish();
        //Toast.makeText(this,"Go Home",Toast.LENGTH_LONG).show();
    }

    public void goToMyGroups(View view) {
        //Toast.makeText(this,"My Groups",Toast.LENGTH_LONG).show();

    }


    public void searchForLists(View view) {
        //Toast.makeText(this,"Search Button",Toast.LENGTH_LONG).show();
    }

    public void goToMessages(View view) {
        //Toast.makeText(this, "Go To Messages", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,Messages_SplashScreen.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onRefresh() {
        loadUpData();
    }


}
