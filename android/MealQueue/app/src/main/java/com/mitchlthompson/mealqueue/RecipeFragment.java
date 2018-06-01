package com.mitchlthompson.mealqueue;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private Map<String,String> ingredientsMap;
    private ArrayList<String> ingredients;
    private ArrayAdapter<String> itemsAdapter;
    private String recipeID, recipeName, directions, ingredientName, ingredientAmount;
    private TextView recipeNameTextView, directionsTextview, ingredientsList;
    private Button recipeDelete, recipeEdit, recipeAddToMealPlan;
    private String removeMealDate, date;


    public RecipeFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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

        recipeNameTextView = view.findViewById(R.id.recipe_name_textView);
        directionsTextview = view.findViewById(R.id.recipe_directions_textView);
        ingredientsList = view.findViewById(R.id.ingredients_list);

        ingredientsMap = new HashMap<>();
        ingredients = new ArrayList<>();
//        itemsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, ingredients);
//        ListView listView = view.findViewById(R.id.ingredients_listView);
//        listView.setAdapter(itemsAdapter);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                recipe = (HashMap<String,Object>) dataSnapshot.getValue();
                if(recipe!=null) {

                    recipeNameTextView.setText(recipe.get("Recipe Name").toString());
                    directionsTextview.setText(recipe.get("Directions").toString());
                    String ingredientsString = "";
                    //Get ingredients map
                    ingredientsMap = (HashMap) recipe.get("Ingredients");
                    for (String key : ingredientsMap.keySet()) {
                        //iterate over key
                        //Log.d(TAG, ingredientsMap.get(key) + " " + key);
                        ingredientsString += ingredientsMap.get(key) + "  " + key + "\n\n";
//                        ingredients.add(ingredientsMap.get(key) + " " + key);
                    }
                    ingredientsList.setText(ingredientsString);
//                    itemsAdapter.notifyDataSetChanged();

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
                deleteRecipe();
                startActivity(new Intent(context, MainActivity.class)
                        .putExtra("Date", date));
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

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame, newFragment)
                        .commit();
            }
        });




        return view;
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
