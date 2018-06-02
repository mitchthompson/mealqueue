package com.mitchlthompson.mealqueue;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.mitchlthompson.mealqueue.helpers.GrocerySyncHelper;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroceryFragment extends Fragment {
    private static final String TAG = "GroceryFragment";
    private View view;
    private Context context;

    private RecyclerView recyclerView;
    private RelativeLayout relativeLayout;
    private GroceryListAdapter groceryListAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private ArrayList<String> itemNames;
    private ArrayList<String> itemIDs;
    private EditText groceryItem;
    private EditText groceryItemAmount;
    private Button addItemBtn;
    private Button clearItemsBtn;
    private Button grocerySyncBtn;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private String userID;

    private GrocerySyncHelper gSync;
    private ArrayList<String> recipeIDs;
    private ArrayList<String> groceryItems;


    public GroceryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_grocery, container, false);
        context = getActivity();

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mRef = mFirebaseDatabase.getReference("/grocery/" + userID);

//        mAuthListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser user = mAuth.getCurrentUser();
//                if(user != null){
//                    Log.d(TAG, "onAuthStateChanged:signed_in: " + user.getUid());
//                    //Toast.makeText(context,"Successfully signing in with: " + user.getEmail(), Toast.LENGTH_SHORT).show();
//                }else{
//                    Log.d(TAG, "onAuthStateChanged:signed_out");
//                    //Toast.makeText(context,"Successfully signed out", Toast.LENGTH_SHORT).show();
//                }
//            }
//        };

        Log.d(TAG, userID);

        itemNames = new ArrayList<>();
        itemIDs = new ArrayList<>();
        gSync = new GrocerySyncHelper();

        grocerySyncBtn = view.findViewById(R.id.grocery_sync_btn);
        grocerySyncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SyncCalendarFragment syncCalendarFragment = new SyncCalendarFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame, syncCalendarFragment)
                        .commit();

            }
        });


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
                    mRef.push().setValue(groceryItem.getText().toString()
                            + "\n" + groceryItemAmount.getText().toString());
                    groceryItem.setText("");
                    groceryItemAmount.setText("");
                }
            }
        });

        clearItemsBtn = view.findViewById(R.id.grocery_clear);
        clearItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
                mBuilder.setIcon(R.drawable.ic_remove_circle_outline_black_24dp);
                mBuilder.setTitle("Clear Grocery List");
                mBuilder.setMessage("Are you sure you want to remove all items from the grocery list?");
                mBuilder.setPositiveButton("YES, remove all items", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRef.removeValue();
                        itemIDs.clear();
                        itemNames.clear();
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

        mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String value = dataSnapshot.getValue(String.class);
                String key = dataSnapshot.getKey();
                itemIDs.add(key);
                itemNames.add(value);
                groceryListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        relativeLayout = view.findViewById(R.id.nav_grocery);
        recyclerView = view.findViewById(R.id.grocery_recycler);
        recyclerViewLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        groceryListAdapter = new GroceryListAdapter(context, userID, itemNames, itemIDs);
        recyclerView.setAdapter(groceryListAdapter);



        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

}
