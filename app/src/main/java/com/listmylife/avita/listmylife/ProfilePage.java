package com.listmylife.avita.listmylife;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.joanzapata.iconify.widget.IconButton;
import com.onesignal.OneSignal;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class ProfilePage extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener , ConnectivityReceiver.ConnectivityReceiverListener {
    FirebaseAuth myFireBaseAuth;
    ListView friends;
    ArrayList listOfFriends;
    ArrayList myCurrentFriends;
    ArrayAdapter adapterForListView;
    ArrayAdapter adapter;
    private StorageReference mStorage;
    SwipeRefreshLayout swiperefresh;
    DatabaseReference databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcUsers");
    DatabaseReference databaseReferenceFriends = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcFriendRequest");
    DatabaseReference databaseReferenceNewFriends = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcFriends");
    DatabaseReference databaseReferenceLoginWaitForFriendAccept = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLoginWaitForFriendAccept");
    HashMap<String, String> getUserNamesAndEmails;
    BottomNavigationView navigationView;

    TextView itemMessagesBadgeTextView;
    IconButton iconButtonMessages;

    TextView tv;



    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    private static ProfilePage mInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.listmylife.avita.listmylife.R.layout.activity_profile_page);
        myFireBaseAuth = FirebaseAuth.getInstance();
        if(myFireBaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        //profileName.setText(myFireBaseAuth.getCurrentUser().getEmail().toString());

        mStorage = FirebaseStorage.getInstance().getReference();

        getUserNamesAndEmails= (HashMap) getIntent().getSerializableExtra("hashmap");
        Log.d("TESTING_HASHMAP", getUserNamesAndEmails.toString());
        myCurrentFriends = new ArrayList();
        listOfFriends = new ArrayList();
        swiperefresh = (SwipeRefreshLayout) findViewById(com.listmylife.avita.listmylife.R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(this);
        swiperefresh.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swiperefresh.setRefreshing(true);
                                        populateFriends();
                                        checkNotification();

                                    }
                                }
        );
        adapterForListView = new ArrayAdapter<String>(this,
                com.listmylife.avita.listmylife.R.layout.support_simple_spinner_dropdown_item, myCurrentFriends);


        /*friends = (ListView) findViewById(R.id.friendsListView);
        friends.setVisibility(View.INVISIBLE);
        friends.setAdapter(adapterForListView);

        friends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String email = parent.getItemAtPosition(position).toString();
                String getEmail="";
                for (Map.Entry<String, String> entry : getUserNamesAndEmails.entrySet()) {
                    if (Objects.equals(email, entry.getValue())) {
                         getEmail = entry.getKey();
                    }
                }
                //Toast.makeText(getApplicationContext(),email,Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getApplicationContext(), Other_User_Profile.class);
                intent.putExtra("email", getEmail);
                startActivity(intent);
                finish();
            }
        });*/
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        expListView = (ExpandableListView) findViewById(com.listmylife.avita.listmylife.R.id.friendsListViewExpandable);
        expListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ?
                        0 : expListView.getChildAt(0).getTop();
                swiperefresh.setEnabled((topRowVerticalPosition >= 0));
            }
        });
        loadUpData();
        checkConnection();;
        pushLoginInfoYes();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                //Toast.makeText(getApplicationContext(),Integer.toString(i),Toast.LENGTH_LONG).show();

                ArrayList gotValues = new ArrayList();
                gotValues = (ArrayList) listDataChild.get(convertIntToLetter(i).toString());
                String username = gotValues.get(i1).toString();
                String getEmail="";
                for (Map.Entry<String, String> entry : getUserNamesAndEmails.entrySet()) {
                    if (Objects.equals(username, entry.getValue())) {
                        getEmail = entry.getKey();
                    }
                }


                // Toast.makeText(getApplicationContext(),getEmail,Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), Other_User_Profile.class);
                intent.putExtra("hashmap",getUserNamesAndEmails);
                intent.putExtra("email", getEmail);
                startActivity(intent);
                finish();

                return false;
            }
        });





        adapter = new
                ArrayAdapter(this,android.R.layout.simple_list_item_1,listOfFriends);

        navigationView = (BottomNavigationView) findViewById(com.listmylife.avita.listmylife.R.id.navigation);
        BottomNavigationViewHelper.removeShiftMode(navigationView);
        View view = navigationView.findViewById(com.listmylife.avita.listmylife.R.id.menu_my_profile);
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
                        Intent intent2 = new Intent(getApplicationContext(), Private_Lists.class);
                        startActivity(intent2);
                        finish();
                        return true;
                    case com.listmylife.avita.listmylife.R.id.menu_messages:
                        goToMessages();;
                        return true;
                    case com.listmylife.avita.listmylife.R.id.menu_my_profile:
                        expListView.setSelectionAfterHeaderView();
                        return true;
                }
                return true;
            }
        });

        getSupportActionBar().setTitle("My Friends");

    }
    @Override
    public void onRefresh() {
        populateFriends();
        checkNotification();
    }


    public String convertIntToLetter(int number) {

        if(number==0)
        {
            return "A";
        }
        if(number==1)
        {
            return "B";
        }
        if(number==2)
        {
            return "C";
        }
        if(number==3)
        {
            return "D";
        }
        if(number==4)
        {
            return "E";
        }
        if(number==5)
        {
            return "F";
        }
        if(number==6)
        {
            return "G";
        }
        if(number==7)
        {
            return "H";
        }
        if(number==8)
        {
            return "I";
        }
        if(number==9)
        {
            return "J";
        }
        if(number==10)
        {
            return "K";
        }
        if(number==11)
        {
            return "L";
        }
        if(number==12)
        {
            return "M";
        }
        if(number==13)
        {
            return "N";
        }
        if(number==14)
        {
            return "O";
        }
        if(number==15)
        {
            return "P";
        }
        if(number==16)
        {
            return "Q";
        }
        if(number==17)
        {
            return "R";
        }
        if(number==18)
        {
            return "S";
        }
        if(number==19)
        {
            return "T";
        }
        if(number==20)
        {
            return "U";
        }
        if(number==21)
        {
            return "V";
        }
        if(number==22)
        {
            return "W";
        }
        if(number==23)
        {
            return "X";
        }
        if(number==24)
        {
            return "Y";
        }
        if(number==25)
        {
            return "Z";
        }
        return "#";


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
    public void populateFriends()
    {
        Log.d("HELLOHERE","HEREIAMINPOPULATE");
        swiperefresh.setRefreshing(true);
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
                        getValues.add(snapshot.child("friend").getValue().toString());

                    }
                }
                /*ArrayList getAllValues = new ArrayList();
                for(int j=0; j<getValues.size();j++)
                {
                    getAllValues.add(getUserNamesAndEmails.get(getValues.get(j)).toString());
                    Log.d("TESTING_VALUESTEST", getUserNamesAndEmails.get(getValues.get(j)).toString());
                }*/
                //myCurrentFriends.clear();
                //myCurrentFriends.addAll(getAllValues);
                //adapterForListView.notifyDataSetChanged();
                listDataHeader.clear();
                listDataChild.clear();

                listDataHeader.add("A");
                listDataHeader.add("B");
                listDataHeader.add("C");
                listDataHeader.add("D");
                listDataHeader.add("E");
                listDataHeader.add("F");
                listDataHeader.add("G");
                listDataHeader.add("H");
                listDataHeader.add("I");
                listDataHeader.add("J");
                listDataHeader.add("K");
                listDataHeader.add("L");
                listDataHeader.add("M");
                listDataHeader.add("N");
                listDataHeader.add("O");
                listDataHeader.add("P");
                listDataHeader.add("Q");
                listDataHeader.add("R");
                listDataHeader.add("S");
                listDataHeader.add("T");
                listDataHeader.add("U");
                listDataHeader.add("V");
                listDataHeader.add("W");
                listDataHeader.add("X");
                listDataHeader.add("Y");
                listDataHeader.add("Z");
                listDataHeader.add("#");


                ArrayList<String> a = new ArrayList<String>();
                ArrayList<String> b = new ArrayList<String>();
                ArrayList<String> c = new ArrayList<String>();
                ArrayList<String> d = new ArrayList<String>();
                ArrayList<String> e = new ArrayList<String>();
                ArrayList<String> f = new ArrayList<String>();
                ArrayList<String> g = new ArrayList<String>();
                ArrayList<String> h = new ArrayList<String>();
                ArrayList<String> i1 = new ArrayList<String>();
                ArrayList<String> j = new ArrayList<String>();
                List<String> k = new ArrayList<String>();
                List<String> l = new ArrayList<String>();
                List<String> m = new ArrayList<String>();
                List<String> n = new ArrayList<String>();
                List<String> o = new ArrayList<String>();
                List<String> p = new ArrayList<String>();
                List<String> q = new ArrayList<String>();
                List<String> r = new ArrayList<String>();
                List<String> s1 = new ArrayList<String>();
                List<String> t = new ArrayList<String>();
                List<String> u = new ArrayList<String>();
                List<String> v = new ArrayList<String>();
                List<String> w = new ArrayList<String>();
                List<String> x = new ArrayList<String>();
                List<String> y = new ArrayList<String>();
                List<String> z = new ArrayList<String>();
                List<String> other = new ArrayList<String>();





                //ArrayList getAllValues=  new ArrayList();
                for(int getValue=0; getValue<getValues.size();getValue++)
                {
                    String username = getUserNamesAndEmails.get(getValues.get(getValue)).toString();
                    if(username.toLowerCase().charAt(0) == 'a')
                    {
                        a.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'b')
                    {
                        b.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'c')
                    {
                        c.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'd')
                    {
                        d.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'e')
                    {
                        e.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'f')
                    {
                        f.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'g')
                    {
                        g.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'h')
                    {
                        h.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'i')
                    {
                        i1.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'j')
                    {
                        j.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'k')
                    {
                        k.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'l')
                    {
                        l.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'm')
                    {
                        m.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'n')
                    {
                        n.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'o')
                    {
                        o.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'p')
                    {
                        p.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'q')
                    {
                        q.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'r')
                    {
                        r.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 's')
                    {
                        s1.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 't')
                    {
                        t.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'u')
                    {
                        u.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'v')
                    {
                        v.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'w')
                    {
                        w.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'x')
                    {
                        x.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'y')
                    {
                        y.add(username);
                    }
                    if(username.toLowerCase().charAt(0) == 'z')
                    {
                        z.add(username);
                    }
                    else if("abcdefghijklmnopqrstuvwxyz".indexOf(username.toLowerCase().charAt(0))<0)
                    {
                        other.add(username);
                    }

                    //Log.d("TESTING_VALUESTEST", getUserNamesAndEmails.get(s.get(j)).toString());
                }

                listDataChild.put(listDataHeader.get(0), a); // Header, Child data
                listDataChild.put(listDataHeader.get(1), b); // Header, Child data
                listDataChild.put(listDataHeader.get(2), c); // Header, Child data
                listDataChild.put(listDataHeader.get(3), d); // Header, Child data
                listDataChild.put(listDataHeader.get(4), e); // Header, Child data
                listDataChild.put(listDataHeader.get(5), f); // Header, Child data
                listDataChild.put(listDataHeader.get(6), g); // Header, Child data
                listDataChild.put(listDataHeader.get(7), h); // Header, Child data
                listDataChild.put(listDataHeader.get(8), i1); // Header, Child data
                listDataChild.put(listDataHeader.get(9), j); // Header, Child data
                listDataChild.put(listDataHeader.get(10), k); // Header, Child data
                listDataChild.put(listDataHeader.get(11), l); // Header, Child data
                listDataChild.put(listDataHeader.get(12), m); // Header, Child data
                listDataChild.put(listDataHeader.get(13), n); // Header, Child data
                listDataChild.put(listDataHeader.get(14), o); // Header, Child data
                listDataChild.put(listDataHeader.get(15), p); // Header, Child data
                listDataChild.put(listDataHeader.get(16), q); // Header, Child data
                listDataChild.put(listDataHeader.get(17), r); // Header, Child data
                listDataChild.put(listDataHeader.get(18), s1); // Header, Child data
                listDataChild.put(listDataHeader.get(19), t); // Header, Child data
                listDataChild.put(listDataHeader.get(20), u); // Header, Child data
                listDataChild.put(listDataHeader.get(21), v); // Header, Child data
                listDataChild.put(listDataHeader.get(22), w); // Header, Child data
                listDataChild.put(listDataHeader.get(23), x); // Header, Child data
                listDataChild.put(listDataHeader.get(24), y); // Header, Child data
                listDataChild.put(listDataHeader.get(25), z); // Header, Child data
                listDataChild.put(listDataHeader.get(26), other); // Header, Child data




                //listOfFriends.clear();
                //listOfFriends.addAll(getAllValues);


                Log.d("TESTINGFRIENDS",listOfFriends.toString());

                ///Toast.makeText(getApplicationContext(),listOfFriends.toString(),Toast.LENGTH_LONG).show();
                listAdapter.notifyDataSetChanged();

                swiperefresh.setRefreshing(false);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }




    public void loadUpData()
    {

        databaseReferenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList s  = new ArrayList();
                Iterator i = dataSnapshot.getChildren().iterator();
                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    String getEmail = snapshot.child("email").getValue().toString();
                    if(!myCurrentFriends.contains(getEmail)) {
                        s.add(getEmail.toString());
                    }
                    //Toast.makeText(getApplicationContext(),getEmail.toString(),Toast.LENGTH_LONG).show();
                }


                ArrayList getAllValues=  new ArrayList();
                for(int getValue=0; getValue<s.size();getValue++)
                {
                   getAllValues.add(getUserNamesAndEmails.get(s.get(getValue)).toString());



                    //Log.d("TESTING_VALUESTEST", getUserNamesAndEmails.get(s.get(j)).toString());
                }





                listOfFriends.clear();
                listOfFriends.addAll(getAllValues);


                Log.d("TESTINGFRIENDS",listOfFriends.toString());


                ///Toast.makeText(getApplicationContext(),listOfFriends.toString(),Toast.LENGTH_LONG).show();
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    public void incomingFriendRequest() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(ProfilePage.this);
        builderSingle.setTitle("Pending Friend Requests: ");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ProfilePage.this, android.R.layout.select_dialog_singlechoice);
        databaseReferenceFriends.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                ArrayList getValues = new ArrayList();
                ArrayList friendToBeRequested = new ArrayList();
                ArrayList requestor = new ArrayList();
                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    HashMap getFriends = (HashMap) snapshot.getValue();
                    ArrayList values = new ArrayList();

                    //Toast.makeText(getApplicationContext(), getFriends.values().toString(), Toast.LENGTH_LONG).show();
                    Iterator iterator = getFriends.values().iterator();
                    while(iterator.hasNext())
                    {
                        String getValue = iterator.next().toString();
                        //Toast.makeText(getApplicationContext(),getValue +"\n" + myFireBaseAuth.getCurrentUser().getEmail().toString(),Toast.LENGTH_LONG).show();
                        if(getValue.equals(myFireBaseAuth.getCurrentUser().getEmail().toString())) {
                            String getResponse = iterator.next().toString();
                            arrayAdapter.add(getResponse);
                        }
                        else {
                            iterator.next();
                        }
                    }



                }
                //Toast.makeText(getApplicationContext(),requestor.toString(),Toast.LENGTH_LONG).show();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String strName = arrayAdapter.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(ProfilePage.this);
                builderInner.setMessage(strName);

                builderInner.setTitle("Do you want to be friends?");
                builderInner.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        ProgressDialog progress = new ProgressDialog(getApplicationContext());
                        //
                        Map notification = new HashMap<>();
                        notification.put("user", myFireBaseAuth.getCurrentUser().getEmail().toString());
                        notification.put("friend", strName.toString());

                        databaseReferenceNewFriends.push().setValue(notification);

                        Map notification2 = new HashMap<>();
                        notification2.put("user", strName.toString());
                        notification2.put("friend", myFireBaseAuth.getCurrentUser().getEmail().toString());


                        databaseReferenceNewFriends.push().setValue(notification2);

                        Query applesQuery = databaseReferenceFriends.orderByChild("friend_to_be_requested").equalTo(myFireBaseAuth.getCurrentUser().getEmail().toString());
                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    if(appleSnapshot.child("requester").getValue().equals(strName.toString()))
                                    {
                                        Log.e("CHECKING", "onCancelled");

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
                        DatabaseReference databaseReferenceNewLoginUser = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLogin").child(strName.toString().replace('.', ' '));
                        databaseReferenceNewLoginUser.addListenerForSingleValueEvent(new ValueEventListener() {
                             @Override
                             public void onDataChange(DataSnapshot dataSnapshot) {
                                 Iterator i = dataSnapshot.getChildren().iterator();
                                 DataSnapshot previous = null;
                                 while (i.hasNext()) {
                                     Log.d("TESTING-FRIENDS", "INSIDE WHILE");
                                     previous = (DataSnapshot) i.next();
                                     if(previous.child("is_login").getValue().toString().equals("yes")){
                                         Log.d("SHOW_ERRORS","MADE IT NO ISSUES");
                                         sendNotificationConfirmation(strName.toString(), myFireBaseAuth.getCurrentUser().getEmail().toString());
                                         return;

                                     }
                                     else
                                     {
                                         pushWaitingForLogin(strName.toString());
                                     }
                                 }
                             }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        checkNotification();
                        dialog.dismiss();
                    }


                });
                builderInner.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Query applesQuery = databaseReferenceFriends.orderByChild("friend_to_be_requested").equalTo(myFireBaseAuth.getCurrentUser().getEmail().toString());
                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    if(appleSnapshot.child("requester").getValue().equals(strName.toString()))
                                    {
                                        Log.e("CHECKING", "onCancelled");

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
                        checkNotification();
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();

    }
    public void pushWaitingForLogin(final String email_value)
    {
        Map notification = new HashMap<>();
        notification.put("friend_to_be_accepted", email_value);
        notification.put("accept", myFireBaseAuth.getCurrentUser().getEmail().toString());

        databaseReferenceLoginWaitForFriendAccept.push().setValue(notification);
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
                                + "\"contents\": {\"en\": \"You have a new friend: " + accepted + " \n Go to your profile to check them out.\"}"
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
   /* public void newFriend(View view) {

        if(TextUtils.isEmpty(autoCompleteText))
        {
            Toast.makeText(this,"Empty User", Toast.LENGTH_LONG).show();
            return;
        }
        if(!listOfFriends.contains(autoCompleteText))
        {
            Toast.makeText(this,"User does not exist", Toast.LENGTH_LONG).show();
            return;
        }
        if(autoCompleteText.equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
        {

            Intent intent = new Intent(this, ProfilePage.class);
            finish();
            startActivity(intent);
            return;
        }
        //Toast.makeText(this,autoCompleteText + "\n" +myFireBaseAuth.getCurrentUser().getEmail().toString(), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, Other_User_Profile.class);
        intent.putExtra("email", autoCompleteText);
        startActivity(intent);
        Toast.makeText(this,autoCompleteText + "\n" + myFireBaseAuth.getCurrentUser().getEmail().toString().replace('.', ' '),Toast.LENGTH_LONG).show();
        //Intent intent = new Intent(this, Other_User_Profile.class);
        //intent.putExtra("email", autoCompleteText);
        //startActivity(intent);

    }*/

    public void addNewFriend(View view) {
        /*AlertDialog.Builder alert = new AlertDialog.Builder(this);


        final AutoCompleteTextView edittext = new AutoCompleteTextView(getApplicationContext());
        edittext.setAdapter(adapter);
        edittext.setThreshold(3);
        alert.setMessage("Enter Email Of Friend You Wish to Add");
        alert.setTitle("Friend Request");
        alert.setCancelable(true);

        alert.setView(edittext);

        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                Editable autoCompleteText = edittext.getText();
                //OR
                if(TextUtils.isEmpty(autoCompleteText))
                {
                    Toast.makeText(getApplicationContext(),"Empty User", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!listOfFriends.contains(autoCompleteText.toString().replaceAll("\\s+","")))
                {
                    Toast.makeText(getApplicationContext(),"User does not exist", Toast.LENGTH_LONG).show();
                    Log.d("TESTINGLISTS",listOfFriends.toString());
                    Log.d("TESTINGLISTS",autoCompleteText.toString());


                    return;
                }
                Intent intent = new Intent(getApplicationContext(), Other_User_Profile.class);
                intent.putExtra("email", autoCompleteText.toString().replaceAll("\\s+",""));
                startActivity(intent);
                //Toast.makeText(getApplicationContext(),autoCompleteText + "\n" + myFireBaseAuth.getCurrentUser().getEmail().toString().replace('.', ' '),Toast.LENGTH_LONG).show();


            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alert.show();*/
        Intent intent = new Intent(getApplicationContext(), Find_New_Friends.class);
        intent.putExtra("hashmap", getUserNamesAndEmails);
        startActivity(intent);
        finish();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.messages);
        MenuItemCompat.setActionView(item, R.layout.notification_friend);
        RelativeLayout notifCount = (RelativeLayout) MenuItemCompat.getActionView(item);

        tv = (TextView) notifCount.findViewById(R.id.actionbar_notifcation_textview);
        checkNotification();

        return super.onCreateOptionsMenu(menu);
        /*MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search,menu);
        MenuItem item = menu.findItem(R.id.action_favorite);
        SearchView searchView = (SearchView)item.getActionView();
        searchView.setQueryHint("Find Friends...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //friends.setAdapter(adapterForListView);
                //myCurrentFriends.remove(myFireBaseAuth.getCurrentUser().getEmail().toString());
                //adapterForListView.getFilter().filter(s);
                return false;
            }

        });*/
    }
    public static synchronized ProfilePage getInstance() {
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case com.listmylife.avita.listmylife.R.id.action_settings:
                Intent intent = new Intent(this,Settings.class);
                startActivity(intent);
               // Toast.makeText(this, "Refresh selected", Toast.LENGTH_SHORT)
                        //.show();
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
                        //.show();
                return true;
            case com.listmylife.avita.listmylife.R.id.messages:
                incomingFriendRequest();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    private void searchBar() {


    }

    private void faq() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(ProfilePage.this);
        builderSingle.setTitle("FAQ");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ProfilePage.this, android.R.layout.select_dialog_singlechoice);
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
                AlertDialog.Builder builderInner = new AlertDialog.Builder(ProfilePage.this);
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




    public void goToHome(View view) {

        Intent intent = new Intent(this, MainActivity.class);
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




    public void goToMessages() {
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


    public void checkValues(View view) {
        //Toast.makeText(getApplicationContext(),"Clicked",Toast.LENGTH_LONG).show();
        incomingFriendRequest();
    }
    public void checkNotification()
    {
        Log.d("HELLOHERE","I AM HERE");
        final ArrayList getValues = new ArrayList();
        tv.setVisibility(View.INVISIBLE);
        databaseReferenceFriends.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                ArrayList getValues = new ArrayList();
                ArrayList friendToBeRequested = new ArrayList();
                ArrayList requestor = new ArrayList();
                int count=0;
                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    HashMap getFriends = (HashMap) snapshot.getValue();
                    ArrayList values = new ArrayList();

                    //Toast.makeText(getApplicationContext(), getFriends.values().toString(), Toast.LENGTH_LONG).show();
                    Iterator iterator = getFriends.values().iterator();
                    while(iterator.hasNext())
                    {
                        String getValue = iterator.next().toString();
                        //Toast.makeText(getApplicationContext(),getValue +"\n" + myFireBaseAuth.getCurrentUser().getEmail().toString(),Toast.LENGTH_LONG).show();
                        if(getValue.equals(myFireBaseAuth.getCurrentUser().getEmail().toString())) {
                            //String getResponse = iterator.next().toString();
                            count++;
                            iterator.next();

                        }
                        else {
                            iterator.next();
                        }
                    }
                    Log.d("HELLOHERE",Integer.toString(count));
                    if(count>9)
                    {
                        //tv.setText(Integer.toString(count));
                        tv.setVisibility(View.VISIBLE);
                        tv.setText("9+");

                    }
                    else if(count>0 && count<=9)
                    {
                        tv.setVisibility(View.VISIBLE);
                        tv.setText(Integer.toString(count));
                    }
                    else if(count==0){
                        tv.setVisibility(View.INVISIBLE);
                    }



                }
                //Toast.makeText(getApplicationContext(),requestor.toString(),Toast.LENGTH_LONG).show();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}
