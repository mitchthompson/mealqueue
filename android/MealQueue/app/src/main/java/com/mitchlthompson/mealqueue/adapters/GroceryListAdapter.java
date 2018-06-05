package com.mitchlthompson.mealqueue.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mitchlthompson.mealqueue.R;
import com.mitchlthompson.mealqueue.helpers.Ingredient;

import java.util.ArrayList;

public class GroceryListAdapter extends RecyclerView.Adapter<GroceryListAdapter.GroceryListViewHolder> {

    private Context context;
    private View view;
    private GroceryListViewHolder groceryListViewHolder;
    private LayoutInflater inflater;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private String userID;

    private ArrayList<Ingredient> ingredients;

    public GroceryListAdapter(Context newContext, String userID, ArrayList<Ingredient> ingredients) {
        this.context = newContext;
        inflater = LayoutInflater.from(context);
        this.userID = userID;
        this.ingredients = ingredients;
    }

    @Override
    public GroceryListViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        view = LayoutInflater.from(context).inflate(R.layout.grocery_items,parent,false);
        groceryListViewHolder = new GroceryListViewHolder(view);
        return groceryListViewHolder;
    }

    @Override
    public void onBindViewHolder(GroceryListViewHolder holder, final int position) {
        final Ingredient ingredient = ingredients.get(position);
        holder.groceryItem.setText(ingredients.get(position).getName());
        holder.groceryAmount.setText(ingredients.get(position).getAmount());
        holder.clearItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                userID = user.getUid();
                mRef = mFirebaseDatabase.getReference("/grocery/" + userID);
                mRef.child(ingredients.get(position).getId()).removeValue();
                ingredients.remove(position);
                notifyDataSetChanged();

            }
        });

    }



    @Override
    public int getItemCount() {
        return ingredients.size();
    }


    public static class GroceryListViewHolder extends RecyclerView.ViewHolder {
        public TextView groceryItem;
        public TextView groceryAmount;
        public Button clearItemBtn;
        public GroceryListViewHolder(View v) {
            super(v);
            clearItemBtn = v.findViewById(R.id.clear_item_btn);
            groceryItem = v.findViewById(R.id.grocery_item);
            groceryAmount = v.findViewById(R.id.grocery_amount);
        }
    }
}
