package com.listmylife.avita.listmylife;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth myFireBaseAuth;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().getRoot();

    EditText email;
    EditText password;
    EditText username;
    EditText passwordAgain;
    EditText getUserName;

    private ProgressDialog progressDialog;

    DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcUsers");;
    DatabaseReference root2 = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcUsersNames");;

    String keys;
    EditText lastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.listmylife.avita.listmylife.R.layout.activity_register);

        myFireBaseAuth = FirebaseAuth.getInstance();
        getSupportActionBar().setTitle("Register Account");

        if(myFireBaseAuth.getCurrentUser()!=null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        email = (EditText) findViewById(com.listmylife.avita.listmylife.R.id.editText2);
        password = (EditText) findViewById(com.listmylife.avita.listmylife.R.id.editText3);
        username = (EditText) findViewById(com.listmylife.avita.listmylife.R.id.editTextgetUserName);
        lastName  = (EditText) findViewById(R.id.editText4);
        passwordAgain = (EditText) findViewById(com.listmylife.avita.listmylife.R.id.editText5);
        getUserName = (EditText) findViewById(R.id.editText6);
        progressDialog = new ProgressDialog(this);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);




    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void actionBarSetup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar ab = getActionBar();
            ab.setTitle("Register");
        }
    }
    public void addUsers(final String userEmail, final String userName)
    {

        //Toast.makeText(getApplicationContext(),"HERE",Toast.LENGTH_LONG).show();
        Log.d("CHECK-VALUE2", "CLICKED");
        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.i("CHECK-VALUE", dataSnapshot.toString());
                } else {
                    Log.e("CHECK-VALUE", "Not found: " + "WE HERE");
                }
                Iterator i = dataSnapshot.getChildren().iterator();
                ArrayList usernames = new ArrayList();
                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    Log.d("CHECK-VALUE", snapshot.child("username").toString());
                    usernames.add(snapshot.child("username").getValue().toString());
                }
                Log.d("CHECK-VALUE", usernames.toString());
                if(usernames.contains(userName))
                {
                    Log.d("CHECK-VALUE","WE HAVE CAUGHT A DUP");
                    Toast.makeText(getApplicationContext(),"Username Taken", Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    Map notification = new HashMap<>();
                    notification.put("email", userEmail);
                    notification.put("username", userName);
                    root.push().setValue(notification);
                    pushNotificationYes();
                    OneSignal.setSubscription(true);
                    SharedPreferences.Editor e = getSharedPreferences("NOTIFICATION", Context.MODE_PRIVATE).edit();
                    e.putString("notification", "yes");
                    e.commit();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("CHECK-VALUE", databaseError.toString());

            }
        });

    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.values, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.phone:
                Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT)
                        .show();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this,Settings.class);
                startActivity(intent);
                Toast.makeText(this, "Refresh selected", Toast.LENGTH_SHORT)
                        .show();
                finish();
                return true;

            case R.id.action_favorite:
                refreshPage();
                Toast.makeText(this, "Refresh selected", Toast.LENGTH_SHORT)
                        .show();
                return true;
            case R.id.action_register:
                register_person();
                Toast.makeText(this, "Register selected", Toast.LENGTH_SHORT)
                        .show();
                return true;


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void register_person() {
        Intent intent = new Intent(this, RegisterActivity.class);

        startActivity(intent);
        finish();
    }

    private void refreshPage() {
        finish();
        startActivity(getIntent());
    }

    public void goToHome(View view) {
        Toast.makeText(this,"Go Home",Toast.LENGTH_LONG).show();
    }

    public void goToMyGroups(View view) {
        Toast.makeText(this,"My Groups",Toast.LENGTH_LONG).show();

    }*/
    DatabaseReference databaseReferenceNotification = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcEnableNotification");

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

    public void questions(View view) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("FAQs");
        builder1.setCancelable(true);
        final TextView input = new TextView (this);
        input.setText(" -Enter only a valid email address \n" +
                " -Some examples include test@gmail.com,  test@yahoo.com");
        builder1.setView(input);
        builder1.setPositiveButton(
                "Got It",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public void passwordFAQ(View view) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("FAQs");
        builder1.setCancelable(true);
        final TextView input = new TextView (this);
        input.setText(" -Password must be at least 6 characters \n" +
                " -Some good passwords include test123!!, Azjg91??@");
        builder1.setView(input);
        builder1.setPositiveButton(
                "Got It",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
    public void createNewUser(final String emailText, final String passwordText, final String userNameText)
    {



    }
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    public void submitNewAccount(View view) {

        final String passwordAgainText = passwordAgain.getText().toString();
        final String emailText = email.getText().toString();
        final String passwordText = password.getText().toString();
        final String userNameText = username.getText().toString();
        final String lastNameText = lastName.getText().toString();
        final String getUserNameText = getUserName.getText().toString();
        if(TextUtils.isEmpty(emailText))
        {
            Toast.makeText(this,"Empty Email", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(passwordText))
        {
            Toast.makeText(this,"Empty Password", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(userNameText))
        {
            Toast.makeText(this,"Empty First Name", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(getUserNameText))
        {
            Toast.makeText(this,"Empty Username", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(lastNameText))
        {
            Toast.makeText(this,"Empty Last Name", Toast.LENGTH_LONG).show();
            return;
        }

        if(passwordText.length() < 6)
        {
            Toast.makeText(this,"Password must be at least 6 characters long", Toast.LENGTH_LONG).show();
            return;
        }

        if(!isEmailValid(emailText))
        {
            Toast.makeText(this,"Please enter a valid email", Toast.LENGTH_LONG).show();
            return;
        }
        if(!passwordAgainText.equals(passwordText))
        {
            Toast.makeText(this, "Passwords Not Equal", Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Registering User");
        progressDialog.show();


        Log.d("CHECK-VALUE2", "CLICKED");
        myFireBaseAuth.createUserWithEmailAndPassword(emailText,passwordText).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(userNameText).build();
                            myFireBaseAuth.getCurrentUser().updateProfile(profileUpdates);
                            progressDialog.dismiss();
                            root.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d("CHECK-VALUE", "MADE IT HERE");
                                    Iterator i = dataSnapshot.getChildren().iterator();
                                    ArrayList usernames = new ArrayList();
                                    ArrayList emails = new ArrayList();
                                    while(i.hasNext())
                                    {
                                        DataSnapshot snapshot = ((DataSnapshot)i.next());
                                        Log.d("CHECK-VALUE", snapshot.child("username").toString());
                                        usernames.add(snapshot.child("username").getValue().toString());
                                    }
                                    Log.d("CHECK-VALUE", usernames.toString());
                                    if(usernames.contains(getUserNameText))
                                    {
                                        Log.d("CHECK-VALUE","WE HAVE CAUGHT A DUP");
                                        Toast.makeText(getApplicationContext(),"Username Taken", Toast.LENGTH_LONG).show();
                                        myFireBaseAuth.getCurrentUser().delete();
                                        return;
                                    }
                                    else {
                                        Toast.makeText(RegisterActivity.this,"Successful Registration", Toast.LENGTH_LONG).show();
                                        Map notification = new HashMap<>();
                                        notification.put("email", emailText);
                                        notification.put("username", getUserNameText);
                                        root.push().setValue(notification);

                                        Map notification2 = new HashMap();
                                        notification2.put("email",emailText);
                                        notification2.put("name",userNameText + " " + lastNameText);
                                        root2.push().setValue(notification2);
                                        Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }


                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("CHECK-VALUE", databaseError.toString());
                                }
                            });


                        }
                        else
                        {
                            Toast.makeText(RegisterActivity.this,"Email Already Taken", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }

                    }
                });



    }

    public void goToLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void usernameQuestions(View view) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("FAQs");
        builder1.setCancelable(true);
        final TextView input = new TextView (this);
        input.setText(" -Use only A-Z and 0-9 for username \n" +
                " -Refrain from using symbols such as ?,!,@" +
                " -Some good usernames include test123, John345");
        builder1.setView(input);
        builder1.setPositiveButton(
                "Got It",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}
