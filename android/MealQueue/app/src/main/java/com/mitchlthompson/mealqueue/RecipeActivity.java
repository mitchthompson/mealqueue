package com.mitchlthompson.mealqueue;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mitchlthompson.mealqueue.adapters.RecipeAdapter;

import java.util.HashMap;
import java.util.Map;

public class RecipeActivity extends AppCompatActivity {
    private static final String TAG = "RecipeActivity";

    private Context context;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private String userID;

    private Map<String,Object> recipe;
    private Map<String,String> ingredients;
    private String recipeID, recipeName, directions, ingredientName, ingredientAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        context = getApplicationContext();

        if(getIntent().hasExtra("Recipe ID")) {
            Bundle bundle = getIntent().getExtras();
            recipeID = bundle.getString("Recipe ID");
            recipeName = bundle.getString("Recipe Name");
        }
        else {
            Log.d(TAG, "Nothing in intent bundle");
        }

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mRef = mFirebaseDatabase.getReference("/recipes/" + userID + "/" + recipeID);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged:signed_in: " + user.getUid());
                    Toast.makeText(context,"Successfully signing in with: " + user.getEmail(),
                            Toast.LENGTH_SHORT).show();
                }else{
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Toast.makeText(context,"Successfully signed out", Toast.LENGTH_SHORT).show();
                }
            }
        };

        ingredients = new HashMap<>();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                recipe = (Map<String,Object>) dataSnapshot.getValue();

                directions = recipe.get("Directions").toString();

                //Get ingredients map
                Map ingredients = (Map) recipe.get("Ingredients");
                    Log.d(TAG, " recipe name: " + recipe.get("Recipe Name").toString()
                            + " recipeID: " + recipe.get("Recipe ID").toString()
                    + " directions: " + directions + " ingredients: " + ingredients.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
}