package com.listmylife.avita.listmylife;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ViewListValues extends AppCompatActivity {
    FirebaseAuth myFireBaseAuth;
    DatabaseReference databaseReference;
    DatabaseReference databaseReferenceLikes;
    DatabaseReference getDatabaseReferencedislikes;
    DatabaseReference getDatabaseReferenceLikes;
    DatabaseReference databaseReferenceGettingValues;
    String first_value;
    String second_value;
    ArrayList<String> room_list = new ArrayList<>();
    ListView listViewExpanded;
    boolean thumbsUp=false;
    boolean thumbsDown=false;
    DatabaseReference root;
    ImageView thumbUpImage;
    ImageView thumbDownImage;
    String keys;
    ArrayList title;
    //TextView likesAndDislikes;
    DatabaseReference databaseReferencePublicOrPrivate;

    New_List_Adapter newListAdapter;

    EditText editTextListItem;

    TextView like;
    TextView dislike;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.listmylife.avita.listmylife.R.layout.activity_view_list_values);
        checkForInternet();
        //getSupportActionBar().setTitle("Items for: " + getIntent().getExtras().get("list_name").toString());
        getSupportActionBar().setTitle("Items for: " + getIntent().getExtras().get("list_name2").toString());
        myFireBaseAuth = FirebaseAuth.getInstance();
        if(myFireBaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        editTextListItem = (EditText) findViewById(com.listmylife.avita.listmylife.R.id.editTextListItem);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("zxcLists").child(getIntent().getExtras().get("list_name").toString());
        databaseReferencePublicOrPrivate = FirebaseDatabase.getInstance().getReference().child("zxcPublicOrPrivate").child(getIntent().getExtras().get("list_name").toString());
        databaseReferenceLikes = FirebaseDatabase.getInstance().getReference().child("zxcLikes").child(getIntent().getExtras().get("list_name").toString());
        getDatabaseReferencedislikes = FirebaseDatabase.getInstance().getReference().child("zxcDislikes").child(getIntent().getExtras().get("list_name").toString());
        getDatabaseReferenceLikes = FirebaseDatabase.getInstance().getReference().child("zxcLikes");
        //thumbUpImage = (ImageView) findViewById(R.id.imageViewUp);
        //thumbDownImage = (ImageView) findViewById(R.id.imageViewDown);

        like = (TextView) findViewById(com.listmylife.avita.listmylife.R.id.textViewLike);
        title= new ArrayList();
        dislike = (TextView) findViewById(com.listmylife.avita.listmylife.R.id.textViewDislike);
        //likesAndDislikes = (TextView) findViewById(R.id.textViewSetLikes);
        getLikesAndDislikes();
        getValues();
        pushLoginInfoYes();
        listViewExpanded = (ListView) findViewById(com.listmylife.avita.listmylife.R.id.listViewExpanded);
        listViewExpanded.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        newListAdapter = new New_List_Adapter(this,room_list);
        listViewExpanded.setAdapter(newListAdapter);
    }
    public void getLikesAndDislikes()
    {
        databaseReferenceLikes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                DataSnapshot previous = null;
                int total=0;
                int value=0;
                ArrayList s  = new ArrayList();
                while(i.hasNext()) {
                    previous = (DataSnapshot) i.next();

                    if((long) previous.child("count").getValue()==1)
                    {
                        value++;
                        if(previous.child("likes").getValue().toString().equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                        {
                            like.setTextColor(Color.BLUE);
                            Log.d("VIEWLIST", "HERE");
                        }
                    }
                    else
                    {
                        if(previous.child("likes").getValue().toString().equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                        {
                            dislike.setTextColor(Color.RED);
                            Log.d("VIEWLIST", "HERE");
                        }

                    }

                    total++;
                }

                //Toast.makeText(getApplicationContext(),s.toString(),Toast.LENGTH_LONG).show();
                like.setText(Integer.toString(value));
                dislike.setText(Integer.toString(total-value));
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
    public void getValues()
    {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                final ArrayList s  = new ArrayList();
                while(i.hasNext()) {
                    DataSnapshot snapshot = ((DataSnapshot) i.next());
                    Log.d("TESTING_VALUES", snapshot.toString());
                    if (snapshot.getKey().equals("item")) {

                        ArrayList getValues = (ArrayList) snapshot.getValue();

                        for (int j = 0; j < getValues.size(); j++) {
                            s.add(getValues.get(j));
                        }
                    }
                    if(snapshot.getKey().equals("title"))
                    {
                        title.add(snapshot.getValue());
                    }
                    String last="";
                    if (snapshot.getKey().equals("users")) {
                        /*ArrayList getValues2 = (ArrayList) snapshot.getValue();

                        last = getValues2.get(getValues2.size() - 1).toString();
                        s.add(0, "Author: " + last);*/

                    }


                }



                room_list.clear();

                room_list.addAll(s);

                //arrayAdapter.notifyDataSetChanged();
                newListAdapter.notifyDataSetChanged();


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
    public void thumbsUp(View view) {
        if(thumbsUp ==false && thumbsDown == false) {
            //Toast.makeText(this, "Thumbs Up", Toast.LENGTH_LONG).show();
            thumbsUp= true;
            like.setTextColor(Color.BLUE);
            updateLikes(getIntent().getExtras().get("list_name").toString());
        }

        if(thumbsUp==false && thumbsDown==true)
        {
            //Toast.makeText(this, "Thumbs Up", Toast.LENGTH_LONG).show();
            thumbsUp = true;
            thumbsDown=false;
            like.setTextColor(Color.BLUE);
            dislike.setTextColor(Color.GRAY);
            updateLikes(getIntent().getExtras().get("list_name").toString());
        }
        databaseReferenceLikes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                DataSnapshot previous = null;
                int total=0;
                int value=0;
                ArrayList s  = new ArrayList();
                while(i.hasNext()) {
                    previous = (DataSnapshot) i.next();

                    if((long) previous.child("count").getValue()==1)
                    {
                        if(previous.child("likes").toString().equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                        {
                            like.setTextColor(Color.BLUE);
                        }

                        value++;
                    }
                    else
                    {
                        if(previous.child("likes").getValue().toString().equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                        {
                            dislike.setTextColor(Color.RED);
                            Log.d("VIEWLIST", "HERE");
                        }

                    }

                    total++;
                }

                //Toast.makeText(getApplicationContext(),s.toString(),Toast.LENGTH_LONG).show();
                like.setText(Integer.toString(value));
                dislike.setText(Integer.toString(total-value));
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void thumbsDown(View view) {
        if(thumbsUp == false && thumbsDown==false) {
            //Toast.makeText(this, "Thumbs Down", Toast.LENGTH_LONG).show();
            thumbsDown =true;
            dislike.setTextColor(Color.RED);
            updateDislikes(getIntent().getExtras().get("list_name").toString());
        }
        if(thumbsUp == true && thumbsDown==false) {
            //Toast.makeText(this, "Thumbs Down", Toast.LENGTH_LONG).show();
            thumbsDown =true;
            thumbsUp = false;
            dislike.setTextColor(Color.RED);
            like.setTextColor(Color.GRAY);
            updateDislikes(getIntent().getExtras().get("list_name").toString());
        }
        databaseReferenceLikes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                DataSnapshot previous = null;
                int total=0;
                int value=0;
                ArrayList s  = new ArrayList();
                while(i.hasNext()) {
                    previous = (DataSnapshot) i.next();

                    if((long) previous.child("count").getValue()==1)
                    {
                        if(previous.child("likes").toString().equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                        {
                            like.setTextColor(Color.BLUE);
                        }
                        value++;
                    }
                    else
                    {
                        if(previous.child("likes").getValue().toString().equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
                        {
                            dislike.setTextColor(Color.RED);
                            Log.d("VIEWLIST", "HERE");
                        }

                    }

                    total++;
                }



                //Toast.makeText(getApplicationContext(),s.toString(),Toast.LENGTH_LONG).show();
                like.setText(Integer.toString(value));
                dislike.setText(Integer.toString(total-value));
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        savedInstanceState.putBoolean("ThumbsUp", thumbsUp);
        savedInstanceState.putBoolean("ThumbsDown",thumbsDown);
        savedInstanceState.putString("StringLike",like.getText().toString());
        savedInstanceState.putString("StringDisLike",dislike.getText().toString());


        // etc.

        super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        Toast.makeText(getApplicationContext(),"HERE",Toast.LENGTH_LONG).show();
        thumbsUp = savedInstanceState.getBoolean("ThumbsUp");
        thumbsDown = savedInstanceState.getBoolean("ThumbsDown");


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
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(ViewListValues.this);
        builderSingle.setTitle("FAQ");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ViewListValues.this, android.R.layout.select_dialog_singlechoice);
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
                AlertDialog.Builder builderInner = new AlertDialog.Builder(ViewListValues.this);
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

    public void goToChat(View view) {
        Intent intent = new Intent(this, Chat_Room.class);
        intent.putExtra("name", myFireBaseAuth.getCurrentUser().getEmail().toString());
        intent.putExtra("value", getIntent().getExtras().get("key").toString());
        intent.putExtra("chat",getIntent().getExtras().get("list_name2").toString());
        intent.putExtra("has_list","yes");
        startActivity(intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
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
    public void deleteListItem(View view){
        final Object removedItem = view.getTag();
        //Toast.makeText(getApplicationContext(),removedItem.toString(),Toast.LENGTH_LONG).show();
        android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(ViewListValues.this);
        builder1.setTitle("Confirmation");
        builder1.setMessage("Do you wish to delete this list item?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        room_list.remove(removedItem.toString());
                        HashMap<String,Object> pushValues = new HashMap();
                        pushValues.put("title",title.get(0));
                        pushValues.put("item",room_list);
                        databaseReference.updateChildren(pushValues);
                        newListAdapter.notifyDataSetChanged();
                        Utility.setListViewHeightBasedOnChildren(listViewExpanded);
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

        final android.support.v7.app.AlertDialog alert11 = builder1.create();
        alert11.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alert11.getButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
                alert11.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            }
        });
        alert11.show();

    }
    private void scrollMyListViewToBottom() {
        listViewExpanded.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listViewExpanded.setSelection(newListAdapter.getCount() - 1);
            }
        });
    }

    public void newItemAdded(View view) {
        String getValue = editTextListItem.getText().toString().trim();
        if(TextUtils.isEmpty(getValue))
        {
            Toast.makeText(this,"Enter Value", Toast.LENGTH_LONG).show();
            return;
        }
        room_list.add(getValue);
        HashMap<String,Object> pushValues = new HashMap();
        pushValues.put("title",title.get(0));
        pushValues.put("item",room_list);
        databaseReference.updateChildren(pushValues);
        newListAdapter.notifyDataSetChanged();
        Utility.setListViewHeightBasedOnChildren(listViewExpanded);
        scrollMyListViewToBottom();
        editTextListItem.setText("");
    }
    public void deleteList(View view) {
        final ArrayList publicList = (ArrayList) getIntent().getExtras().get("list_public");
        final ArrayList getDescriptions = (ArrayList) getIntent().getExtras().get("list_description");
        final int position =  getIntent().getExtras().getInt("position");
        //Toast.makeText(getApplicationContext(),"Delete List",Toast.LENGTH_LONG).show();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                ViewListValues.this );

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
}
