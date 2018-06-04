package com.mitchlthompson.mealqueue.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mitchlthompson.mealqueue.HomeFragment;
import com.mitchlthompson.mealqueue.MainActivity;
import com.mitchlthompson.mealqueue.R;
import com.mitchlthompson.mealqueue.RecipeFragment;
import com.mitchlthompson.mealqueue.helpers.MealPlanDayFilterHelper;
import com.mitchlthompson.mealqueue.helpers.Recipe;
import com.mitchlthompson.mealqueue.helpers.RecipesFilterHelper;

import java.util.ArrayList;

public class MealPlanRecipeAdapter extends RecyclerView.Adapter<MealPlanRecipeAdapter.RecipeViewHolder> implements Filterable{

    private Context context;
    private View view;
    private RecipeViewHolder recipeViewHolder;
    private LayoutInflater inflater;
    private ArrayList<Recipe> recipesList;
    private String date;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private String userID;

    //used for search & filter
    ArrayList<Recipe> currentList;

    public MealPlanRecipeAdapter(Context newContext, String newDate, ArrayList<Recipe> newRecipesList) {
        this.context = newContext;
        inflater = LayoutInflater.from(context);
        this.date = newDate;
        this.recipesList = newRecipesList;
        this.currentList = recipesList;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        view = LayoutInflater.from(context).inflate(R.layout.recipe_items,parent,false);
        recipeViewHolder = new RecipeViewHolder(view);
        return recipeViewHolder;
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, final int position) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mRef = mFirebaseDatabase.getReference("/mealplans/" + userID + "/").child(date);

        holder.recipeBtn.setText(recipesList.get(position).getName());
        holder.recipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRef.child(recipesList.get(position).getName()).setValue(recipesList.get(position).getId());

                HomeFragment homeFragment = new HomeFragment();
                Bundle fragmentBundle = new Bundle();
                fragmentBundle.putString("Date",  date);
                homeFragment.setArguments(fragmentBundle);

                AppCompatActivity activity = (AppCompatActivity) view.getContext();

                FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_frame, homeFragment);
                fragmentTransaction.commit();

                //context.startActivity(new Intent(context, MainActivity.class).putExtra("Date", date));
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipesList.size();
    }

    @Override
    public Filter getFilter() {
        return MealPlanDayFilterHelper.newInstance(currentList, this);
    }

    public void setRecipeName(ArrayList<Recipe> filteredRecipeName){
        this.recipesList = filteredRecipeName;
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        public Button recipeBtn;
        public RecipeViewHolder(View v) {
            super(v);
            recipeBtn = v.findViewById(R.id.recipe_btn);
        }
    }
}