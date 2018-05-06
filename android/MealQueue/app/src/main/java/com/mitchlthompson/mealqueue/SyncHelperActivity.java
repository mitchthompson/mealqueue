package com.mitchlthompson.mealqueue;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mitchlthompson.mealqueue.helpers.Recipe;

import java.util.HashMap;
import java.util.Map;

public class SyncHelperActivity extends AppCompatActivity {
    private static final String TAG = "SyncHelperActivity";
    private Context context;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;

    private String syncStartDate, syncEndDate;
    private DatabaseReference mRefMealPlan;
    private Map<String,Object> mealPlanData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_helper);

        context = getApplicationContext();

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged:signed_in: " + user.getUid());
                    Toast.makeText(context,"Successfully signing in with: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                }else{
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Toast.makeText(context,"Successfully signed out", Toast.LENGTH_SHORT).show();
                }
            }
        };

        if(getIntent().hasExtra("Start") && getIntent().hasExtra("End")) {
            Bundle bundle = getIntent().getExtras();
            syncStartDate = bundle.getString("Start");
            syncEndDate = bundle.getString("End");
            Log.d(TAG, "Start: " + syncStartDate + " End: " + syncEndDate);
        }
        else {
            Log.d(TAG, "Nothing in intent bundle");
            startActivity(new Intent(SyncHelperActivity.this, SyncActivity.class));
        }

        mRefMealPlan = mFirebaseDatabase.getReference("/mealplans/" + userID);

        mRefMealPlan.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mealPlanData = (HashMap<String,Object>) dataSnapshot.getValue();

                if(mealPlanData!=null) {
                    for (Object value : mealPlanData.values()) {
                        Log.d(TAG, "Whole value: " + value.toString());

                    }


//                    for (Map.Entry<String, Object> entry : mealPlanData.entrySet()) {
//                        String key = entry.getKey();
//                        Object value = entry.getValue();
//                        //Log.d(TAG, "Sync Start: " + syncStartDate + " key: " + key);
//                        if (key.equals(syncStartDate) || key.equals(syncEndDate)) {
//                            Log.d(TAG, "Whole value: " + value.toString());
//
//                        } else {
//                            Log.d(TAG, "Datasnapshot key did not match");
//                        }
//                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
