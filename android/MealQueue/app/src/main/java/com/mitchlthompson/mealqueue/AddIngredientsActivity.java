package com.mitchlthompson.mealqueue;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mitchlthompson.mealqueue.adapters.IngredientsAdapter;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;

public class AddIngredientsActivity extends AppCompatActivity {

    private static final String TAG = "AddIngredientsActivity";

    private Context context;
    private RecyclerView recyclerView;
    private IngredientsAdapter ingredientsAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private FrameLayout mMainFrame;

    private String recipeName, directions;
    private Map<String, String> ingredients;
    private Button addRecipeBtn, addIngredientBtn;

    private ArrayList<String> itemNames;
    private ArrayList<String> itemAmounts;
    private EditText ingredientItem, ingredientAmount;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredients);
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


        if(getIntent().hasExtra("Recipe Name")) {
            Bundle bundle = getIntent().getExtras();
            recipeName = bundle.getString("Recipe Name");
            directions = bundle.getString("Directions");
        }
        else {
            Log.d(TAG, "Nothing in intent bundle");
            startActivity(new Intent(AddIngredientsActivity.this, AddRecipeActivity.class));
        }

        //prevent keyboard from popping up on activity start
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ingredientItem = findViewById(R.id.ingredient_item_EditText);
        ingredientAmount = findViewById(R.id.ingredient_amount_EditText);


        ingredients = new HashMap<>();
        itemNames = new ArrayList<>();
        itemAmounts = new ArrayList<>();

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
                    itemNames.add(capitalizeFully(ingredientItem.getText().toString()));
                    itemAmounts.add(ingredientAmount.getText().toString());
                    Log.d(TAG, "Add ingredient: " + ingredientItem.getText().toString() + " " + ingredientAmount.getText().toString());
                    ingredientsAdapter.notifyDataSetChanged();
                    ingredientAmount.setText("");
                    ingredientItem.setText("");
                }
            }
        });

        context = getApplicationContext();
        recyclerView = findViewById(R.id.ingredient_recycler);
        recyclerViewLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);

        ingredientsAdapter = new IngredientsAdapter(context, itemNames, itemAmounts);
        recyclerView.setAdapter(ingredientsAdapter);

        addRecipeBtn = findViewById(R.id.recipe_done_btn);
        addRecipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemNames.isEmpty() || itemAmounts.isEmpty()){
                    Toast.makeText(context, "Add the ingredients to finish the recipe", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "No ingredients entered");
                }else{
                    AddRecipe();
                    startActivity(new Intent(AddIngredientsActivity.this, MainActivity.class)
                            .putExtra("New Recipe", recipeName));
                }

            }
        });
    }

    private void AddRecipe(){
        Log.d(TAG, "Recipe name: " + recipeName + " Directions: " + directions);
        for(int i=0;i<itemNames.size();i++){
            ingredients.put(itemNames.get(i), itemAmounts.get(i));
            Log.d(TAG, "Add to Ingredients List: " + itemNames.get(i) + " " + itemAmounts.get(i));
        }
        Log.d(TAG, ingredients.toString());
        String key = mRef.push().getKey();
        mRef.child(key).child("Recipe Name").setValue(capitalizeFully(recipeName));
        mRef.child(key).child("Directions").setValue(directions);
        mRef.child(key).child("Ingredients").setValue(ingredients);
        mRef.child(key).child("Recipe ID").setValue(key);
    }
}
