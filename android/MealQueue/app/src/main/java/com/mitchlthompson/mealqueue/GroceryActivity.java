package com.mitchlthompson.mealqueue;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mitchlthompson.mealqueue.R;
import com.mitchlthompson.mealqueue.adapters.GroceryListAdapter;

import java.util.ArrayList;

public class GroceryActivity extends AppCompatActivity {
    private static final String TAG = "GroceryActivity";

    private Context context;
    private RecyclerView recyclerView;
    private RelativeLayout relativeLayout;
    private GroceryListAdapter groceryListAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private ArrayList<String> itemNames;
    private ArrayList<String> itemIDs;
    private EditText groceryItem;
    private Button addItemBtn;
    private Button clearItemsBtn;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery);

        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mRef = mFirebaseDatabase.getReference("/grocery/" + userID);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged:signed_in: " + user.getUid());
                    Toast.makeText(context,"Successfully signing in with: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                }else{
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Toast.makeText(context,"Successfully signed out", Toast.LENGTH_SHORT).show();
                }
            }
        };

        Log.d(TAG, userID);

        itemNames = new ArrayList<>();
        itemIDs = new ArrayList<>();

        groceryItem = findViewById(R.id.grocery_item);
        addItemBtn = findViewById(R.id.add_item_btn);
        addItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRef.push().setValue(groceryItem.getText().toString());

            }
        });

        clearItemsBtn = findViewById(R.id.grocery_clear);
        clearItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRef.removeValue();
                itemIDs.clear();
                itemNames.clear();
                groceryListAdapter.notifyDataSetChanged();
            }

        });

        mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String value = dataSnapshot.getValue(String.class);
                String key = dataSnapshot.getKey();
                itemIDs.add(key);
                itemNames.add(value);
                groceryListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        context = getApplicationContext();
        relativeLayout = findViewById(R.id.action_grocery);
        recyclerView = findViewById(R.id.grocery_recycler);
        recyclerViewLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);

        groceryListAdapter = new GroceryListAdapter(context, userID, itemNames, itemIDs);
        recyclerView.setAdapter(groceryListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}