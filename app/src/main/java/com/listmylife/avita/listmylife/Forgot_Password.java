package com.listmylife.avita.listmylife;

import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.FormatException;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Forgot_Password extends AppCompatActivity {
    HashMap getUserNamesAndEmails;
    private final int SPLASH_DISPLAY_LENGTH = 300;
    ProgressDialog pd;
    EditText email;
    ArrayList getValues;
    FirebaseAuth myFireBaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot__password);
        email = (EditText) findViewById(R.id.editTextName);
        getUserNamesAndEmails = new HashMap();
        myFireBaseAuth = FirebaseAuth.getInstance();
        if(myFireBaseAuth.getCurrentUser()!=null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        getValues = new ArrayList();

                Log.d("TESTING_VALUES","HERE");




    }
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    public void recoverPassword(View view) {
        final String getEmail;
        getEmail = email.getText().toString();
        Log.d("GET_VALUES",getUserNamesAndEmails.toString());

        if(getEmail.length()==0)
        {
            Toast.makeText(getApplicationContext(),"Please Enter Valid Email", Toast.LENGTH_LONG).show();
            return;
        }
        if(!isEmailValid(getEmail))
        {
            Toast.makeText(getApplicationContext(),"Please Enter Valid Email", Toast.LENGTH_LONG).show();
            return;
        }



        pd = new ProgressDialog(view.getContext());
        pd.setMessage("Sending Email for Password Recovery.");
        pd.show();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                FirebaseAuth.getInstance().sendPasswordResetEmail(getEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(),"Password Recovery Email Sent", Toast.LENGTH_LONG).show();
                        pd.dismiss();
                        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                });
                FirebaseAuth.getInstance().sendPasswordResetEmail(getEmail).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Password Recovery Email Not Sent", Toast.LENGTH_LONG).show();
                        pd.dismiss();
                    }
                });



            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
