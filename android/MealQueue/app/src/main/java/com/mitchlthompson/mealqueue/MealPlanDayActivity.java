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
import com.mitchlthompson.mealqueue.adapters.MealPlanRecipeAdapter;
import com.mitchlthompson.mealqueue.adapters.RecipeAdapter;

import java.util.ArrayList;
import java.util.Map;

public class MealPlanDayActivity extends AppCompatActivity {

    private static final String TAG = "MealPlanDayActivity";

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;
    private DatabaseReference mRef;

    private Context context;
    private RecyclerView recyclerView;
    private MealPlanRecipeAdapter mealPlanRecipeAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private ArrayList<String> recipeNames;
    private ArrayList<String> recipeIDs;
    private String date;
    private Button addRecipeBtn;

    private Map<String,Object> recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan_day);
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

        if(getIntent().hasExtra("Date")) {
            Bundle bundle = getIntent().getExtras();
            date = bundle.getString("Date");
        }
        else {
            Log.d(TAG, "Nothing in intent bundle");
            startActivity(new Intent(MealPlanDayActivity.this, MainActivity.class));
        }



        recipeIDs = new ArrayList<>();
        recipeNames = new ArrayList<>();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                recipes = (Map<String,Object>) dataSnapshot.getValue();
                for (Map.Entry<String, Object> entry : recipes.entrySet()){
                    //Get user map
                    Map singleRecipe = (Map) entry.getValue();
                    //Get recipe name field and append to list
                    recipeNames.add((singleRecipe.get("Recipe Name").toString()));
                    recipeIDs.add(singleRecipe.get("Recipe ID").toString());

                    recyclerView = findViewById(R.id.recipe_recycler);
                    recyclerViewLayoutManager = new LinearLayoutManager(context);
                    recyclerView.setLayoutManager(recyclerViewLayoutManager);

                    mealPlanRecipeAdapter = new MealPlanRecipeAdapter(context, date, recipeNames, recipeIDs);
                    recyclerView.setAdapter(mealPlanRecipeAdapter);

                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
