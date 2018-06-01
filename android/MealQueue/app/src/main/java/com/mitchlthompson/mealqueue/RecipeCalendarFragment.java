package com.mitchlthompson.mealqueue;


import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.util.Date;


public class RecipeCalendarFragment extends Fragment {
    private static final String TAG = "RecipeCalendarFragment";

    View viewer;
    private Context context;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private String userID;

    private CalendarPickerView calendarPickerView;
    private Button addToMealPlanBtn;
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

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        if (getArguments() != null) {
            recipeID = getArguments().getString("Recipe ID");
            recipeName = getArguments().getString("Recipe Name");
        }

        Date today = new Date();
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

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

        addToMealPlanBtn = viewer.findViewById(R.id.recipe_add_meal_done_button);
        addToMealPlanBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mRef = mFirebaseDatabase.getReference("/mealplans/" + userID + "/").child(selectedDate);
                if(selectedDate == null){
                    Toast.makeText(context, "Please select a date", Toast.LENGTH_LONG).show();
                } else {
                    mRef = mFirebaseDatabase.getReference("/mealplans/" + userID + "/").child(selectedDate);
                    mRef.child(recipeName).setValue(recipeID);
                    startActivity(new Intent(context, MainActivity.class)
                            .putExtra("Date", selectedDate));
                }



            }
        });
        return viewer;
    }

}
