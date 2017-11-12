package com.listmylife.avita.listmylife;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;

public class Messages_SplashScreen extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 0;
    FirebaseAuth myFireBaseAuth;
    DatabaseReference databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcUsers");
    HashMap getUserNamesAndEmails;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.listmylife.avita.listmylife.R.layout.activity_messages__splash_screen);
        getUserNamesAndEmails = new HashMap();
        pd = new ProgressDialog(Messages_SplashScreen.this);

        pd.setMessage("Retrieving Messages...");
        pd.setCancelable(false);
        pd.show();


        myFireBaseAuth = FirebaseAuth.getInstance();
        if(myFireBaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),Messages.class));//start profile activity
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
               getAllUserNames();
            }
        });
         /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/

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
                Log.d("TESTING_VALUESTEST", getUserNamesAndEmails.toString());
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                        Intent mainIntent = new Intent(Messages_SplashScreen.this,Messages.class);
                        mainIntent.putExtra("hashmap", getUserNamesAndEmails);
                        Log.i("HASHMAP",getUserNamesAndEmails.toString());
                        Messages_SplashScreen.this.startActivity(mainIntent);
                        Messages_SplashScreen.this.finish();
                        pd.dismiss();
                    }
                }, SPLASH_DISPLAY_LENGTH);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {

    }
}
