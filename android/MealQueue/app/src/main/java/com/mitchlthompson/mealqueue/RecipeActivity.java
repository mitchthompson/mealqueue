package com.mitchlthompson.mealqueue;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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

    private HashMap<String,Object> recipe;
    private Map<String,String> ingredientsMap;
    private ArrayList<String> ingredients;
    private String recipeID, recipeName, directions, ingredientName, ingredientAmount;
    private TextView recipeNameTextView, directionsTextview;
    private Button recipeDelete, recipeEdit, recipeAddToMealPlan;
    private String removeMealDate;

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
//                    Toast.makeText(context,"Successfully signing in with: " + user.getEmail(),
//                            Toast.LENGTH_SHORT).show();
                }else{
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    //Toast.makeText(context,"Successfully signed out", Toast.LENGTH_SHORT).show();
                }
            }
        };


        recipeNameTextView = findViewById(R.id.recipe_name_textView);
        directionsTextview = findViewById(R.id.recipe_directions_textView);

        ingredientsMap = new HashMap<>();
        ingredients = new ArrayList<>();
        final ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, ingredients);
        ListView listView = findViewById(R.id.ingredients_listView);
        listView.setAdapter(itemsAdapter);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                recipe = (HashMap<String,Object>) dataSnapshot.getValue();
                if(recipe!=null) {

                    recipeNameTextView.setText(recipe.get("Recipe Name").toString());
                    directionsTextview.setText(recipe.get("Directions").toString());

                    //Get ingredients map
                    ingredientsMap = (HashMap) recipe.get("Ingredients");
                    for (String key : ingredientsMap.keySet()) {
                        //iterate over key
                        Log.d(TAG, ingredientsMap.get(key) + " " + key);
                        ingredients.add(ingredientsMap.get(key) + " " + key);
                    }
                    itemsAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        recipeDelete = findViewById(R.id.recipe_delete);
        recipeDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    deleteRecipe();
            }
        });

    }

    private void deleteRecipe(){
        mFirebaseDatabase.getReference("/recipes/" + userID).child(recipeID).removeValue();
        final DatabaseReference removeFromMealPlan = mFirebaseDatabase.getReference("/mealplans/" + userID);
        removeFromMealPlan.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //Log.d(TAG, snapshot.getKey().toString());
                    removeMealDate = snapshot.getKey().toString();
                    for(DataSnapshot child: snapshot.getChildren()){
                        //Log.d(TAG, child.getValue().toString() + " " + child.getKey().toString());
                        if(child.getValue().toString().equals(recipeID)){
                            removeFromMealPlan.child(removeMealDate).child(child.getKey().toString()).removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
