package com.listmylife.avita.listmylife;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VistListValuesPrivate extends AppCompatActivity {
    FirebaseAuth myFireBaseAuth;
    DatabaseReference databaseReference;
    DatabaseReference databaseReferenceLikes;
    DatabaseReference getDatabaseReferencedislikes;
    DatabaseReference getDatabaseReferenceLikes;
    String first_value;
    String second_value;
    ArrayList<String> room_list = new ArrayList<>();
    ArrayList get_values;
    ListView listViewExpanded;
    ArrayAdapter<String> arrayAdapter;
    boolean thumbsUp=false;
    boolean thumbsDown=false;
    DatabaseReference root;
    ImageView thumbUpImage;
    ImageView thumbDownImage;
    String keys;
    TextView likesAndDislikes;

    TextView like;
    TextView dislike;
    TextView time;
    TextView itemsFor;

    String getDescription;
    String getTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.listmylife.avita.listmylife.R.layout.activity_vist_list_values_private);

        time = (TextView) findViewById(com.listmylife.avita.listmylife.R.id.textView11);
        itemsFor = (TextView) findViewById(com.listmylife.avita.listmylife.R.id.textViewItemsFor);
        itemsFor.setText("Items for: " + getIntent().getExtras().get("list_name").toString());
        //getSupportActionBar().setTitle("Items for: " + getIntent().getExtras().get("list_name").toString());
        getSupportActionBar().setTitle("Private Lists");
        get_values = new ArrayList();
        myFireBaseAuth = FirebaseAuth.getInstance();
        if(myFireBaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        pushLoginInfoYes();
        listViewExpanded = (ListView) findViewById(com.listmylife.avita.listmylife.R.id.listViewExpanded);
        arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                room_list);
        listViewExpanded.setAdapter(arrayAdapter);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("zxcLists").child(getIntent().getExtras().get("list_name2").toString());

        //thumbUpImage = (ImageView) findViewById(R.id.imageViewUp);
        //thumbDownImage = (ImageView) findViewById(R.id.imageViewDown);

        like = (TextView) findViewById(com.listmylife.avita.listmylife.R.id.textViewLike);
        dislike = (TextView) findViewById(com.listmylife.avita.listmylife.R.id.textViewDislike);
        likesAndDislikes = (TextView) findViewById(com.listmylife.avita.listmylife.R.id.textViewSetLikes);
        getValues();

    }
    Calendar c;
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

                        DatabaseReference message_root5 = databaseReference;
                        Map<String, Object> map6 = new HashMap<>();
                        map6.put("name", myFireBaseAuth.getCurrentUser().getEmail());
                        map6.put("notification", c.getTimeInMillis());
                        message_root5.updateChildren(map6);
                        time.setText(date_time+" "+ finalHour + ":" + finalMinute + " " + amOrPM);

                        DatabaseReference message_root50 = databaseReference;
                        Map<String, Object> map60 = new HashMap<>();
                        map60.put("name", myFireBaseAuth.getCurrentUser().getEmail());
                        map60.put("notification_time", time.getText().toString());
                        message_root50.updateChildren(map60);

                        //Cancel Old Notification
                        Intent intent = new Intent(getApplicationContext(), Notification_Reciever.class);
                        PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), getDescription.hashCode()+getTitle.toString().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.cancel(sender);

                        //Set up new notification
                        intent.putExtra("value", getTitle);
                        intent.putExtra("title", getTitle);
                        intent.putExtra("message", get_values.toString());
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), getTitle.hashCode() + getDescription.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager alarmManager2 = (AlarmManager) getSystemService(ALARM_SERVICE);
                        //Log.d("NO_NOTIFICATION", Integer.toString(title.getText().toString().hashCode() + description.toString().hashCode()));
                        alarmManager2.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
                        Log.d("NO_NOTIFICATION", getTitle);
                        Log.d("NO_NOTIFICATION", get_values.toString());
                        Intent getIntent = getIntent();
                        startActivity(getIntent);
                        finish();
                        Toast.makeText(getApplicationContext(), "New Notification Time Created", Toast.LENGTH_LONG);


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
    public void getValues()
    {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                DataSnapshot previous = null;
                ArrayList s  = new ArrayList();
                ArrayList s2 = new ArrayList();

                while(i.hasNext())
                {
                    previous = (DataSnapshot)i.next();
                    Log.d("OUT_TESTING",previous.toString());

                    if(previous.getKey().equals("item"))
                    {
                        ArrayList values = (ArrayList) previous.getValue();
                        for(int j=0; j<values.size();j++)
                        {
                            s.add(values.get(j));
                            s2.add(values.get(j));
                        }
                    }

                    else if(previous.getKey().equals("description"))
                    {
                        /*s.add(0,"Author " + myFireBaseAuth.getCurrentUser().getEmail().toString() +
                                "\nList Description: " + previous.getValue().toString());*/
                        getDescription = previous.getValue().toString();
                    }
                    else if(previous.getKey().equals("notification_time"))
                    {
                        if(previous.getValue().equals("No Notification Set"))
                        {
                            time.setText("No Notification Set");
                        }
                        else
                        {
                            time.setText(previous.getValue().toString());

                        }
                    }
                    else if(previous.getKey().equals("title"))
                    {
                        getTitle = previous.getValue().toString();
                    }

                }
                room_list.clear();

                room_list.addAll(s);

                get_values.clear();
                get_values.addAll(s2);

                arrayAdapter.notifyDataSetChanged();
                /*while(i.hasNext())
                {
                    previous = (DataSnapshot)i.next();
                    Log.d("TESTING_EXTRA_VALUES", previous.getValue().toString());
                    Object value = (previous.getValue());

                    if(value instanceof ArrayList) {

                        ArrayList<Object> values = (ArrayList<Object>) value;
                        for(int j=0;j<values.size();j++)
                        {
                            s.add(values.get(j));
                        }
                    }
                    else
                    {
                        s.add(0,"Author " + value.toString() + "\nList Title: "+
                                getIntent().getExtras().get("list_name").toString());
                        //Toast.makeText(getApplicationContext(),value.toString(),Toast.LENGTH_LONG).show();
                    }

                }
                room_list.clear();

                room_list.addAll(s);

                arrayAdapter.notifyDataSetChanged();*/


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateLikes(final String title)
    {
        root = FirebaseDatabase.getInstance().getReference().child("zxcLikes");
        Map<String,Object> map = new HashMap<>();

        keys = root.push().getKey();

        root.updateChildren(map);


        DatabaseReference message_root  = root.child(title);

        Map<String,Object> map2 = new HashMap<>();

        map2.put("likes", myFireBaseAuth.getCurrentUser().getEmail());
        map2.put("count", 1);
        String email = myFireBaseAuth.getCurrentUser().getEmail().toString();
        message_root.child(email.replace('.',' ')).updateChildren(map2);
    }
    public void updateDislikes(String title)
    {
        root = FirebaseDatabase.getInstance().getReference().child("zxcLikes");
        Map<String,Object> map = new HashMap<>();

        keys = root.push().getKey();

        root.updateChildren(map);


        DatabaseReference message_root  = root.child(title);

        Map<String,Object> map2 = new HashMap<>();

        map2.put("likes", myFireBaseAuth.getCurrentUser().getEmail());
        map2.put("count", 0);
        String email = myFireBaseAuth.getCurrentUser().getEmail().toString();
        message_root.child(email.replace('.',' ')).updateChildren(map2);
    }



    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.




        // etc.

        super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

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
                //.show();
                finish();
                return true;

            case com.listmylife.avita.listmylife.R.id.action_favorite:
                refreshPage();
                //Toast.makeText(this, "Refresh selected", Toast.LENGTH_SHORT)
                //.show();
                return true;

            case com.listmylife.avita.listmylife.R.id.new_list:
                new_list_selected();
                //Toast.makeText(this, "New List Selected", Toast.LENGTH_SHORT)
                //.show();
                return true;
            case com.listmylife.avita.listmylife.R.id.logout:
                pushLoginInfoNo();
                logout();
                OneSignal.setSubscription(false);
                //Toast.makeText(this, "Logout Selected", Toast.LENGTH_SHORT)
                //.show();
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


    private void faq() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(VistListValuesPrivate.this);
        builderSingle.setTitle("FAQ");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(VistListValuesPrivate.this, android.R.layout.select_dialog_singlechoice);
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
                AlertDialog.Builder builderInner = new AlertDialog.Builder(VistListValuesPrivate.this);
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
    public void goToMessages(View view) {
        Intent intent = new Intent(this, Messages_SplashScreen.class);
        startActivity(intent);
        finish();
        //Toast.makeText(this, "Go To Messages", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Private_Lists.class);
        startActivity(intent);
        finish();
    }

    public void deleteList(View view) {
        final ArrayList publicList = (ArrayList) getIntent().getExtras().get("list_public");
        final ArrayList getDescriptions = (ArrayList) getIntent().getExtras().get("list_description");
        final int position =  getIntent().getExtras().getInt("position");
        //Toast.makeText(getApplicationContext(),"Delete List",Toast.LENGTH_LONG).show();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                VistListValuesPrivate.this );

        // set title

        // set dialog message
        alertDialogBuilder
                .setTitle("Confirmation")
                .setMessage("Do you wish to delete list")
                .setCancelable(true)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLists");
                        Query applesQuery = databaseReference2.orderByChild("title").equalTo(publicList.get(position).toString());
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
                                            //Toast.makeText(getApplicationContext(),"DELETED",Toast.LENGTH_LONG).show();
                                            Log.d("NO_NOTIFICATION", Integer.toString(publicList.get(position).toString().hashCode()+getDescriptions.get(position).toString().hashCode()));
                                        }catch (Exception e)
                                        {
                                            Log.d("NO_NOTIFICATION","NO NOTIFICATION SET");
                                        }

                                        Intent intent = new Intent(getApplicationContext(),Private_Lists.class);
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
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                                      @Override
                                      public void onShow(DialogInterface arg0) {
                                          alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
                                          alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);

                                      }
                                  });
        alertDialog.show();


    }

    public void alterDateTime(View view) {
        datePicker();
    }
}
