package com.mitchlthompson.mealqueue;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
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
import com.mitchlthompson.mealqueue.adapters.IngredientsAdapter;
import com.mitchlthompson.mealqueue.helpers.Ingredient;

import java.util.ArrayList;

import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;

public class AddRecipeActivity extends AppCompatActivity {
    private static final String TAG = "AddRecipeActivity";

    private Context context;

    private TextView recipeNameInput, directionsInput;
    private String recipeName, directions;
    private Button nextBtn, importBtn, addIngredientBtn;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private String userID;

    private RecyclerView recyclerView;
    private IngredientsAdapter ingredientsAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private ArrayList<Ingredient> ingredients;
    private EditText ingredientItem, ingredientAmount;

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
                    //Toast.makeText(context,"Successfully signing in with: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                }else{
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    //Toast.makeText(context,"Successfully signed out", Toast.LENGTH_SHORT).show();
                }
            }
        };

        //prevent keyboard from popping up on activity start
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        recipeNameInput = findViewById(R.id.edit_name_input);
        directionsInput = findViewById(R.id.edit_directions_input);

        //ingredients = new HashMap<>();

        ingredients = new ArrayList<>();

        nextBtn = findViewById(R.id.add_to_recipes);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(recipeNameInput.getText())) {
                    Toast.makeText(context, "Enter the name of the recipe", Toast.LENGTH_LONG).show();
                }else if(TextUtils.isEmpty(directionsInput.getText())){
                    Toast.makeText(context, "Enter directions for the recipe", Toast.LENGTH_LONG).show();
                }else if(ingredients.isEmpty()){
                    Toast.makeText(context, "Add the ingredients to finish the recipe", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "No ingredients entered");
                }else{
                    AddRecipe();
                }
            }
        });

        importBtn = findViewById(R.id.import_btn);
        importBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddRecipeActivity.this, ImportActivity.class));
            }
        });

        ingredientItem = findViewById(R.id.edit_ingredient_item_EditText);
        ingredientAmount = findViewById(R.id.edit_ingredient_amount_EditText);


        addIngredientBtn = findViewById(R.id.add_ingredient_btn);
        addIngredientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d(TAG, ingredientItem.getText().toString());
                InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                if(TextUtils.isEmpty(ingredientItem.getText())) {
                    Toast.makeText(context, "Enter an item to add to the ingredients list", Toast.LENGTH_LONG).show();
                }else if(TextUtils.isEmpty(ingredientAmount.getText())){
                    Toast.makeText(context, "Enter the item amount", Toast.LENGTH_LONG).show();
                } else {

                    ingredients.add(new Ingredient(capitalizeFully(ingredientItem.getText().toString()),ingredientAmount.getText().toString()));
                    ingredientsAdapter.notifyDataSetChanged();
                    ingredientAmount.setText("");
                    ingredientItem.setText("");
                }
            }
        });

        context = getApplicationContext();
        recyclerView = findViewById(R.id.edit_ingredient_recycler);
        recyclerViewLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);

        ingredientsAdapter = new IngredientsAdapter(context, ingredients);
        recyclerView.setAdapter(ingredientsAdapter);

    }

    private void AddRecipe(){
        recipeName = recipeNameInput.getText().toString();
        directions = directionsInput.getText().toString();
        String key = mRef.push().getKey();
        mRef.child(key).child("Recipe Name").setValue(capitalizeFully(recipeName));
        mRef.child(key).child("Directions").setValue(directions);
        mRef.child(key).child("Ingredients").setValue(ingredients);
        mRef.child(key).child("Recipe ID").setValue(key);



    }
}
