package com.mitchlthompson.mealqueue;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mitchlthompson.mealqueue.adapters.IngredientsAdapter;
import com.mitchlthompson.mealqueue.helpers.Ingredient;

import java.io.IOException;
import java.util.ArrayList;

import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;

/**
 * This activity takes form inputs for recipe name, directions, picture, and ingredients then
 * makes a database call to add them as a new recipe in user's recipes. Includes form validation
 * for all fields. Also includes a recyclerview for ingredients.
 *
 * @author Mitchell Thompson
 * @version 1.0
 * @see IngredientsAdapter
 */
public class AddRecipeActivity extends AppCompatActivity {
    private static final String TAG = "AddRecipeActivity";
    private Context context;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;

    private DatabaseReference mRef;

    private TextView recipeNameInput, directionsInput;
    private String recipeName, directions;
    private Button nextBtn, importBtn, addIngredientBtn;

    private RecyclerView recyclerView;
    private IngredientsAdapter ingredientsAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private ArrayList<Ingredient> ingredients;
    private EditText ingredientItem, ingredientAmount;

    private Button btnChoose;
    private ImageView imageView;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Boolean imageSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        context = getApplicationContext();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

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

        mRef = mFirebaseDatabase.getReference("/recipes/" + userID);

        recipeNameInput = findViewById(R.id.edit_name_input);
        directionsInput = findViewById(R.id.edit_directions_input);

        ingredients = new ArrayList<>();

        /**
         * Sets OnClickListener for button to Add Recipe. Checks for recipe name, directions, and
         * if at least one ingredient was added. If yes then calls method AddRecipe to add recipe to
         * database.
         */
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

        //On click of import button launches ImportActivity
        importBtn = findViewById(R.id.import_btn);
        importBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddRecipeActivity.this, ImportActivity.class));
            }
        });

        imageSelected = false;
        btnChoose = findViewById(R.id.chose_recipe_pic_btn);
        imageView = findViewById(R.id.imgView);
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        ingredientItem = findViewById(R.id.edit_ingredient_item_EditText);
        ingredientAmount = findViewById(R.id.edit_ingredient_amount_EditText);


        /**
         * Click Listener to add user inputted ingredient ingredients ArrayList then notifies
         * IngredientAdapter of change.
         */
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

        /**
         * Passes ingredients Arraylist to the recyclerview IngredientAdapter to list ingredients
         */
        context = getApplicationContext();
        recyclerView = findViewById(R.id.edit_ingredient_recycler);
        recyclerViewLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        ingredientsAdapter = new IngredientsAdapter(context, ingredients);
        recyclerView.setAdapter(ingredientsAdapter);

    }

    /**
     * Makes database call to add recipe to user's recipes then starts MainActivity
     */
    private void AddRecipe(){
        recipeName = recipeNameInput.getText().toString();
        directions = directionsInput.getText().toString();
        String key = mRef.push().getKey();
        mRef.child(key).child("Recipe Name").setValue(capitalizeFully(recipeName));
        mRef.child(key).child("Directions").setValue(directions);
        mRef.child(key).child("Ingredients").setValue(ingredients);
        mRef.child(key).child("Recipe ID").setValue(key);
        Log.d(TAG, imageSelected.toString());
        if(imageSelected){
            uploadImage(key);
        }else{
            startActivity(new Intent(AddRecipeActivity.this, MainActivity.class)
                    .putExtra("New Recipe", recipeName));
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
                imageSelected = true;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(String recipeID) {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/"+ recipeID);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            startActivity(new Intent(AddRecipeActivity.this, MainActivity.class)
                                    .putExtra("New Recipe", recipeName));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddRecipeActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }
}
