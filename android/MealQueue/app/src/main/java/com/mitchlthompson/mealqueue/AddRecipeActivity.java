package com.mitchlthompson.mealqueue;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddRecipeActivity extends AppCompatActivity {
    private static final String TAG = "AddRecipeActivity";

    private Context context;

    private TextView recipeNameInput, directionsInput;
    private String recipeName, directions;
    private Map<String, String> ingredients;
    private Button nextBtn;

    private ArrayList<String> itemNames;
    private ArrayList<String> itemIDs;
    private EditText ingredientItem;
    private Button addIngredientBtn;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

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

        //prevent keyboard from popping up on activity start
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        recipeNameInput = findViewById(R.id.recipe_name_input);
        directionsInput = findViewById(R.id.directions_input);

        ingredients = new HashMap<>();

        nextBtn = findViewById(R.id.add_to_recipes);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(recipeNameInput.getText())) {
                    Toast.makeText(context, "Enter the name of the recipe", Toast.LENGTH_LONG).show();
                }else if(TextUtils.isEmpty(directionsInput.getText())){
                    Toast.makeText(context, "Enter directions for the recipe", Toast.LENGTH_LONG).show();
                } else {
                    recipeName = recipeNameInput.getText().toString();
                    directions = directionsInput.getText().toString();
                    Log.d(TAG, "Recipe name: " + recipeName + " Directions: " + directions);
                    //String key = mRef.push().getKey();
                    //mRef.child(key).child("Recipe Name").setValue(recipeName);
                    //mRef.child(key).child("Directions").setValue(directions);
                    //mRef.child(key).child("Ingredients").setValue(ingredients);
                    //mRef.child(key).child("Recipe ID").setValue(key);
                    startActivity(new Intent(AddRecipeActivity.this, AddIngredientsActivity.class)
                            .putExtra("Recipe Name", recipeName)
                            .putExtra("Directions", directions));
                }


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
