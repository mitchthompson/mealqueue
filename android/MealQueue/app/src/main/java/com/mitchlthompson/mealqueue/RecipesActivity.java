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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.Map;

public class RecipesActivity extends AppCompatActivity {
    private static final String TAG = "RecipesActivity";

    private Context context;
    private RecyclerView recyclerView;
    private RelativeLayout relativeLayout;
    private RecipeAdapter recipeAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private ArrayList<String> recipeNames;
    private ArrayList<String> recipeIDs;
    private Button addRecipeBtn;

    private Map<String,Object> recipes;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);
        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        context = getApplicationContext();

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mRef = mFirebaseDatabase.getReference("/recipes/" + userID);

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

        Log.d(TAG, "Firebase URL: " + mRef);
        Log.d(TAG, "User ID: " + userID);


        addRecipeBtn = findViewById(R.id.launch_addrecipe_btn);
        addRecipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RecipesActivity.this, AddRecipeActivity.class));

            }
        });

        recipeIDs = new ArrayList<>();
        recipeNames = new ArrayList<>();

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                recipes = (Map<String,Object>) dataSnapshot.getValue();
                if(recipes!= null) {
                    for (Map.Entry<String, Object> entry : recipes.entrySet()) {
                        //Get user map
                        Map singleRecipe = (Map) entry.getValue();
                        //Get recipe name field and append to list
                        recipeNames.add((singleRecipe.get("Recipe Name").toString()));
                        recipeIDs.add(singleRecipe.get("Recipe ID").toString());
                        Log.d(TAG, " recipe name: " + singleRecipe.get("Recipe Name").toString()
                                + " recipeID: " + singleRecipe.get("Recipe ID").toString());

                        relativeLayout = (RelativeLayout) findViewById(R.id.action_recipes);
                        recyclerView = (RecyclerView) findViewById(R.id.recipe_recycler);
                        recyclerViewLayoutManager = new LinearLayoutManager(context);
                        recyclerView.setLayoutManager(recyclerViewLayoutManager);

                        recipeAdapter = new RecipeAdapter(context, recipeNames, recipeIDs);
                        recyclerView.setAdapter(recipeAdapter);
                    }
                }else{
                    Log.d(TAG, "No Recipes found.");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}