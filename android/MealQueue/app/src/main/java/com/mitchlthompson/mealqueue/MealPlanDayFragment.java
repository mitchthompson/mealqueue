package com.mitchlthompson.mealqueue;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mitchlthompson.mealqueue.adapters.MealPlanRecipeAdapter;
import com.mitchlthompson.mealqueue.helpers.Recipe;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;


public class MealPlanDayFragment extends Fragment {
    private static final String TAG = "MealPlanDayFragment";
    private View view;
    private Context context;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private String userID;
    private DatabaseReference mRef;

    private RecyclerView recyclerView;
    private MealPlanRecipeAdapter mealPlanRecipeAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private SearchView searchView;

    private ArrayList<Recipe> recipesList;
    private String date;
    private Button cancelBtn;

    private Map<String,Object> recipes;


    public MealPlanDayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_meal_plan_day, container, false);
        context = getActivity();

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mRef = mFirebaseDatabase.getReference("/recipes/" + userID);

        if (getArguments() != null) {
            date = getArguments().getString("Date");

        } else {
            Date today = new Date();
            date = DateFormat.getDateInstance(DateFormat.FULL).format(today);
            HomeFragment newFragment = new HomeFragment();
            Bundle bundle = new Bundle();
            bundle.putString("Date", date);
            newFragment.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, newFragment)
                    .commit();
        }

        cancelBtn = view.findViewById(R.id.meal_plan_day_cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeFragment newFragment = new HomeFragment();
                Bundle bundle = new Bundle();
                bundle.putString("Date", date);
                newFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame, newFragment)
                        .commit();
            }
        });
        searchView = view.findViewById(R.id.mealday_recipe_searchview);

        recipesList = new ArrayList<>();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                recipes = (Map<String,Object>) dataSnapshot.getValue();
                if(recipes!=null){
                    for (Map.Entry<String, Object> entry : recipes.entrySet()){
                        //Get user map
                        Map singleRecipe = (Map) entry.getValue();
                        //Get recipe name field and append to list
                        recipesList.add(new Recipe(singleRecipe.get("Recipe Name").toString(), singleRecipe.get("Recipe ID").toString()));
                    }
                    //sort recipeList alphabetically
                    Collections.sort(recipesList, new Comparator<Recipe>() {
                        public int compare(Recipe r1, Recipe r2) {
                            return r1.getName().compareTo(r2.getName());
                        }
                    });

                    recyclerView = view.findViewById(R.id.recipe_recycler);
                    recyclerViewLayoutManager = new LinearLayoutManager(context);
                    recyclerView.setLayoutManager(recyclerViewLayoutManager);

                    mealPlanRecipeAdapter = new MealPlanRecipeAdapter(context, date, recipesList);
                    recyclerView.setAdapter(mealPlanRecipeAdapter);

                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            mealPlanRecipeAdapter.getFilter().filter(newText);
                            return false;
                        }
                    });


                }else{

                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

}
