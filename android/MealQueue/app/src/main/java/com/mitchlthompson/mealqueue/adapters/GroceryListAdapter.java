package com.mitchlthompson.mealqueue.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mitchlthompson.mealqueue.R;

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

    private ArrayList<String> itemNames = new ArrayList();
    private ArrayList<String> itemIDs = new ArrayList();

    public GroceryListAdapter(Context newContext, String userID, ArrayList<String> names, ArrayList<String> ids) {
        this.context = newContext;
        inflater = LayoutInflater.from(context);
        this.userID = userID;
        this.itemNames = names;
        this.itemIDs = ids;
    }

    @Override
    public GroceryListViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        view = LayoutInflater.from(context).inflate(R.layout.grocery_items,parent,false);
        groceryListViewHolder = new GroceryListViewHolder(view);
        return groceryListViewHolder;
    }

    @Override
    public void onBindViewHolder(GroceryListViewHolder holder, final int position) {
        holder.textView.setText(itemNames.get(position));
        holder.clearItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                userID = user.getUid();
                mRef = mFirebaseDatabase.getReference("/grocery/" + userID);
                mRef.child(itemIDs.get(position)).removeValue();
                itemIDs.remove(position);
                itemNames.remove(position);
                notifyDataSetChanged();

            }
        });
    }



    @Override
    public int getItemCount() {
        return itemNames.size();
    }

    public static class GroceryListViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public Button clearItemBtn;
        public GroceryListViewHolder(View v) {
            super(v);
            clearItemBtn = v.findViewById(R.id.clear_item_btn);
            textView = v.findViewById(R.id.grocery_item);
        }
    }
}
