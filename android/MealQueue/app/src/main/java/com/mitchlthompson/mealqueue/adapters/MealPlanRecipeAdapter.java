package com.mitchlthompson.mealqueue.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mitchlthompson.mealqueue.R;
import com.mitchlthompson.mealqueue.RecipeActivity;

import java.util.ArrayList;

public class MealPlanRecipeAdapter extends RecyclerView.Adapter<MealPlanRecipeAdapter.RecipeViewHolder> {

    private Context context;
    private View view;
    private RecipeViewHolder recipeViewHolder;
    private LayoutInflater inflater;
    private ArrayList<String> recipeNames;
    private ArrayList<String> recipeIDs;
    private String date;
    private String weekStart;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private String userID;

    public MealPlanRecipeAdapter(Context newContext, String newWeekStart, String newDate, ArrayList<String> newNameData, ArrayList<String> newIDData) {
        this.context = newContext;
        inflater = LayoutInflater.from(context);
        this.weekStart = newWeekStart;
        this.date = newDate;
        this.recipeNames = newNameData;
        this.recipeIDs = newIDData;
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
        mRef = mFirebaseDatabase.getReference("/mealplans/" + userID + "/" + date);

        holder.recipeBtn.setText(recipeNames.get(position).toString());
        holder.recipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, recipeNames.get(position), Toast.LENGTH_SHORT).show();
                //String key = mRef.push().getKey();
                mRef.child(recipeNames.get(position)).setValue(recipeIDs.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeNames.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        public Button recipeBtn;
        public RecipeViewHolder(View v) {
            super(v);
            recipeBtn = v.findViewById(R.id.recipe_btn);
        }
    }
}