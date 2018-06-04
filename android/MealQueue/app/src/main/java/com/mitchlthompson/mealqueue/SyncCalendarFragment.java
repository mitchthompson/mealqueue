package com.mitchlthompson.mealqueue;


import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.mitchlthompson.mealqueue.helpers.GrocerySyncHelper;
import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.squareup.timessquare.CalendarPickerView.SelectionMode.RANGE;

public class SyncCalendarFragment extends Fragment {
    private static final String TAG = "SyncCalendarFragment";

    View viewer;
    private Context context;

    private Button doneBtn, cancelBtn;
    private List selectedDates;
    private ArrayList<String> formattedDates;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private String userID;

    private Map<String,Object> firebaseData;
    private ArrayList<String> recipeIDs, ingredients, syncDates;

    public SyncCalendarFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewer = inflater.inflate(R.layout.fragment_calendar, container,
                false);
        context = getActivity();

        Date today = new Date();
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

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

                    GroceryFragment newFragment = new GroceryFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_frame, newFragment)
                            .commit();

                }

            }
        });

        cancelBtn = viewer.findViewById(R.id.sync_cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroceryFragment newFragment = new GroceryFragment();

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame, newFragment)
                        .commit();
            }
        });

        return viewer;

    }

    private void getData(ArrayList<String> selectedDates){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mRef = mFirebaseDatabase.getReference("/mealplans/" + userID);

        syncDates = selectedDates;

        mRef.addValueEventListener(new ValueEventListener() {
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
                    //Log.d(TAG,ingredientsMap.get(key)+" "+key);
                    ingredients.add(key+" \n    "+ ingredientsMap.get(key));
                }

                addIngredientsToGroceryList(ingredients);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addIngredientsToGroceryList(ArrayList<String> items){
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
