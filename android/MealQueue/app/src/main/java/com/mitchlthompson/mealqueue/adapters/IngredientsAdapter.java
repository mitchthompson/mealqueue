package com.mitchlthompson.mealqueue.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mitchlthompson.mealqueue.R;

import java.util.ArrayList;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.IngredientsViewHolder> {

    private Context context;
    private View view;
    private IngredientsViewHolder ingredientsViewHolder;
    private LayoutInflater inflater;

    private ArrayList<String> itemNames = new ArrayList();
    private ArrayList<String> itemAmounts = new ArrayList();

    public IngredientsAdapter(Context newContext, ArrayList<String> names, ArrayList<String> amounts) {
        this.context = newContext;
        inflater = LayoutInflater.from(context);
        this.itemNames = names;
        this.itemAmounts = amounts;
    }

    @Override
    public IngredientsViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        view = LayoutInflater.from(context).inflate(R.layout.ingredient_items,parent,false);
        ingredientsViewHolder = new IngredientsViewHolder(view);
        return ingredientsViewHolder;
    }

    @Override
    public void onBindViewHolder(final IngredientsViewHolder holder, int position) {
        //Log.d("TAG", itemNames.get(position));
        holder.ingredientsItem.setText(itemNames.get(position));
        holder.ingredientsAmount.setText(itemAmounts.get(position));
        holder.clearIngredientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemNames.remove(holder.getAdapterPosition());
                itemAmounts.remove(holder.getAdapterPosition());
                notifyDataSetChanged();
            }
        });
    }

    public void addItem(String name, String amount)
    {
        itemNames.add(name);
        itemAmounts.add(amount);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return itemNames.size();
    }

    public static class IngredientsViewHolder extends RecyclerView.ViewHolder {
        public TextView ingredientsItem;
        public TextView ingredientsAmount;
        public Button clearIngredientBtn;
        public IngredientsViewHolder(View v) {
            super(v);
            clearIngredientBtn = v.findViewById(R.id.clear_ingredient_btn);
            ingredientsItem = v.findViewById(R.id.indy_ingredients_item);
            ingredientsAmount = v.findViewById(R.id.ingredient_amount);
        }
    }
}
