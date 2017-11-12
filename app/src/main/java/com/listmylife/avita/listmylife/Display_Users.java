package com.listmylife.avita.listmylife;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Display_Users extends AppCompatActivity {
    FirebaseAuth myFireBaseAuth;

    TextView getChatName;
    String name;
    String chat_room;
    String hasList;
    ListView listViewUsers;
    ArrayList friendsThatExist;
    ArrayList listOfFriends;
    ArrayList myCurrentFriends;
    ArrayAdapter adapterForListView;
    ArrayAdapter adapter;

    String key;

    HashMap<String,String> getUserNamesAndEmails;
    ArrayList setUsers;
    DatabaseReference databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcUsers");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.listmylife.avita.listmylife.R.layout.activity_display__users);
        myFireBaseAuth = FirebaseAuth.getInstance();
        if(myFireBaseAuth.getCurrentUser()==null)
        {
            finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));//start profile activity
        }
        Intent intent = getIntent();
        setUsers = new ArrayList();
        setUsers = getIntent().getExtras().getStringArrayList("list_of_users");
        chat_room = intent.getStringExtra("chat_name");
        name = intent.getStringExtra("name");
        key = intent.getStringExtra("value");
        getSupportActionBar().setTitle("Users");;
        getUserNamesAndEmails= (HashMap) getIntent().getSerializableExtra("hashmap");
        hasList = getIntent().getStringExtra("has_list");
        ImageButton imageButton = (ImageButton) findViewById(R.id.buttonAddNewFriend);

        if(hasList.equals("yes"))
        {imageButton.setVisibility(View.INVISIBLE);}
        else if(hasList.equals("no"))
        {
            imageButton.setVisibility(View.VISIBLE);
        }

        getChatName = (TextView) findViewById(com.listmylife.avita.listmylife.R.id.textViewChatName);
        getChatName.setText(chat_room);

        friendsThatExist = new ArrayList();
        for(HashMap.Entry<String,String> entry: getUserNamesAndEmails.entrySet())
        {
            for(int i=0;i<setUsers.size();i++)
            {
                if(setUsers.get(i).toString().equals(entry.getKey()))
                {
                    friendsThatExist.add(entry.getValue());
                }
            }
        }

        myCurrentFriends = new ArrayList();
        listOfFriends = new ArrayList();

        adapter = new
                ArrayAdapter(this,android.R.layout.simple_list_item_1,listOfFriends);

        listViewUsers = (ListView) findViewById(com.listmylife.avita.listmylife.R.id.listViewItems);

        ArrayAdapter existingFriends = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,friendsThatExist);
        listViewUsers.setAdapter(existingFriends);
        updateList();
        loadUpData();





    }


    public void updateList()
    {
        final ArrayList displayUsers = new ArrayList();
        DatabaseReference databaseReferenceChat = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcChat").child(chat_room);
        databaseReferenceChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                ArrayList a = new ArrayList();
                String value="";
                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    int value_test;
                    try {
                        value_test = Integer.parseInt(snapshot.getKey());
                        a.add(snapshot.getValue().toString());
                        Log.d("TESTING_VALUES" , snapshot.getValue().toString());

                    } catch (NumberFormatException e) {
                        value_test = 0;
                    }

                }
                //Toast.makeText(getApplicationContext(),a.toString(),Toast.LENGTH_LONG).show();
                for(int j=0; j<a.size();j++)
                {
                    displayUsers.add(a.get(j));
                    friendsThatExist.add(a.get(j));

                }

               // listViewUsers.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void returnToChat(View view) {
        Intent intent = new Intent(this, Chat_Room.class);
        intent.putExtra("name",name);
        intent.putExtra("chat",chat_room);
        startActivity(intent);
    }

    public void newFriend(View view) {
        /*final DatabaseReference databaseReferenceChat = FirebaseDatabase.getInstance().getReference().getRoot().child("zxcChat").child(chat_room);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Friends To Add To Chat");
        builder.setCancelable(true);
        final EditText input = new EditText(this);
        input.setHint("hint");
        builder.setMessage("Hello");
        LayoutInflater inflater = getLayoutInflater();
        final View view2 = inflater.inflate(com.listmylife.avita.listmylife.R.layout.textview19, null);
        builder.setView(view2);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AutoCompleteTextView editText = (AutoCompleteTextView) view2.findViewById(com.listmylife.avita.listmylife.R.id.editText);
                editText.setAdapter(adapter);
                editText.setThreshold(3);
                String getText = editText.getText().toString();
                if(TextUtils.isEmpty(getText))
                {
                    Toast.makeText(getApplicationContext(),"Empty User", Toast.LENGTH_LONG).show();
                    return;
                }
                if(friendsThatExist.contains(getText)){
                    Toast.makeText(getApplicationContext(),"User Already In Chat", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!listOfFriends.contains(getText))
                {
                    Toast.makeText(getApplicationContext(),"Add a User's Email you are friends with", Toast.LENGTH_LONG).show();
                    return;
                }

                Log.d("TESTING", editText.getText().toString());
                Log.d("TESTING", Integer.toString(friendsThatExist.size()));

                Map m = new HashMap();
                m.put(Integer.toString(friendsThatExist.size()),getText);
                databaseReferenceChat.updateChildren(m);
                updateList();;

            }
        });

        AlertDialog alert11 = builder.create();
        alert11.show();*/
        Intent intent = new Intent(this,Add_New_Friend.class);
        /*setUsers = getIntent().getExtras().getStringArrayList("list_of_users");
        chat_room = intent.getStringExtra("chat_name");
        name = intent.getStringExtra("name");
        getUserNamesAndEmails= (HashMap) getIntent().getSerializableExtra("hashmap");
        hasList = getIntent().getStringExtra("has_list");*/
        intent.putStringArrayListExtra("list_of_users",setUsers);
        intent.putExtra("chat_name",chat_room);
        intent.putExtra("name",name);
        intent.putExtra("hashmap",getUserNamesAndEmails);
        intent.putExtra("has_list",hasList);
        intent.putExtra("value",key);
        startActivity(intent);
        finish();



    }
    public void loadUpData()
    {

        databaseReferenceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList s  = new ArrayList();
                Iterator i = dataSnapshot.getChildren().iterator();
                while(i.hasNext())
                {
                    DataSnapshot snapshot = ((DataSnapshot)i.next());
                    String getEmail = snapshot.child("email").getValue().toString();

                    s.add(getEmail.toString());
                    //Toast.makeText(getApplicationContext(),getEmail.toString(),Toast.LENGTH_LONG).show();
                }




                listOfFriends.clear();
                listOfFriends.addAll(s);
                ///Toast.makeText(getApplicationContext(),listOfFriends.toString(),Toast.LENGTH_LONG).show();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
