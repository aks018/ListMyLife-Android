package com.listmylife.avita.listmylife;

import android.app.AlarmManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class New_List extends AppCompatActivity {
    ListView listView;

    CheckBox publicBox;

    CheckBox privateBox;


    BottomNavigationView navigationView;


    boolean checkPublic;
    boolean checkPrivate;
    ScrollView scrollView;

    EditText title;
    EditText listViewItem;
    ArrayList listViewItemsValues;
    New_List_Adapter arrayAdapter;
    FirebaseAuth myFireBaseAuth;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLists");
    DatabaseReference getDescriptions;
    DatabaseReference root;
    Calendar c;
    DatabaseReference databaseReferenceNewFriends = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcFriends");
    DatabaseReference databaseReferenceLoginWaitForList = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLoginList");

    ArrayList myCurrentFriends;
    ImageView newDateTime;
    TextView newTimeDate;
    EditText getDescriptionsFromUser;

    TextView mTextView;
    TextView noListValues;

    String keys;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.listmylife.avita.listmylife.R.layout.activity_new__list);

        //getSupportActionBar().setTitle("New List");
        setTitle("New List");


        mTextView = (TextView) findViewById(com.listmylife.avita.listmylife.R.id.mTextView);
        mTextView.setText("Characters Left: 60 ");
        getDescriptionsFromUser = (EditText) findViewById(com.listmylife.avita.listmylife.R.id.editText5);
        getUserNamesAndEmails = new HashMap();
        getAllUserNames();
        getDescriptionsFromUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
            @Override
            public void onTextChanged(CharSequence s, int i, int i2, int i3) {
                if(60 - s.length() < 0)
                {
                    mTextView.setText(String.valueOf(60 - s.length()) + " ");
                    mTextView.setTextColor(Color.RED);
                }
                else {
                    mTextView.setText("Characters Left: " + String.valueOf(60 - s.length()) + " ");
                    mTextView.setTextColor(Color.BLACK);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (null != getDescriptionsFromUser.getLayout() && getDescriptionsFromUser.getLayout().getLineCount() > 3) {
                    getDescriptionsFromUser.getText().delete(getDescriptionsFromUser.getText().length() - 1, getDescriptionsFromUser.getText().length());
                }
            }
        });

        publicBox = (CheckBox) findViewById(com.listmylife.avita.listmylife.R.id.checkBoxPublic);
        privateBox = (CheckBox) findViewById(com.listmylife.avita.listmylife.R.id.checkBoxPrivate);
        myFireBaseAuth = FirebaseAuth.getInstance();
        if(myFireBaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        scrollView = (ScrollView) findViewById(com.listmylife.avita.listmylife.R.id.scrollView2);

        newDateTime = (ImageView) findViewById(com.listmylife.avita.listmylife.R.id.buttonSetNotificationTime);
        newDateTime.setVisibility(View.INVISIBLE);

        newTimeDate = (TextView) findViewById(com.listmylife.avita.listmylife.R.id.textViewDateTime);
        newTimeDate.setVisibility(View.INVISIBLE);

        checkPublic = false;
        checkPrivate = false;

        title = (EditText) findViewById(com.listmylife.avita.listmylife.R.id.editTextTitle);
        listViewItem = (EditText) findViewById(com.listmylife.avita.listmylife.R.id.editTextListItem);

        listView = (ListView) findViewById(com.listmylife.avita.listmylife.R.id.listView);

        myCurrentFriends = new ArrayList();
        listViewItemsValues = new ArrayList();

        pushLoginInfoYes();
        publicBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    //Toast.makeText(getApplicationContext(), "Public Now Checked", Toast.LENGTH_LONG).show();
                    if(publicBox.getText().equals("Set Notification"))
                    {
                        newDateTime.setVisibility(View.VISIBLE);
                        newTimeDate.setVisibility(View.VISIBLE);

                    }
                    else
                    {
                        newDateTime.setVisibility(View.INVISIBLE);
                        newTimeDate.setVisibility(View.INVISIBLE);
                        newTimeDate.setText("No Date Set");
                    }

                }
                else
                {
                    if(privateBox.isChecked())
                    {
                        publicBox.setText("Set Notification");

                    }
                    newDateTime.setVisibility(View.INVISIBLE);
                    newTimeDate.setVisibility(View.INVISIBLE);

                    newTimeDate.setText("No Date Set");
                }
            }
        });
        privateBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    //Toast.makeText(getApplicationContext(), "Private Now Checked", Toast.LENGTH_LONG).show();
                    publicBox.setText("Set Notification");
                    if(publicBox.isChecked())
                    {
                        newDateTime.setVisibility(View.VISIBLE);
                        newTimeDate.setVisibility(View.VISIBLE);

                    }


                }
                else
                {
                    //Toast.makeText(getApplicationContext(), "Private Not Checked", Toast.LENGTH_LONG).show();
                    publicBox.setText("Public");
                    newDateTime.setVisibility(View.INVISIBLE);
                    newTimeDate.setVisibility(View.INVISIBLE);
                    newTimeDate.setText("No Date Set");
                }
            }
        });
        navigationView = (BottomNavigationView) findViewById(com.listmylife.avita.listmylife.R.id.navigation2);
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
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case com.listmylife.avita.listmylife.R.id.menu_top:
                        scrollView.fullScroll(ScrollView.FOCUS_UP);
                        return true;
                    case com.listmylife.avita.listmylife.R.id.menu_submit:
                        submitList();

                        return true;
                    case com.listmylife.avita.listmylife.R.id.menu_clear:
                        clearList();
                        return true;
                }
                return true;
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
    public void clearList()
    {
        arrayAdapter = new New_List_Adapter(this,listViewItemsValues);
        listViewItemsValues.clear();
        arrayAdapter.notifyDataSetChanged();
        Utility.setListViewHeightBasedOnChildren(listView);
        scrollMyListViewToBottom();
    }

    public void publicOnClick(View view) {
        publicBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    //Toast.makeText(getApplicationContext(), "Public Now Checked", Toast.LENGTH_LONG).show();
                    if(publicBox.getText().equals("Set Notification"))
                    {
                        newDateTime.setVisibility(View.VISIBLE);
                        newTimeDate.setVisibility(View.VISIBLE);

                    }
                    else
                    {
                        newDateTime.setVisibility(View.INVISIBLE);
                        newTimeDate.setVisibility(View.INVISIBLE);
                        newTimeDate.setText("No Date Set");
                    }

                }
                else
                {
                    if(privateBox.isChecked())
                    {
                        publicBox.setText("Set Notification");

                    }
                    newDateTime.setVisibility(View.INVISIBLE);
                    newTimeDate.setVisibility(View.INVISIBLE);

                    newTimeDate.setText("No Date Set");
                }
            }
        });
    }

    public void privateOnClick(View view) {

        privateBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    //Toast.makeText(getApplicationContext(), "Private Now Checked", Toast.LENGTH_LONG).show();
                    publicBox.setText("Set Notification");
                    if(publicBox.isChecked())
                    {
                        newDateTime.setVisibility(View.VISIBLE);
                        newTimeDate.setVisibility(View.VISIBLE);

                    }


                }
                else
                {
                    //Toast.makeText(getApplicationContext(), "Private Not Checked", Toast.LENGTH_LONG).show();
                    publicBox.setText("Public");
                    newDateTime.setVisibility(View.INVISIBLE);
                    newTimeDate.setVisibility(View.INVISIBLE);
                    newTimeDate.setText("No Date Set");
                }
            }
        });
    }

    public void goToMyGroups(View view) {
       // Toast.makeText(this,"My Groups",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();

    }

    public void goToHome(View view) {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void submitList() {
        final String description = getDescriptionsFromUser.getText().toString().trim();
        if(description.trim().length()<1)
        {
            Toast.makeText(getApplicationContext(),"List Must Have A Description", Toast.LENGTH_LONG).show();
            return;
        }
        if(description.length()>60)
        {
            Toast.makeText(getApplicationContext(),"Description Must Be Less Than 60 Characters", Toast.LENGTH_LONG).show();
            return;
        }

        if(publicBox.isChecked() && privateBox.isChecked() && publicBox.getText().equals("Public"))
        {
            Toast.makeText(getApplicationContext(), "List cannot be private and public", Toast.LENGTH_LONG).show();
            return;
        }
        if(!publicBox.isChecked() && !privateBox.isChecked())
        {
            Toast.makeText(getApplicationContext(), "List has to be private or public", Toast.LENGTH_LONG).show();
            return;
        }
        if(publicBox.getText().equals("Set Notification") && !privateBox.isChecked())
        {
            Toast.makeText(getApplicationContext(), "List must be private to set notifications", Toast.LENGTH_LONG).show();
            return;
        }
        if(listViewItemsValues.size()==0)
        {
            Toast.makeText(getApplicationContext(), "List cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }
        final String titleValue = title.getText().toString();
        if (TextUtils.isEmpty(titleValue)) {
            Toast.makeText(getApplicationContext(), "Title cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            if (privateBox.isChecked() && !publicBox.isChecked()) {
                root = FirebaseDatabase.getInstance().getReference().child("zxcLists");


                DatabaseReference d = root.push();


                DatabaseReference message_root = root.child(d.getKey());

                Map<String, Object> map2 = new HashMap<>();

                map2.put("name", myFireBaseAuth.getCurrentUser().getEmail());
                map2.put("item", listViewItemsValues);

                message_root.updateChildren(map2);
                //NOW WE WILL PUT IN DESCRIPTION
            /*getDescriptions = FirebaseDatabase.getInstance().getReference().child("zxcDescriptions");

            Map<String, Object> map10 = new HashMap<>();
            keys = getDescriptions.push().getKey();
            getDescriptions.updateChildren(map10);*/

                DatabaseReference message_root99 = root.child(d.getKey());
                Map<String, Object> map99 = new HashMap<>();
                map99.put("name", myFireBaseAuth.getCurrentUser().getEmail());
                map99.put("notification_time","No Notification Set");
                message_root99.updateChildren(map99);


                DatabaseReference message_root2 = root.child(d.getKey());

                Map<String, Object> map11 = new HashMap<>();
                map11.put("name", myFireBaseAuth.getCurrentUser().getEmail());
                map11.put("description", description);

                DatabaseReference message_root3 = root.child(d.getKey());

                Map<String, Object> mapTime = new HashMap<>();
                mapTime.put("name", myFireBaseAuth.getCurrentUser().getEmail());

                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

// textView is the TextView view that should display it
                Log.d("TESTING_DATE", currentDateTimeString);
                mapTime.put("time", currentDateTimeString);


                message_root2.updateChildren(map11);

                message_root3.updateChildren(mapTime);

                DatabaseReference message_root4 = root.child(d.getKey());

                Map<String, Object> map = new HashMap<>();
                map.put("name", myFireBaseAuth.getCurrentUser().getEmail());
                map.put("title", titleValue);


                message_root4.updateChildren(map);

                DatabaseReference message_root5 = root.child(d.getKey());
                Map<String, Object> map6 = new HashMap<>();
                map6.put("name", myFireBaseAuth.getCurrentUser().getEmail());
                map6.put("notification", "No Notification Set");
                message_root5.updateChildren(map6);

                //createLikes(titleValue);
                //createDislikes(titleValue);
                //createLikes(titleValue);
                if (privateBox.isChecked()) {
                    publicOrPrivate(titleValue, false, d);
                } else {
                    publicOrPrivate(titleValue, true, d);
                }

                Log.d("TESTING_VALUES", "LIST_CREATED");


                Intent intent2 = new Intent(getApplicationContext(), Private_Lists.class);
                startActivity(intent2);
                finish();
                return;
            }
            if (privateBox.isChecked() && publicBox.getText().equals("Set Notification") && publicBox.isChecked()) {
                Log.d("TESTING_VALUES", "WANTS NOTIFICATIONS");
                if(newTimeDate.getText().equals("No Date Set"))
                {
                    Toast.makeText(getApplicationContext(),"You Need To Select A Date and Time", Toast.LENGTH_SHORT).show();
                    return;
                }


                Intent intent = new Intent(getApplicationContext(), Notification_Reciever.class);
                intent.putExtra("value", title.getText().toString());
                intent.putExtra("title", title.getText().toString());
                intent.putExtra("message", listViewItemsValues.toString());
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), title.getText().toString().hashCode() + description.toString().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Log.d("NO_NOTIFICATION", Integer.toString(title.getText().toString().hashCode() + description.toString().hashCode()));
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);


                Toast.makeText(getApplicationContext(), "Notification Set and List Created", Toast.LENGTH_LONG).show();


                root = FirebaseDatabase.getInstance().getReference().child("zxcLists");

                //newTimeDate
                DatabaseReference d = root.push();

                DatabaseReference message_root5 = root.child(d.getKey());
                Map<String, Object> map6 = new HashMap<>();
                map6.put("name", myFireBaseAuth.getCurrentUser().getEmail());
                map6.put("notification", c.getTimeInMillis());
                message_root5.updateChildren(map6);


                DatabaseReference message_root99 = root.child(d.getKey());
                Map<String, Object> map99 = new HashMap<>();
                map99.put("name", myFireBaseAuth.getCurrentUser().getEmail());
                map99.put("notification_time",newTimeDate.getText());
                message_root99.updateChildren(map99);

                DatabaseReference message_root = root.child(d.getKey());

                Map<String, Object> map2 = new HashMap<>();

                map2.put("name", myFireBaseAuth.getCurrentUser().getEmail());
                map2.put("item", listViewItemsValues);

                message_root.updateChildren(map2);
                //NOW WE WILL PUT IN DESCRIPTION
            /*getDescriptions = FirebaseDatabase.getInstance().getReference().child("zxcDescriptions");

            Map<String, Object> map10 = new HashMap<>();
            keys = getDescriptions.push().getKey();
            getDescriptions.updateChildren(map10);*/
                DatabaseReference message_root2 = root.child(d.getKey());

                Map<String, Object> map11 = new HashMap<>();
                map11.put("name", myFireBaseAuth.getCurrentUser().getEmail());
                map11.put("description", description);

                DatabaseReference message_root3 = root.child(d.getKey());

                Map<String, Object> mapTime = new HashMap<>();
                mapTime.put("name", myFireBaseAuth.getCurrentUser().getEmail());

                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

// textView is the TextView view that should display it
                Log.d("TESTING_DATE", currentDateTimeString);
                mapTime.put("time", currentDateTimeString);


                message_root2.updateChildren(map11);

                message_root3.updateChildren(mapTime);

                DatabaseReference message_root4 = root.child(d.getKey());

                Map<String, Object> map = new HashMap<>();
                map.put("name", myFireBaseAuth.getCurrentUser().getEmail());
                map.put("title", titleValue);

                message_root4.updateChildren(map);
                //createLikes(titleValue);
                //createDislikes(titleValue);
                //createLikes(titleValue);
                if (privateBox.isChecked()) {
                    publicOrPrivate(titleValue, false, d);
                } else {
                    publicOrPrivate(titleValue, true, d);
                }

                Log.d("TESTING_VALUES", "LIST_CREATED");


                Intent intent2 = new Intent(getApplicationContext(), Private_Lists.class);
                startActivity(intent2);
                finish();
                return;
            }
            //NOW WE ARE CREATING A PUBLIC LIST
            final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setTitle("Choose Friends To Add To List");
            builder.setCancelable(true);
            databaseReferenceNewFriends.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterator i = dataSnapshot.getChildren().iterator();
                    ArrayList getValues = new ArrayList();
                    while (i.hasNext()) {
                        DataSnapshot snapshot = ((DataSnapshot) i.next());
                        //Toast.makeText(getApplicationContext(),snapshot.child("user").getValue().toString(),Toast.LENGTH_LONG).show();

                        if (snapshot.child("user").getValue().toString().equals(myFireBaseAuth.getCurrentUser().getEmail().toString())) {
                            //getUserNamesAndEmails.get(snapshot.child("friend").getValue().toString());
                            myCurrentFriends.add(getUserNamesAndEmails.get(snapshot.child("friend").getValue().toString()));
                        }
                    }
                    final boolean[] booleanValues = new boolean[myCurrentFriends.size()];
                    for (int j = 0; j < myCurrentFriends.size(); j++) {
                        booleanValues[j] = false;
                    }
                    final String[] friendsArray = new String[myCurrentFriends.size()];
                    for (int j = 0; j < myCurrentFriends.size(); j++) {
                        friendsArray[j] = myCurrentFriends.get(j).toString();
                    }
                    if (myCurrentFriends.size() == 0) {
                        builder.setMessage("You must be friends with someone to make a public chat");
                    }
                    builder.setMultiChoiceItems(friendsArray, booleanValues, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            booleanValues[which] = isChecked;
                            String currentItem = myCurrentFriends.get(which).toString();

                            //Toast.makeText(getApplicationContext(),
                            // currentItem + " " + isChecked, Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do something when click positive button
                            ArrayList a = new ArrayList();
                            for (int i = 0; i < booleanValues.length; i++) {
                                boolean checked = booleanValues[i];
                                if (checked) {
                                    for (int j = 0; j< myCurrentFriends.size(); j++) {
                                        for (Map.Entry<String, String> entry : getUserNamesAndEmails.entrySet()) {
                                            if (entry.getValue().equals(myCurrentFriends.get(i).toString())) {
                                                a.add(entry.getKey());
                                            }
                                        }
                                    }
                                    //a.add(myCurrentFriends.get(i));
                                }
                            }

                            a.add(myFireBaseAuth.getCurrentUser().getEmail());
                            if (a.size() == 1) {
                                Toast.makeText(getApplicationContext(), "Must be at least one other user in list room", Toast.LENGTH_LONG).show();
                                return;
                            }

                            root = FirebaseDatabase.getInstance().getReference().child("zxcLists");


                            DatabaseReference d = root.push();
                            DatabaseReference message_root5 = root.child(d.getKey());
                            Map<String, Object> mapUsers = new HashMap<>();
                            mapUsers.put("name", myFireBaseAuth.getCurrentUser().getEmail().toString());
                            mapUsers.put("users", a);
                            message_root5.updateChildren(mapUsers);


                            DatabaseReference message_root = root.child(d.getKey());

                            Map<String, Object> map2 = new HashMap<>();

                            map2.put("name", myFireBaseAuth.getCurrentUser().getEmail());
                            map2.put("item", listViewItemsValues);

                            message_root.updateChildren(map2);
                            DatabaseReference message_root2 = root.child(d.getKey());

                            Map<String, Object> map11 = new HashMap<>();
                            map11.put("name", myFireBaseAuth.getCurrentUser().getEmail());
                            map11.put("description", description);

                            DatabaseReference message_root3 = root.child(d.getKey());

                            Map<String, Object> mapTime = new HashMap<>();
                            mapTime.put("name", myFireBaseAuth.getCurrentUser().getEmail());

                            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

// textView is the TextView view that should display it
                            Log.d("TESTING_DATE", currentDateTimeString);
                            mapTime.put("time", currentDateTimeString);


                            message_root2.updateChildren(map11);

                            message_root3.updateChildren(mapTime);

                            DatabaseReference message_root4 = root.child(d.getKey());

                            Map<String, Object> map = new HashMap<>();
                            map.put("name", myFireBaseAuth.getCurrentUser().getEmail().toString());
                            map.put("title", titleValue);

                            message_root4.updateChildren(map);





                            //createLikes(titleValue);
                            //createDislikes(titleValue);
                            //createLikes(titleValue);
                            if (privateBox.isChecked()) {
                                publicOrPrivate(titleValue, false, d);
                            } else {
                                publicOrPrivate(titleValue, true, d);
                            }

                            Log.d("TESTING_VALUES", "LIST_CREATED");

                            //NOW WE NEED TO CREATE THE CHAT....
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcChat");
                            DatabaseReference d2 = databaseReference.push();
                            DatabaseReference message_root20 = databaseReference.child(d2.getKey());
                            Map<String, Object> mapUsers2 = new HashMap<>();
                            mapUsers2.put("name", titleValue);
                            mapUsers2.put("users", a);
                            message_root20.updateChildren(mapUsers2);

                            DatabaseReference message_root21 = databaseReference.child(d2.getKey());
                            Map<String, Object> mapUsers3 = new HashMap<>();
                            mapUsers3.put("name", titleValue);
                            String currentDateTimeString2 = DateFormat.getDateTimeInstance().format(new Date());
                            mapUsers3.put("time", currentDateTimeString2);
                            message_root21.updateChildren(mapUsers3);


                            DatabaseReference message_root22 = databaseReference.child(d2.getKey());
                            Map<String, Object> mapUsers4 = new HashMap<>();
                            mapUsers4.put("name", titleValue);
                            mapUsers4.put("nochatname", "");
                            message_root22.updateChildren(mapUsers4);

                            DatabaseReference message_root28 = databaseReference.child(d2.getKey());
                            Map<String, Object> mapUsers8 = new HashMap<>();
                            mapUsers8.put("name", titleValue);
                            mapUsers8.put("last_message_name", "");
                            message_root28.updateChildren(mapUsers8);

                            DatabaseReference message_root29 = databaseReference.child(d2.getKey());
                            Map<String, Object> mapUsers9 = new HashMap<>();
                            mapUsers9.put("name", titleValue);
                            mapUsers9.put("last_message_time",  DateFormat.getDateTimeInstance().format(new Date()));
                            message_root29.updateChildren(mapUsers9);

                            DatabaseReference message_root30 = databaseReference.child(d2.getKey());
                            Map<String, Object> mapUsers10 = new HashMap<>();
                            mapUsers10.put("name", titleValue);
                            mapUsers10.put("last_message", "No Last Message");
                            message_root30.updateChildren(mapUsers10);

                            DatabaseReference message_root31 = databaseReference.child(d2.getKey());
                            Map<String, Object> mapUsers100 = new HashMap<>();
                            mapUsers100.put("name", titleValue);
                            mapUsers100.put("last_message_time_seconds",   System.currentTimeMillis());
                            message_root31.updateChildren(mapUsers100);

                            DatabaseReference message_root32 = databaseReference.child(d2.getKey());
                            Map<String, Object> mapUsers101 = new HashMap<>();
                            mapUsers101.put("name", titleValue);
                            mapUsers101.put("has_list_attached",  "yes");
                            message_root32.updateChildren(mapUsers101);


                            DatabaseReference message_rootChat = root.child(d.getKey());

                            Map<String, Object> mapChat = new HashMap<>();
                            mapChat.put("name", myFireBaseAuth.getCurrentUser().toString());
                            mapChat.put("chatValue", d2.getKey().toString());

                            message_rootChat.updateChildren(mapChat);




                            Toast.makeText(getApplicationContext(), "List and Chat Added", Toast.LENGTH_LONG).show();


                            Log.d("TESTING_VALUES", "LIST_CREATED");
                            Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent2);
                            checkIfLogin(a);


                        }
                    });
                    AlertDialog alert11 = builder.create();
                    alert11.show();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
        }
        catch(Exception e)
        {
            Toast.makeText(getApplicationContext(),"Unable to Add List", Toast.LENGTH_SHORT).show();
        }


    }
    HashMap<String,String> getUserNamesAndEmails;
    DatabaseReference databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcUsers");

    public void getAllUserNames()
    {
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


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    int mYear;
    int mMonth;
    int mDay;
    int mHour;
    int mMinute;
    String date_time;
    private void tiemPicker(){
        // Get Current Time
        c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {


                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,int minute) {

                        mHour = hourOfDay;
                        mMinute = minute;
                        String amOrPM="PM";
                        if(mHour<12)
                        {
                            amOrPM = "AM";
                        }
                        if(mHour>12)
                        {
                            mHour = mHour-12;
                        }
                        String finalHour="";
                        String finalMinute="";
                        if(mHour<10)
                        {
                            String hour = Integer.toString(mHour);
                            finalHour = "0"+hour;
                        }
                        else if(mHour>=10)
                        {
                            finalHour = Integer.toString(mHour);
                        }
                        if(minute<10)
                        {
                            String minuteTime = Integer.toString(minute);
                             finalMinute = "0"+minuteTime;
                        }
                        else if(minute>=10)
                        {
                            finalMinute = Integer.toString(minute);
                        }
                        Log.d("TESTING_VALUES", date_time+" "+ finalHour + ":" + finalMinute + " " + amOrPM);
                        c.set(Calendar.HOUR,mHour);
                        c.set(Calendar.MINUTE, mMinute);
                        c.set(Calendar.SECOND, 3);
                        newTimeDate.setText(date_time+" "+ finalHour + ":" + finalMinute + " " + amOrPM);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }
    private void datePicker(){

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        date_time = (monthOfYear + 1) + "-" + dayOfMonth + "-" + year;
                        c.set(Calendar.YEAR, mYear);
                        c.set(Calendar.MONTH, mMonth);
                        c.set(Calendar.DAY_OF_MONTH, mDay);
                        //*************Call Time Picker Here ********************
                        tiemPicker();
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }
    public void publicOrPrivate(String titleValue,boolean isChecked, DatabaseReference d)
    {
        root = FirebaseDatabase.getInstance().getReference().child("zxcLists");
        DatabaseReference message_root2 = root.child(d.getKey());
        Map<String, Object> map11 = new HashMap<>();


        if(isChecked) {
            map11.put("name",myFireBaseAuth.getCurrentUser().getEmail());
            map11.put("public_or_private", "public");
        }
        else
        {
            map11.put("name",myFireBaseAuth.getCurrentUser().getEmail());
            map11.put("public_or_private", "private");
        }


        message_root2.updateChildren(map11);
    }
    public void createLikes(String title)
    {
        root = FirebaseDatabase.getInstance().getReference().child("zxcLikes");
        Map<String,Object> map = new HashMap<>();

        keys = root.push().getKey();

        root.updateChildren(map);


        DatabaseReference message_root  = root.child(title);

        Map<String,Object> map2 = new HashMap<>();

        map2.put("likes", myFireBaseAuth.getCurrentUser().getEmail());
        map2.put("count", 0);
        message_root.updateChildren(map2);
    }
    public void createDislikes(String title)
    {
        root = FirebaseDatabase.getInstance().getReference().child("zxcDislikes");
        Map<String,Object> map = new HashMap<>();

        keys = root.push().getKey();

        root.updateChildren(map);


        DatabaseReference message_root  = root.child(title);

        Map<String,Object> map2 = new HashMap<>();

        map2.put("likes", myFireBaseAuth.getCurrentUser().getEmail());
        map2.put("count", 0);
        message_root.updateChildren(map2);
    }






    private void faq() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(New_List.this);
        builderSingle.setTitle("FAQ");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(New_List.this, android.R.layout.select_dialog_singlechoice);
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
                AlertDialog.Builder builderInner = new AlertDialog.Builder(New_List.this);
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
                      //  .show();
                finish();
                return true;

            case com.listmylife.avita.listmylife.R.id.action_favorite:
                refreshPage();
                //Toast.makeText(this, "Refresh selected", Toast.LENGTH_SHORT)
                        //.show();
                return true;

            case com.listmylife.avita.listmylife.R.id.logout:
                pushLoginInfoNo();
                logout();
                OneSignal.setSubscription(false);
                return true;

            case com.listmylife.avita.listmylife.R.id.new_list:
                new_list_selected();
                //Toast.makeText(this, "New List Selected", Toast.LENGTH_SHORT)
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

    public void newItemAdded(View view) {

        String listItem = listViewItem.getText().toString();
        if(TextUtils.isEmpty(listItem.trim()))
        {
            Toast.makeText(getApplicationContext(), "Item cannot be empty ", Toast.LENGTH_LONG).show();
            return;
        }
        listViewItemsValues.add(listItem);
        arrayAdapter = new New_List_Adapter(this,listViewItemsValues);
        listView.setAdapter(arrayAdapter);
        Utility.setListViewHeightBasedOnChildren(listView);
        arrayAdapter.notifyDataSetChanged();
        scrollMyListViewToBottom();
        listViewItem.setText("");

    }

    public void goToMessages(View view) {
        Intent intent = new Intent(this, Messages_SplashScreen.class);
        startActivity(intent);
        finish();
       // Toast.makeText(getApplicationContext(), "Go To Messages ", Toast.LENGTH_LONG).show();
    }

    public void addNewDateTime(View view) {

        datePicker();

    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void scrollMyListViewToBottom() {
        listView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listView.setSelection(arrayAdapter.getCount() - 1);
            }
        });
    }

    public void deleteListItem(View view) {
        final Object removedItem = view.getTag();
        AlertDialog.Builder builder1 = new AlertDialog.Builder(New_List.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Do you wish to delete this list item?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listViewItemsValues.remove(removedItem.toString());
                        arrayAdapter.notifyDataSetChanged();
                        Utility.setListViewHeightBasedOnChildren(listView);
                        scrollMyListViewToBottom();
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        final AlertDialog alert11 = builder1.create();
        alert11.setOnShowListener( new DialogInterface.OnShowListener() {
                                      @Override
                                      public void onShow(DialogInterface arg0) {
                                          alert11.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
                                          alert11.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                                      }
                                  });
        alert11.show();

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

        databaseReferenceLoginWaitForList.push().setValue(notification);
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
                                + "\"contents\": {\"en\": \"You have been invited to a new list \n Go to your Public Lists to check it out\"}"
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
