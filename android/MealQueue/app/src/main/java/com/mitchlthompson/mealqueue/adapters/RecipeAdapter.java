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

import com.mitchlthompson.mealqueue.R;
import com.mitchlthompson.mealqueue.RecipeActivity;
import com.mitchlthompson.mealqueue.RecipeFragment;
import com.mitchlthompson.mealqueue.helpers.RecipesFilterHelper;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> implements Filterable{

    private Context context;
    private View view;
    private RecipeViewHolder recipeViewHolder;
    private LayoutInflater inflater;
    private ArrayList<String> recipeNames;
    private ArrayList<String> recipeIDs;

    //used for search & filter
    ArrayList<String> currentList;

    public RecipeAdapter(Context newContext, ArrayList<String> newNameData, ArrayList<String> newIDData) {
        this.context = newContext;
        inflater = LayoutInflater.from(context);
        this.recipeNames = newNameData;
        this.recipeIDs = newIDData;
        this.currentList = recipeNames;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        view = LayoutInflater.from(context).inflate(R.layout.recipe_items,parent,false);
        recipeViewHolder = new RecipeViewHolder(view);
        return recipeViewHolder;
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, final int position) {
        holder.recipeBtn.setText(recipeNames.get(position));
        holder.recipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecipeFragment recipeFragment = new RecipeFragment();
                Bundle recipeFragmentBundle = new Bundle();
                recipeFragmentBundle.putString("Recipe ID",  recipeIDs.get(position));
                recipeFragmentBundle.putString("Recipe Name", recipeNames.get(position));
                recipeFragment.setArguments(recipeFragmentBundle);

                AppCompatActivity activity = (AppCompatActivity) view.getContext();

                FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_frame, recipeFragment);
                fragmentTransaction.commit();



            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeNames.size();
    }

    @Override
    public Filter getFilter() {
        return RecipesFilterHelper.newInstance(currentList, this);
    }

    public void setRecipeName(ArrayList<String> filteredRecipeName){
        this.recipeNames = filteredRecipeName;
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        public Button recipeBtn;
        public RecipeViewHolder(View v) {
            super(v);
            recipeBtn = v.findViewById(R.id.recipe_btn);
        }
    }
}