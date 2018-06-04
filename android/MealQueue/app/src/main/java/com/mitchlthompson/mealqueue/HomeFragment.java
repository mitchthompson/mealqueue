package com.mitchlthompson.mealqueue;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private DatabaseReference mRemoveMeal;

    private String todaysDate;
    private Date today, dateSelected;
    private CalendarPickerView calendar;

    private Map<String,Object> mealPlanData;
    private Boolean threeMeals;

    private TextView dateTextView, dayDateTextView;
    private Button addPlanBtn, editMealPlanBtn;
    private TextView mealsTextView1, mealsTextView2, mealsTextView3;
    private CardView mealPlanCardView;
    private Button removeMeal1, removeMeal2, removeMeal3;

    private RecipeFragment recipeFragment;


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


        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.MONTH, 6);
        calendar = view.findViewById(R.id.meal_plan_calendar);
        today = new Date();

        if (getArguments() != null) {
            todaysDate = getArguments().getString("Date");

            String expectedPattern = "EEE, MMM d, yyyy";
            SimpleDateFormat formatter = new SimpleDateFormat(expectedPattern);
            try {
                dateSelected = formatter.parse(todaysDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            dateSelected = today;
        }

        dateTextView = view.findViewById(R.id.date_tv);
        dayDateTextView = view.findViewById(R.id.date_day_tv);

        String[] dateSplitArray = todaysDate.split(",");
        dayDateTextView.setText(dateSplitArray[0]);
        dateTextView.setText(dateSplitArray[1]);

        addPlanBtn = view.findViewById(R.id.add_plan_btn);
        getMeals(todaysDate);

        calendar.init(today, nextYear.getTime())
                .withSelectedDate(dateSelected);

        calendar.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                todaysDate = DateFormat.getDateInstance(DateFormat.FULL).format(date);
                String[] dateSplitArray = todaysDate.split(",");

                dayDateTextView.setText(dateSplitArray[0]);
                dateTextView.setText(dateSplitArray[1]);
                mealsTextView1.setText("");
                mealsTextView2.setText("");
                mealsTextView3.setText("");

                if (removeMeal1.getVisibility() == View.VISIBLE){
                    removeMeal1.setVisibility(View.INVISIBLE);
                }
                if (removeMeal2.getVisibility() == View.VISIBLE){
                    removeMeal2.setVisibility(View.INVISIBLE);
                }
                if (removeMeal3.getVisibility() == View.VISIBLE){
                    removeMeal3.setVisibility(View.INVISIBLE);
                }


                mRefMealPlan2 = mFirebaseDatabase.getReference("/mealplans/" + userID + "/").child(todaysDate);

                mRefMealPlan2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mealPlanData = (HashMap<String,Object>) dataSnapshot.getValue();
                        if(mealPlanData!=null) {
                            //Log.d(TAG, String.valueOf(mealPlanData.size()));
                            if(mealPlanData.size() >= 3){
                                threeMeals = true;
                            }else{
                                threeMeals = false;
                            }

                            Meals meals2 = new Meals();
                            for (Map.Entry<String, Object> entry : mealPlanData.entrySet()) {
                                String key = entry.getKey();
                                Object value = entry.getValue();
                                //Log.d(TAG, "Meal: " + key + " ID: " + value);
                                meals2.addMeal(value.toString(), key);
                            }
                            mealsTextView1.setText(meals2.getMealName(0));
                            mealsTextView1.setTag(meals2.getMealID(0));

                            mealsTextView2.setText(meals2.getMealName(1));
                            mealsTextView2.setTag(meals2.getMealID(1));

                            mealsTextView3.setText(meals2.getMealName(2));
                            mealsTextView3.setTag(meals2.getMealID(2));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onDateUnselected(Date date) {

           }
       });

        addPlanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mealsTextView3.getText().equals("")) {
                    MealPlanDayFragment newFragment = new MealPlanDayFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("Date", todaysDate);
                    newFragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_frame, newFragment)
                            .commit();
                }else{
                    Toast.makeText(context,"Only three meals per day allowed. Please remove one before adding a new one.", Toast.LENGTH_SHORT).show();

                }
            }
        });

        mealsTextView1 = view.findViewById(R.id.meals_tv_1);
        mealsTextView2 = view.findViewById(R.id.meals_tv_2);
        mealsTextView3 = view.findViewById(R.id.meals_tv_3);
        mealPlanCardView = view.findViewById(R.id.mealplan_cardview);
        removeMeal1 = view.findViewById(R.id.remove_meal_1);
        removeMeal2 = view.findViewById(R.id.remove_meal_2);
        removeMeal3 = view.findViewById(R.id.remove_meal_3);

        mealsTextView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecipeFragment recipeFragment = new RecipeFragment();
                Bundle recipeFragmentBundle = new Bundle();
                recipeFragmentBundle.putString("Recipe ID",  mealsTextView1.getTag().toString());
                recipeFragmentBundle.putString("Recipe Name", mealsTextView1.getText().toString());
                recipeFragmentBundle.putString("Date", todaysDate);
                recipeFragment.setArguments(recipeFragmentBundle);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame, recipeFragment)
                        .commit();

            }
        });

        mealsTextView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecipeFragment recipeFragment = new RecipeFragment();
                Bundle recipeFragmentBundle = new Bundle();
                recipeFragmentBundle.putString("Recipe ID",  mealsTextView2.getTag().toString());
                recipeFragmentBundle.putString("Recipe Name", mealsTextView2.getText().toString());
                recipeFragmentBundle.putString("Date", todaysDate);
                recipeFragment.setArguments(recipeFragmentBundle);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame, recipeFragment)
                        .commit();
            }
        });

        mealsTextView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecipeFragment recipeFragment = new RecipeFragment();
                Bundle recipeFragmentBundle = new Bundle();
                recipeFragmentBundle.putString("Recipe ID",  mealsTextView3.getTag().toString());
                recipeFragmentBundle.putString("Recipe Name", mealsTextView3.getText().toString());
                recipeFragmentBundle.putString("Date", todaysDate);
                recipeFragment.setArguments(recipeFragmentBundle);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame, recipeFragment)
                        .commit();
            }
        });

        editMealPlanBtn = view.findViewById(R.id.edit_meal_plan_btn);
        editMealPlanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(removeMeal1.getVisibility() == View.INVISIBLE &&
                            mealsTextView1.getText() != ""){
                        removeMeal1.setVisibility(View.VISIBLE);
                    }else if (removeMeal1.getVisibility() == View.VISIBLE){
                        removeMeal1.setVisibility(View.INVISIBLE);
                    }

                    if(removeMeal2.getVisibility() == View.INVISIBLE &&
                            mealsTextView2.getText() != ""){
                        removeMeal2.setVisibility(View.VISIBLE);
                    }else if (removeMeal2.getVisibility() == View.VISIBLE){
                        removeMeal2.setVisibility(View.INVISIBLE);
                    }

                    if(removeMeal3.getVisibility() == View.INVISIBLE &&
                            mealsTextView3.getText() != ""){
                        removeMeal3.setVisibility(View.VISIBLE);
                    }else if (removeMeal3.getVisibility() == View.VISIBLE){
                        removeMeal3.setVisibility(View.INVISIBLE);
                    }

            }
        });

        removeMeal1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = mealsTextView1.getText().toString();
                        removeMeal(name, todaysDate);
                        updateRemoveMealBtns();
                        mealsTextView1.setText("");
                    }
                });

        removeMeal2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mealsTextView2.getText().toString();
                removeMeal(name, todaysDate);
                updateRemoveMealBtns();
                mealsTextView2.setText("");
            }
        });

        removeMeal3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mealsTextView3.getText().toString();
                removeMeal(name, todaysDate);
                updateRemoveMealBtns();
                mealsTextView3.setText("");
            }
        });

        return view;
    }


    public void getMeals(String date) {
        mRefMealPlan = mFirebaseDatabase.getReference("/mealplans/" + userID + "/").child(date);

        mRefMealPlan.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mealPlanData = (HashMap<String,Object>) dataSnapshot.getValue();
                if(mealPlanData!=null) {
                    if(mealPlanData.size() >= 3){
                        threeMeals = true;
                    }else{
                        threeMeals = false;
                    }

                    Meals meals = new Meals();
                    for (Map.Entry<String, Object> entry : mealPlanData.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        meals.addMeal(value.toString(), key);
                    }

                    mealsTextView1.setText(meals.getMealName(0));
                    mealsTextView1.setTag(meals.getMealID(0));

                    mealsTextView2.setText(meals.getMealName(1));
                    mealsTextView2.setTag(meals.getMealID(1));

                    mealsTextView3.setText(meals.getMealName(2));
                    mealsTextView3.setTag(meals.getMealID(2));


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void removeMeal(final String recipeName, String date){
        mRemoveMeal = mFirebaseDatabase.getReference("/mealplans/" + userID + "/").child(date);

        mRemoveMeal.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    mRemoveMeal.child(recipeName).removeValue();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateRemoveMealBtns(){
        if(removeMeal1.getVisibility() == View.INVISIBLE &&
                mealsTextView1.getText() != ""){
            removeMeal1.setVisibility(View.VISIBLE);
        }else if (removeMeal1.getVisibility() == View.VISIBLE){
            removeMeal1.setVisibility(View.INVISIBLE);
        }

        if(removeMeal2.getVisibility() == View.INVISIBLE &&
                mealsTextView2.getText() != ""){
            removeMeal2.setVisibility(View.VISIBLE);
        }else if (removeMeal2.getVisibility() == View.VISIBLE){
            removeMeal2.setVisibility(View.INVISIBLE);
        }

        if(removeMeal3.getVisibility() == View.INVISIBLE &&
                mealsTextView3.getText() != ""){
            removeMeal3.setVisibility(View.VISIBLE);
        }else if (removeMeal3.getVisibility() == View.VISIBLE){
            removeMeal3.setVisibility(View.INVISIBLE);
        }
    }


    private class Meals{
        private ArrayList<String> ids;
        private ArrayList<String> names;

        Meals() {
            ids = new ArrayList<>();
            names = new ArrayList<>();
        }

        public void addMeal(String id, String name){
            ids.add(id);
            names.add(name);
        }

        public String getMealID(int index){
            if(ids.size()-1 < index){
                return "";
            }else{
                return ids.get(index);
            }
        }
        public String getMealName(int index){
            if(names.size()-1 < index){
                return "";
            }else {
                return names.get(index);
            }
        }

    }

}
