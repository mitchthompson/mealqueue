package com.mitchlthompson.mealqueue;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private View view;
    private Context context;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private String userID;

    private DatabaseReference mRefMealPlan;
    private DatabaseReference mRefMealPlan2;

    private String todaysDate;
    private CalendarView calenderView;

    private Map<String,Object> mealPlanData;

    private TextView dateTextView;
    private TextView mealsTextView;
    private Button addPlanBtn;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);
        context = getActivity();

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        Calendar c = Calendar.getInstance();
        todaysDate = (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.YEAR);

        Log.d(TAG, "Date: " + todaysDate);

        dateTextView = view.findViewById(R.id.date_tv);
        dateTextView.setText(formatDate(todaysDate));
        mealsTextView = view.findViewById(R.id.meals_tv);
        addPlanBtn = view.findViewById(R.id.add_plan_btn);

        calenderView = view.findViewById(R.id.calendarView);

        addPlanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, MealPlanDayActivity.class)
                        .putExtra("Date", todaysDate));
            }
        });

        getMeals(todaysDate);

        calenderView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                todaysDate = (month + 1) + "-" + dayOfMonth + "-" + year;
                Log.d(TAG, "Date: " + todaysDate);
                dateTextView.setText(formatDate(todaysDate));
                mealsTextView.setText("");

                mRefMealPlan2 = mFirebaseDatabase.getReference("/mealplans/" + userID + "/").child(todaysDate);

                mRefMealPlan2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mealPlanData = (HashMap<String,Object>) dataSnapshot.getValue();
                        String meals = "";
                        if(mealPlanData!=null) {
                            for (String key : mealPlanData.keySet()) {
                                Log.d(TAG, key);
                                meals = meals + key + "\n\n";
                            }
                            if (meals != "") {
                                mealsTextView.setText(meals);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public String formatDate(String oldDate) {
        String date = "";
        String[] splitArray = oldDate.split("-");
        switch (Integer.parseInt(splitArray[0])) {
            case 1:
                date = "Jan";
                break;

            case 2:
                date = "Feb";
                break;

            case 3:
                date = "Mar";
                break;

            case 4:
                date = "Apr";
                break;

            case 5:
                date = "May";
                break;

            case 6:
                date = "June";
                break;

            case 7:
                date = "July";
                break;

            case 8:
                date = "Aug";
                break;

            case 9:
                date = "Sep";
                break;

            case 10:
                date = "Oct";
                break;

            case 11:
                date = "Nov";
                break;

            case 12:
                date = "Dec";
                break;


        }
        date = date + " " + splitArray[1];
        return date;
    }

    public void getMeals(String date) {
        mRefMealPlan = mFirebaseDatabase.getReference("/mealplans/" + userID + "/").child(date);

        mRefMealPlan.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mealPlanData = (HashMap<String,Object>) dataSnapshot.getValue();
                String meals = "";
                if(mealPlanData!=null) {
                    for (String key : mealPlanData.keySet()) {
                        Log.d(TAG, key);
                        meals = meals + key + "\n\n";
                    }
                    if (meals != "") {
                        mealsTextView.setText(meals);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
