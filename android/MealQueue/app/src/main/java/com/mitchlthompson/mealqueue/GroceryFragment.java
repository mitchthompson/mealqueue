package com.mitchlthompson.mealqueue;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mitchlthompson.mealqueue.adapters.GroceryListAdapter;
import com.mitchlthompson.mealqueue.helpers.Ingredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;

/**
 * This fragment controls the UI for the grocery list. Uses a customized recylerview to display
 * each grocery list item. Each item has a button that has an onClickListener for deleting the
 * grocery item. Includes a button that links to SyncCalendarFragment to sync all ingredients from
 * meal plan to grocery list.
 *
 * @author Mitchell Thompson
 * @version 1.0
 * @see MainActivity
 * @see GroceryListAdapter
 * @see SyncCalendarFragment
 */
public class GroceryFragment extends Fragment {
    private static final String TAG = "GroceryFragment";
    private View view;
    private Context context;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private String userID;

    private RecyclerView recyclerView;
    private RelativeLayout relativeLayout;
    private GroceryListAdapter groceryListAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private EditText groceryItem;
    private EditText groceryItemAmount;
    private Button addItemBtn;
    private Button clearItemsBtn;
    private Button grocerySyncBtn;
    private ArrayList<Ingredient> ingredients;

    public GroceryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_grocery, container, false);
        context = getActivity();

        //Firebase variables for verifying user auth
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        //User auth listener. Returns user to login screen if not verified.
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged:signed_in: " + user.getUid());
                    //Toast.makeText(context,"Successfully signing in with: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                }else{
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    //Toast.makeText(context,"Successfully signed out", Toast.LENGTH_SHORT).show();
                }
            }
        };

        //sets OnClickListener on Sync button that replaces current fragment with SyncCalendarFragment
        grocerySyncBtn = view.findViewById(R.id.grocery_sync_btn);
        grocerySyncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SyncCalendarFragment newFragment = new SyncCalendarFragment();;
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.main_frame, newFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });


        //Allows users to enter new grocery item and amount manually then pushes to database
        mRef = mFirebaseDatabase.getReference("/grocery/" + userID);
        groceryItem = view.findViewById(R.id.grocery_item);
        groceryItemAmount = view.findViewById(R.id.grocery_item_amount);
        groceryItem.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        groceryItemAmount.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        addItemBtn = view.findViewById(R.id.add_item_btn);
        addItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm=(InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(),0);

                if(TextUtils.isEmpty(groceryItem.getText())) {
                    Toast.makeText(context, "Enter an item to add to the grocery list", Toast.LENGTH_LONG).show();
                }else if(TextUtils.isEmpty(groceryItemAmount.getText())){
                    Toast.makeText(context, "Enter the item amount", Toast.LENGTH_LONG).show();
                } else {
                    String key = mRef.push().getKey();
                    mRef.child(key).child("Ingredient Name").setValue(capitalizeFully(groceryItem.getText().toString()));
                    mRef.child(key).child("Ingredient Amount").setValue(groceryItemAmount.getText().toString());
                    groceryItem.setText("");
                    groceryItemAmount.setText("");
                }
            }
        });

        //holds all current ingredients
        ingredients = new ArrayList<>();

        //sets OnClickListener on button to clear all grocery items from adapter and database
        clearItemsBtn = view.findViewById(R.id.grocery_clear);
        clearItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sets AlertDialog to confirm user wants to clear grocery list
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
                mBuilder.setIcon(R.drawable.ic_remove_circle_outline_black_24dp);
                mBuilder.setTitle("Clear Grocery List");
                mBuilder.setMessage("Are you sure you want to remove all items from the grocery list?");
                mBuilder.setPositiveButton("YES, remove all items", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRef.removeValue();
                        ingredients.clear();
                        groceryListAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                mBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = mBuilder.create();
                alertDialog.show();
            }

        });

        //database call to get all grocery item names and amounts for user
        mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String name = dataSnapshot.child("Ingredient Name").getValue(String.class);
                String amount = dataSnapshot.child("Ingredient Amount").getValue(String.class);
                String id = dataSnapshot.getKey();
                ingredients.add(new Ingredient(name, amount, id));
                Collections.sort(ingredients, new Comparator<Ingredient>() {
                    public int compare(Ingredient r1, Ingredient r2) {
                        return r1.getName().compareTo(r2.getName());
                    }
                });
                groceryListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //groceryListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Passes all grocery item ingredients to GroceryListAdapter
        relativeLayout = view.findViewById(R.id.nav_grocery);
        recyclerView = view.findViewById(R.id.grocery_recycler);
        recyclerViewLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        groceryListAdapter = new GroceryListAdapter(context, userID, ingredients);
        recyclerView.setAdapter(groceryListAdapter);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

}
