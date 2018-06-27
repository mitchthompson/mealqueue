package com.mitchlthompson.mealqueue;


import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * This fragment is for adding a recipe to the meal plan when the user clicks the 'Meal Plan +'
 * on the RecipeFragment. When user selects a date then a database call is made to add the
 * recipe to the meal for that date.
 * @author Mitchell Thompson
 * @version 1.0
 * @see RecipeFragment
 */
public class RecipeCalendarFragment extends Fragment {
    private static final String TAG = "RecipeCalendarFragment";
    View viewer;
    private Context context;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;

    private DatabaseReference mRef;

    private CalendarPickerView calendarPickerView;
    private Button addToMealPlanBtn, addCancelBtn;
    private String selectedDate, recipeID, recipeName;

    public RecipeCalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewer = inflater.inflate(R.layout.fragment_recipe_calendar, container,
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


        //checks for recipe details passed in through bundle then assigns them to variables
        if (getArguments() != null) {
            recipeID = getArguments().getString("Recipe ID");
            recipeName = getArguments().getString("Recipe Name");
        }


        //sets up calendar starting with the current date and OnClickListener
        Date today = new Date();
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);
        selectedDate = DateFormat.getDateInstance(DateFormat.FULL).format(today);
        calendarPickerView = viewer.findViewById(R.id.recipe_add_meal_calendar);
        calendarPickerView.init(today, nextYear.getTime())
                .withSelectedDate(today);
        calendarPickerView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                selectedDate = DateFormat.getDateInstance(DateFormat.FULL).format(date);
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });


        /**
         * Sets OnClickListener for AddToMealPlan button that makes database call to add
         * recipe to the meal plan for the selected date on the calendar then replaces current
         * fragment with RecipeFragment.
          */
        addToMealPlanBtn = viewer.findViewById(R.id.recipe_add_meal_done_button);
        addToMealPlanBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                    mRef = mFirebaseDatabase.getReference("/mealplans/" + userID + "/").child(selectedDate);
                    mRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            HashMap<String,Object> mealPlanData = (HashMap<String,Object>) dataSnapshot.getValue();
                            if(dataSnapshot.exists()) {
                                if(mealPlanData.size() >= 3){
                                    Toast.makeText(context, selectedDate + " already has three meals planned.", Toast.LENGTH_LONG).show();
                                }else{
                                    mRef.child(recipeName).setValue(recipeID);
                                }
                            }else{
                                mRef.child(recipeName).setValue(recipeID);
                            }

                            RecipeFragment newFragment = new RecipeFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("Recipe ID", recipeID);
                            bundle.putString("Recipe Name", recipeName);
                            newFragment.setArguments(bundle);

                            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                            fragmentTransaction.replace(R.id.main_frame, newFragment)
                                    .commit();

                            Toast.makeText(context, recipeName + " added to meal plan on " + selectedDate, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
        });

        //Cancel button return user to RecipeFragment without changes
        addCancelBtn = viewer.findViewById(R.id.recipe_add_meal_cancel_btn);
        addCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecipeFragment newFragment = new RecipeFragment();
                Bundle bundle = new Bundle();
                bundle.putString("Recipe ID", recipeID);
                bundle.putString("Recipe Name", recipeName);
                newFragment.setArguments(bundle);

                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                fragmentTransaction.replace(R.id.main_frame, newFragment)
                        .commit();
            }
        });
        return viewer;
    }

}
