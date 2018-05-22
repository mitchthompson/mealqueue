package com.mitchlthompson.mealqueue;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class RecipesFragment extends Fragment {
    private static final String TAG = "RecipesFragment";
    private Context context;
    private View view;

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


    public RecipesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_recipes, container, false);
        context = getActivity();

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
                    Toast.makeText(context,"Successfully signing in with: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                }else{
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Toast.makeText(context,"Successfully signed out", Toast.LENGTH_SHORT).show();
                }
            }
        };

        mRef = mFirebaseDatabase.getReference("/recipes/" + userID);

//        Log.d(TAG, "Firebase URL: " + mRef);
//        Log.d(TAG, "User ID: " + userID);


        addRecipeBtn = view.findViewById(R.id.launch_addrecipe_btn);
        addRecipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, AddRecipeActivity.class));

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
                        recipeNames.add((singleRecipe.get("GrocerySync Name").toString()));
                        recipeIDs.add(singleRecipe.get("GrocerySync ID").toString());
                        Log.d(TAG, " recipe name: " + singleRecipe.get("GrocerySync Name").toString()
                                + " recipeID: " + singleRecipe.get("GrocerySync ID").toString());

                        relativeLayout = view.findViewById(R.id.nav_recipes);
                        recyclerView = view.findViewById(R.id.recipe_recycler);
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

        return view;
    }

}
