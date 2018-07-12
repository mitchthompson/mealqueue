package com.mitchlthompson.mealqueue;


import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mitchlthompson.mealqueue.helpers.Ingredient;
import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.squareup.timessquare.CalendarPickerView.SelectionMode.RANGE;
import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;

/**
 * This fragment is for syncing the grocery list with ingredients from the meal plan.
 * Includes a calendar with a date range selector. User selects the dates they want to sync then
 * database calls are made to get the meal plan for those dates then gets all the ingredients from
 * each recipe. Finally those ingredients are then added to that user's grocery list in the database.
 * @author Mitchell Thompson
 * @version 1.0
 * @see GroceryFragment
 */
public class SyncCalendarFragment extends Fragment {
    private static final String TAG = "SyncCalendarFragment";
    View viewer;
    private Context context;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;

    private DatabaseReference mRef;

    private Button doneBtn, cancelBtn;
    private List selectedDates;
    private ArrayList<String> formattedDates;
    private Map<String,Object> firebaseData;
    private ArrayList<String> recipeIDs, syncDates;
    private ArrayList<Ingredient> ingredients;

    public SyncCalendarFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewer = inflater.inflate(R.layout.fragment_calendar, container,
                false);
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

        Date today = new Date();
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        //date range selector and listener
        final CalendarPickerView calendar = viewer.findViewById(R.id.grocery_sync_calendar);
        calendar.init(today, nextYear.getTime())
                .inMode(RANGE);
        calendar.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                selectedDates = calendar.getSelectedDates();
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });

        formattedDates = new ArrayList<>();
        doneBtn = viewer.findViewById(R.id.done_button);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedDates == null){
                    Toast.makeText(context, "Please select a date range to sync", Toast.LENGTH_LONG).show();
                }else{
                    for(int i=0;i<selectedDates.size();i++){
                        formattedDates.add(DateFormat.getDateInstance(DateFormat.FULL).format(selectedDates.get(i)));
                    }
                    getData(formattedDates);
                }
            }
        });

        //if cancel button selected then returns user to GroceryFragment without changes
        cancelBtn = viewer.findViewById(R.id.sync_cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroceryFragment newFragment = new GroceryFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                fragmentTransaction.replace(R.id.main_frame, newFragment);
                fragmentTransaction.commit();

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStack();
            }
        });

        return viewer;

    }

    /**
     * Takes range of dates user selected then makes a database call add all recipeIDs within that
     * range to ArrayList named recipeIDs. Then calls method getRecipeIngredients to make database
     * call for the recipe ingredients for those recipeIDs.
     * @param selectedDates String ArrayList of the range of dates user selected
     */
    private void getData(ArrayList<String> selectedDates){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mRef = mFirebaseDatabase.getReference("/mealplans/" + userID);

        syncDates = selectedDates;

        mRef.addListenerForSingleValueEvent (new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firebaseData = (Map<String, Object>) dataSnapshot.getValue();
                recipeIDs = new ArrayList<>();
                if(firebaseData != null) {
                    for (Map.Entry<String, Object> entry : firebaseData.entrySet()) {
                        for(int i=0;i<syncDates.size();i++){
                            if(syncDates.get(i).equals(entry.getKey().toString())){
                                Map<String,String> singleDay = (HashMap) entry.getValue();
                                for(String key : singleDay.keySet()){
                                    recipeIDs.add(singleDay.get(key));
                                }
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

    /**
     * Makes database call to get all ingredients for recipe then adds them to ingredients ArrayList
     * @param recipeID the ID of the recipe
     */
    private void getRecipeIngredients(String recipeID){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mRef = mFirebaseDatabase.getReference("/recipes/" + userID + "/" + recipeID).child("Ingredients");

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ingredients = new ArrayList<>();
                if(dataSnapshot.exists()) {
                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                        String name = (String) messageSnapshot.child("name").getValue();
                        String amount = (String) messageSnapshot.child("amount").getValue();
                        ingredients.add(new Ingredient(name,amount));
                    }
                }

                addIngredientsToGroceryList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Makes a database call to add all ingredients in ingredients ArrayList to the grocery
     */
    private void addIngredientsToGroceryList(){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mRef = mFirebaseDatabase.getReference("/grocery/" + userID);

        for(int i=0;i <ingredients.size();i++){
            String key = mRef.push().getKey();
            mRef.child(key).child("Ingredient Name").setValue(capitalizeFully(ingredients.get(i).getName()));
            mRef.child(key).child("Ingredient Amount").setValue(ingredients.get(i).getAmount());

        }

        GroceryFragment newFragment = new GroceryFragment();
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fragmentTransaction.replace(R.id.main_frame, newFragment);
        fragmentTransaction.commit();
    }
}
