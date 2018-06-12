package com.mitchlthompson.mealqueue;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mitchlthompson.mealqueue.helpers.Ingredient;

import java.util.ArrayList;
import java.util.HashMap;

import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;


public class RecipeFragment extends Fragment {
    private static final String TAG = "RecipeFragment";

    View view;

    private Context context;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private String userID;

    private HashMap<String,Object> recipe;
    private String recipeID, recipeName, directions, ingredientName, ingredientAmount;
    private TextView recipeNameTextView, directionsTextview, ingredientsList;
    private Button recipeDelete, recipeEdit, recipeAddToMealPlan;
    private String removeMealDate, date;

    private ArrayList<Ingredient> ingredients;

    private ImageView image;
    private StorageReference storageReference;



    public RecipeFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recipe, container, false);

        context = getActivity();

        if (getArguments() != null) {
            recipeID = getArguments().getString("Recipe ID");
            recipeName = getArguments().getString("Recipe Name");
            date = getArguments().getString("Date");
        }

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mRef = mFirebaseDatabase.getReference("/recipes/" + userID + "/" + recipeID);

        recipeNameTextView = view.findViewById(R.id.edit_name_textView);
        directionsTextview = view.findViewById(R.id.recipe_directions_textView);
        ingredientsList = view.findViewById(R.id.ingredients_list);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                recipe = (HashMap<String,Object>) dataSnapshot.getValue();
                if(recipe!=null) {

                    recipeNameTextView.setText(recipe.get("Recipe Name").toString());
                    directionsTextview.setText(recipe.get("Directions").toString());
                    getIngredients();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        recipeDelete = view.findViewById(R.id.recipe_delete);
        recipeDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
                mBuilder.setIcon(R.drawable.ic_remove_circle_outline_black_24dp);
                mBuilder.setTitle("Delete " + recipeName);
                mBuilder.setMessage("Are you sure you want to delete this recipe? It cannot be undone.");
                mBuilder.setPositiveButton("YES, delete recipe", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteRecipe();

                        RecipesFragment newFragment = new RecipesFragment();
                        if(date!=null){
                            Bundle bundle = new Bundle();
                            bundle.putString("Date", date);
                            newFragment.setArguments(bundle);
                        }


                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.setCustomAnimations(R.animator.slide_up,
                                R.animator.slide_down,
                                R.animator.slide_up,
                                R.animator.slide_down);

                        fragmentTransaction.replace(R.id.main_frame, newFragment);
                        fragmentTransaction.commit();

                        dialog.dismiss();
                    }
                });
                mBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = mBuilder.create();
                alertDialog.show();

            }
        });

        recipeAddToMealPlan = view.findViewById(R.id.recipe_add_meal);
        recipeAddToMealPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecipeCalendarFragment newFragment = new RecipeCalendarFragment();
                Bundle bundle = new Bundle();
                bundle.putString("Recipe ID", recipeID);
                bundle.putString("Recipe Name", recipeName);
                bundle.putString("Date", date);
                newFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.animator.slide_up,
                        R.animator.slide_down,
                        R.animator.slide_up,
                        R.animator.slide_down);

                fragmentTransaction.replace(R.id.main_frame, newFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        recipeEdit = view.findViewById(R.id.recipe_edit);
        recipeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, EditRecipeActivity.class)
                        .putExtra("Recipe ID", recipeID));
            }
        });



        storageReference = FirebaseStorage.getInstance().getReference().child("/images/"+recipeID);
        image = view.findViewById(R.id.recipe_imageView);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                image.setVisibility(View.INVISIBLE);
                image.getLayoutParams().height = 50;
                image.requestLayout();
            }
        });




        return view;
    }

    private void getIngredients(){
        mRef = mFirebaseDatabase.getReference("/recipes/" + userID + "/" + recipeID).child("Ingredients");
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String ingredientsString = "";
                    for(DataSnapshot messageSnapshot: dataSnapshot.getChildren()){
                        String name = (String) messageSnapshot.child("name").getValue();
                        name = capitalizeFully(name);
                        String amount = (String) messageSnapshot.child("amount").getValue();
                        ingredientsString += name + "  (" + amount + ")\n\n";
                    }
                    ingredientsList.setText(ingredientsString);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
