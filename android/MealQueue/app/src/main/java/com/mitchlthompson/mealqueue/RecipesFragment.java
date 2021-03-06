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
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mitchlthompson.mealqueue.adapters.RecipeAdapter;
import com.mitchlthompson.mealqueue.helpers.Recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * This fragment controls the UI for the recipes list. Uses a customized recylerview to display
 * each recipe. Includes a searchview that uses a filterhelper to filter search results. Each recipe
 * contains an onClickListener that sends users to the RecipeFragment displaying that recipe.
 *
 * @author Mitchell Thompson
 * @version 1.0
 * @see MainActivity
 * @see com.mitchlthompson.mealqueue.helpers.RecipesFilterHelper
 * @see RecipeAdapter
 * @see RecipeFragment
 */
public class RecipesFragment extends Fragment {
    private static final String TAG = "RecipesFragment";
    private Context context;
    private View view;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private String userID;

    private RecyclerView recyclerView;
    private RelativeLayout relativeLayout;
    private RecipeAdapter recipeAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private SearchView searchView;
    private ArrayList<Recipe> recipesList;
    private Button addRecipeBtn;
    private Map<String,Object> recipes;

    public RecipesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_recipes, container, false);
        context = getActivity();

        //Firebase variables for verifying user auth
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        //User auth listener. Returns user to login screen if not verified.
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


        searchView = view.findViewById(R.id.recipes_searchView);

        //Set onClickListener for addRecipeButton. Launches AddRecipeActivity
        addRecipeBtn = view.findViewById(R.id.launch_addrecipe_btn);
        addRecipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context.getApplicationContext(), AddRecipeActivity.class));

            }
        });

        /**
         * Makes call to database for user's recipes then passes them to recyclerview RecipeAdapter.
         * Also sets OnQueryTextListener on searchview to filter search results.
         * @see RecipeAdapter
         * @see com.mitchlthompson.mealqueue.helpers.RecipesFilterHelper
         */
        mRef = mFirebaseDatabase.getReference("/recipes/" + userID);
        //mRef = mFirebaseDatabase.getReference("/recipes/default");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    recipesList = new ArrayList<>();
                    recipes = (Map<String,Object>) dataSnapshot.getValue();
                    if(recipes!= null) {
                        for (Map.Entry<String, Object> entry : recipes.entrySet()) {
                            //Get user map
                            Map singleRecipe = (Map) entry.getValue();
                                Object name = singleRecipe.get("Recipe Name");
                                Object id = singleRecipe.get("Recipe ID");
                                if(name!=null && id !=null){
                                    recipesList.add(new Recipe(name.toString(), id.toString()));
                                }

                            }

                        Collections.sort(recipesList, new Comparator<Recipe>() {
                            public int compare(Recipe r1, Recipe r2) {
                                return r1.getName().compareTo(r2.getName());
                            }
                        });

                        relativeLayout = view.findViewById(R.id.nav_recipes);
                        recyclerView = view.findViewById(R.id.recipe_recycler);
                        recyclerViewLayoutManager = new LinearLayoutManager(context);
                        recyclerView.setLayoutManager(recyclerViewLayoutManager);

                        recipeAdapter = new RecipeAdapter(context, recipesList);
                        recyclerView.setAdapter(recipeAdapter);

                        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String query) {
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(String newText) {
                                recipeAdapter.getFilter().filter(newText);
                                return false;
                            }
                        });
                    }else{
                        Log.d(TAG, "No Recipes found.");
                    }

                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

}
