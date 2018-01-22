package com.mitchlthompson.mealqueue.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mitchlthompson.mealqueue.R;

import java.util.ArrayList;

public class GroceryListAdapter extends RecyclerView.Adapter<GroceryListAdapter.GroceryListViewHolder> {

    private Context context;
    private View view;
    private GroceryListViewHolder groceryListViewHolder;
    private LayoutInflater inflater;
    private ArrayList<String> groceryList = new ArrayList();

    public GroceryListAdapter(Context newContext, ArrayList<String> newData) {
        this.context = newContext;
        inflater = LayoutInflater.from(context);
        this.groceryList = newData;
    }

    @Override
    public GroceryListViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        view = LayoutInflater.from(context).inflate(R.layout.grocery_items,parent,false);
        groceryListViewHolder = new GroceryListViewHolder(view);
        return groceryListViewHolder;
    }

    @Override
    public void onBindViewHolder(GroceryListViewHolder holder, int position) {
        holder.itemTextView.setText(groceryList.get(position));
    }

    @Override
    public int getItemCount() {
        return groceryList.size();
    }

    public static class GroceryListViewHolder extends RecyclerView.ViewHolder {
        public TextView itemTextView;
        public GroceryListViewHolder(View v) {
            super(v);
            itemTextView = (TextView) v.findViewById(R.id.grocery_item);
        }
    }
}
