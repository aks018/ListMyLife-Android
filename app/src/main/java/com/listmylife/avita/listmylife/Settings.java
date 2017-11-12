package com.listmylife.avita.listmylife;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.google.firebase.storage.UploadTask;
import com.onesignal.OneSignal;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Settings extends AppCompatActivity {
    FirebaseAuth myFireBaseAuth;
    private StorageReference mStorage;

    private ProgressDialog progressDialog;

    ImageView imageViewProfilePicture;
    @SuppressWarnings("VisibleForTests") private Uri downloadURI;

    ListView settings;
    ArrayAdapter adapter;
    TextView username;
    DatabaseReference databaseReferenceNotification = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcEnableNotification");
    DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcNotifications");

    DatabaseReference databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcUsers");
    HashMap getUserNamesAndEmails;

    private static final int GALLERY_VAL = 100;


    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.listmylife.avita.listmylife.R.layout.activity_settings);
        myFireBaseAuth = FirebaseAuth.getInstance();
        getUserNamesAndEmails = new HashMap();
        getAllUserNames();

        if(myFireBaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        navigationView = (BottomNavigationView) findViewById(com.listmylife.avita.listmylife.R.id.navigation);

        progressDialog = new ProgressDialog(this);
        getSupportActionBar().setTitle("Settings");
        mStorage = FirebaseStorage.getInstance().getReference();
        imageViewProfilePicture = (ImageView) findViewById(com.listmylife.avita.listmylife.R.id.imageViewProfilePicture);

        StorageReference childRef = mStorage.child("Profile_Pictures").child(myFireBaseAuth.getCurrentUser().getEmail().toString());
        childRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getApplicationContext()).load(uri).fit().centerCrop().into(imageViewProfilePicture);
            }
        });

        ArrayList values = new ArrayList();
        values.add("Change Profile Picture");
        values.add("Notifications");
        values.add("Version: 1.2.3");
        settings = (ListView) findViewById(com.listmylife.avita.listmylife.R.id.listViewSettings);
        adapter = new ArrayAdapter(this, com.listmylife.avita.listmylife.R.layout.support_simple_spinner_dropdown_item,values);
        settings.setAdapter(adapter);

        settings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0)
                {
                    addNewProfilePicture();
                }
                else if(i==1)
                {
                    doesWantNotification();
                }
            }
        });

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
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        return true;
                    case com.listmylife.avita.listmylife.R.id.menu_my_lists:
                        Intent intent2 = new Intent(getApplicationContext(),Private_Lists.class);
                        startActivity(intent2);
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
        final ImageView imageView = (ImageView) findViewById(R.id.imageNotication);


        root.child(myFireBaseAuth.getCurrentUser().getEmail().toString().replace('.',' ')).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              Iterator i = dataSnapshot.getChildren().iterator();
                while(i.hasNext())
                {
                    DataSnapshot snapshot2 = ((DataSnapshot)i.next());
                    Log.d("CHANNELTESTING",snapshot2.child("value").toString());
                    if(snapshot2.child("value").getValue().toString().equals("no"))
                    {
                        imageView.setBackgroundResource(R.mipmap.ic_notifications_off_black_24dp);
                        OneSignal.setSubscription(false);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    public void goToMessages() {
        //Toast.makeText(this, "Go To Messages", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,Messages_SplashScreen.class);
        startActivity(intent);
        finish();
    }
    public void goToFriends() {
        Intent intent2 = new Intent(this, Profile_Page_SplashScreen.class);
        startActivity(intent2);
        finish();
    }
    public void doesWantNotification()
    {
        final CharSequence[] items = {"Notifications Enabled", "Notifications Disabled"};
// arraylist to keep the selected items
        final ArrayList seletedItems=new ArrayList();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Notifications")
                .setCancelable(true)
                .setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            seletedItems.add(indexSelected);
                        } else if (seletedItems.contains(indexSelected)) {
                            // Else, if the item is already in the array, remove it
                            seletedItems.remove(Integer.valueOf(indexSelected));
                        }
                    }
                }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        if(seletedItems.size()==2)
                        {
                            Toast.makeText(getApplicationContext(),"Cannot Select Both Items", Toast.LENGTH_LONG).show();
                        }
                        try {
                            if ((int) seletedItems.get(0) == 0) {
                                //Toast.makeText(getApplicationContext(),"TOP SELECTED",Toast.LENGTH_LONG).show();
                                Map<String,Object> map = new HashMap<>();

                                keys = root.push().getKey();

                                root.updateChildren(map);


                                DatabaseReference message_root  = root.child(myFireBaseAuth.getCurrentUser().getEmail().toString().replace('.',' '));

                                Map<String,Object> map2 = new HashMap<>();

                                map2.put("user", myFireBaseAuth.getCurrentUser().getEmail());
                                map2.put("value", "yes");
                                String email = myFireBaseAuth.getCurrentUser().getEmail().toString();
                                message_root.child(email.replace('.',' ')).updateChildren(map2);
                                OneSignal.setSubscription(true);
                                Toast.makeText(getApplicationContext(), "Notifications Enabled for This Device", Toast.LENGTH_LONG).show();
                                Intent intent = getIntent();
                                startActivity(intent);
                                finish();

                            } else {
                                //Toast.makeText(getApplicationContext(),"BOTTOM SELECTED",Toast.LENGTH_LONG).show();
                                Map<String,Object> map = new HashMap<>();

                                keys = root.push().getKey();

                                root.updateChildren(map);


                                DatabaseReference message_root  = root.child(myFireBaseAuth.getCurrentUser().getEmail().toString().replace('.',' '));

                                Map<String,Object> map2 = new HashMap<>();

                                map2.put("user", myFireBaseAuth.getCurrentUser().getEmail());
                                map2.put("value", "no");
                                String email = myFireBaseAuth.getCurrentUser().getEmail().toString();
                                message_root.child(email.replace('.',' ')).updateChildren(map2);
                                OneSignal.setSubscription(false);
                                Toast.makeText(getApplicationContext(), "Notifications Disabled for This Device", Toast.LENGTH_LONG).show();
                                Intent intent = getIntent();
                                startActivity(intent);
                                finish();
                            }
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(getApplicationContext(),"Must Select One Item", Toast.LENGTH_LONG).show();

                        }




                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
                }).create();
        dialog.show();
    }
    public void pushNotificationNo()
    {
        Map<String,Object> map = new HashMap<>();

        keys = databaseReferenceNotification.push().getKey();

        databaseReferenceNotification.updateChildren(map);


        DatabaseReference message_root  = databaseReferenceNotification.child(myFireBaseAuth.getCurrentUser().getEmail().toString().replace('.',' '));

        Map<String,Object> map2 = new HashMap<>();

        map2.put("user", myFireBaseAuth.getCurrentUser().getEmail());
        map2.put("notification", "no");
        String email = myFireBaseAuth.getCurrentUser().getEmail().toString();
        message_root.child(email.replace('.',' ')).updateChildren(map2);
    }
    public void pushNotificationYes()
    {
        Map<String,Object> map = new HashMap<>();

        keys = databaseReferenceNotification.push().getKey();

        databaseReferenceNotification.updateChildren(map);


        DatabaseReference message_root  = databaseReferenceNotification.child(myFireBaseAuth.getCurrentUser().getEmail().toString().replace('.',' '));

        Map<String,Object> map2 = new HashMap<>();

        map2.put("user", myFireBaseAuth.getCurrentUser().getEmail());
        map2.put("notification", "yes");
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

    private void faq() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(Settings.this);
        builderSingle.setTitle("FAQ");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Settings.this, android.R.layout.select_dialog_singlechoice);
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
                AlertDialog.Builder builderInner = new AlertDialog.Builder(Settings.this);
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
        //Toast.makeText(this,"My Groups",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,Private_Lists.class);
        startActivity(intent);
        finish();

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

    public void addNewProfilePicture() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_VAL);

    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
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
                username =(TextView) findViewById(com.listmylife.avita.listmylife.R.id.textView12);
                username.setText(getUserNamesAndEmails.get(myFireBaseAuth.getCurrentUser().getEmail().toString()).toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_VAL && resultCode == RESULT_OK)
        {
            progressDialog.setMessage("Uploading Picture");
            progressDialog.show();
            Uri uri = data.getData();

            StorageReference childRef = mStorage.child("Profile_Pictures").child(myFireBaseAuth.getCurrentUser().getEmail().toString());
            childRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(getApplicationContext(),"New Profile Picture Uploaded",Toast.LENGTH_LONG).show();
                    @SuppressWarnings("VisibleForTests") Uri downloadURI = taskSnapshot.getDownloadUrl();
                    Picasso.with(getApplicationContext()).load(downloadURI).fit().centerCrop().into(imageViewProfilePicture);

                    progressDialog.dismiss();

                }
            });

        }
    }
}
