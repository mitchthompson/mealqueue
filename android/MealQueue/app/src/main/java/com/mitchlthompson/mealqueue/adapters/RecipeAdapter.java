package com.mitchlthompson.mealqueue.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mitchlthompson.mealqueue.R;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private Context context;
    private View view;
    private RecipeViewHolder recipeViewHolder;
    private LayoutInflater inflater;
    private ArrayList<String> recipeList = new ArrayList();

    public RecipeAdapter(Context newContext, ArrayList<String> newData) {
        this.context = newContext;
        inflater = LayoutInflater.from(context);
        this.recipeList = newData;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        view = LayoutInflater.from(context).inflate(R.layout.recipe_items,parent,false);
        recipeViewHolder = new RecipeViewHolder(view);
        return recipeViewHolder;
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        holder.itemTextView.setText(recipeList.get(position));
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        public TextView itemTextView;
        public RecipeViewHolder(View v) {
            super(v);
            itemTextView = (TextView) v.findViewById(R.id.recipe_item);
        }
    }
}