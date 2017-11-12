package com.listmylife.avita.listmylife;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.joanzapata.iconify.widget.IconButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Find_New_Friends extends AppCompatActivity {
    BottomNavigationView navigationView;
    HashMap<String,String> getEmailAndUsername;
    ListView getAllUsers;
    ArrayList allUsersList;
    ArrayList addValues;

    FirebaseAuth myFireBaseAuth;


    AddNewFriendAdapter addNewFriendAdapter;

    String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.listmylife.avita.listmylife.R.layout.activity_find__new__friends);
        myFireBaseAuth = FirebaseAuth.getInstance();
        username = "";
        if(myFireBaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        pushLoginInfoYes();
        navigationView = (BottomNavigationView) findViewById(com.listmylife.avita.listmylife.R.id.navigation);
        allUsersList = new ArrayList();
        getEmailAndUsername =(HashMap) getIntent().getSerializableExtra("hashmap");
        addValues = new ArrayList();
        for(Map.Entry<String,String> entry: getEmailAndUsername.entrySet()) {
            if(entry.getKey().equals(myFireBaseAuth.getCurrentUser().getEmail().toString()))
            {
                username = entry.getValue();
            }else {
                allUsersList.add(entry.getValue());
                addValues.add("friends");
            }
        }
        addNewFriendAdapter = new AddNewFriendAdapter(this,allUsersList,addValues);
        getAllUsers =(ListView) findViewById(com.listmylife.avita.listmylife.R.id.listViewAllUsers);
        getAllUsers.setAdapter(addNewFriendAdapter);
        getAllUsers.setVisibility(View.INVISIBLE);

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
                        //expListView.setSelectionAfterHeaderView();
                        Intent intent3 = new Intent(getApplicationContext(),Profile_Page_SplashScreen.class);
                        startActivity(intent3);
                        finish();
                        return true;
                }
                return true;
            }
        });
        Log.d("GOT_THEM",getEmailAndUsername.toString());
        getSupportActionBar().setTitle("Find Friends");

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_only_search, menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(com.listmylife.avita.listmylife.R.menu.menu_only_search,menu);
        MenuItem item = menu.findItem(com.listmylife.avita.listmylife.R.id.action_search);


        SearchView searchView = (SearchView)item.getActionView();

        searchView.setQueryHint("Find Friends...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(s.length()>=1) {

                    //friends.setAdapter(adapterForListView);
                    //myCurrentFriends.remove(myFireBaseAuth.getCurrentUser().getEmail().toString());
                    //adapterForListView.getFilter().filter(s);
                    getAllUsers.setVisibility(View.VISIBLE);
                    getAllUsers.setAdapter(addNewFriendAdapter);
                    //allUsersList.remove(myFireBaseAuth.getCurrentUser().getEmail().toString());

                    addNewFriendAdapter.getFilter().filter(s);

                    return false;
                }
                else
                {
                    getAllUsers.setVisibility(View.INVISIBLE);
                    return false;
                }
            }

        });
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.listmylife.avita.listmylife.R.id.action_search:
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    public void goToMessages() {
        //Toast.makeText(this, "Go To Messages", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,Messages_SplashScreen.class);
        startActivity(intent);
        finish();
    }

    public void weAreFriends(View view) {
        Object value = view.getTag();
        //Toast.makeText(getApplicationContext(),"Already Friends " + value.toString(), Toast.LENGTH_LONG).show();

        String getEmail="";
        for (Map.Entry<String, String> entry : getEmailAndUsername.entrySet()) {
            if (Objects.equals(value.toString(), entry.getValue())) {
                getEmail = entry.getKey();
            }
        }
        //Toast.makeText(getApplicationContext(),getEmail,Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), Other_User_Profile.class);
        intent.putExtra("hashmap",getEmailAndUsername);
        intent.putExtra("email", getEmail);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    DatabaseReference databaseReferenceLogin = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcLogin");
    String keys;
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
}
