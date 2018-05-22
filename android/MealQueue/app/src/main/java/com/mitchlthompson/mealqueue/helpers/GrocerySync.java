package com.mitchlthompson.mealqueue.helpers;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GrocerySync {
    private static final String TAG = "GrocerySync";

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private String userID;

    private String startDate;
    private String endDate;
    private Map<String,Object> firebaseData;
    private ArrayList<String> recipeIDs;
    private ArrayList<String> groceryItems;
    private ArrayList<String> ingredients;

    public GrocerySync(){
    }

    public void getData(String start, String end){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mRef = mFirebaseDatabase.getReference("/mealplans/" + userID);

        startDate = "5-19-2018";
        endDate = "5-20-2018";

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firebaseData = (Map<String, Object>) dataSnapshot.getValue();
                recipeIDs = new ArrayList<>();
                if(firebaseData != null) {
                    for (Map.Entry<String, Object> entry : firebaseData.entrySet()) {
                        if(startDate.equals(entry.getKey().toString())){
                            Map<String,String> singleDay = (HashMap) entry.getValue();
                            for(String key : singleDay.keySet()){
                                recipeIDs.add(singleDay.get(key));
                            }
                        }
                    }
                }else{
                    Log.d(TAG, "No ingredients found.");
                }
                for(int i=0;i<recipeIDs.size();i++){
                    getRecipeIngredients(recipeIDs.get(i));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

    private void getRecipeIngredients(String recipeID){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mRef = mFirebaseDatabase.getReference("/recipes/" + userID + "/" + recipeID);


        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                HashMap<String,Object> recipe = (HashMap<String,Object>) dataSnapshot.getValue();

                ingredients = new ArrayList<>();
                //Get ingredients map
                Map<String,String> ingredientsMap = (HashMap) recipe.get("Ingredients");
                for (String key : ingredientsMap.keySet()){
                    Log.d(TAG,ingredientsMap.get(key)+" "+key);
                    ingredients.add(ingredientsMap.get(key)+" "+key);
                }

                addIngredientsToFirebase(ingredients);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addIngredientsToFirebase(ArrayList<String> items){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mRef = mFirebaseDatabase.getReference("/grocery/" + userID);

        for(int i=0;i <items.size();i++){
            //Log.d(TAG, "addIngredients: " + groceryItems.get(i));
            mRef.push().setValue(items.get(i));
        }

    }
}