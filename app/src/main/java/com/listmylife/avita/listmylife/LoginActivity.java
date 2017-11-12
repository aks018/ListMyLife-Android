package com.listmylife.avita.listmylife;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class LoginActivity extends AppCompatActivity {
    ImageButton login;
    EditText loginName;
    EditText passwordTextEdit;
    FirebaseAuth myFireBaseAuth;
    private ProgressDialog progressDialog;
    DatabaseReference databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcUsers");

    ImageButton registerPage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.listmylife.avita.listmylife.R.layout.activity_login);
        progressDialog = new ProgressDialog(this);
        myFireBaseAuth = FirebaseAuth.getInstance();
        if(myFireBaseAuth.getCurrentUser()!=null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),SplashScreen.class));//start profile activity
        }
        login = (ImageButton) findViewById(com.listmylife.avita.listmylife.R.id.buttonLogin);
        loginName = (EditText) findViewById(com.listmylife.avita.listmylife.R.id.editTextName);
        passwordTextEdit = (EditText) findViewById(com.listmylife.avita.listmylife.R.id.editTextPassword);
        registerPage = (ImageButton) findViewById(com.listmylife.avita.listmylife.R.id.textViewGoToRegister);


    }

    public void goToRegister(View view) {
        finish();

        startActivity(new Intent(this, RegisterActivity.class));
    }


    public void loginButton(View view) {
        String userText = loginName.getText().toString();
        String passwordText = passwordTextEdit.getText().toString();

        if(TextUtils.isEmpty(userText))
        {
            Toast.makeText(this,"Empty Email", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(passwordText))
        {
            Toast.makeText(this,"Empty Password", Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Attempting to Login User");
        progressDialog.show();

        myFireBaseAuth.signInWithEmailAndPassword(userText,passwordText).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()) {
                    finish();
                    startActivity(new Intent(getApplicationContext(),SplashScreen.class));
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Could not login", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }

            }
        });



    }

    public void forgotPassword(View view) {
        Intent intent = new Intent(this,Forgot_Password.class);
        startActivity(intent);
        //finish();

    }
}
