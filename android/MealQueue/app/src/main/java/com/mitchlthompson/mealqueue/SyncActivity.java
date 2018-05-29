package com.mitchlthompson.mealqueue;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.mitchlthompson.mealqueue.helpers.GrocerySync;

import java.util.Calendar;

public class SyncActivity extends AppCompatActivity {
    private static final String TAG = "SyncActivity";
    private Context context;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;

    private DatePickerDialog.OnDateSetListener onStartDateSetListener, onEndDateSetListener;
    private EditText syncStartDateEditText, syncEndDateEditText;
    private Button syncHelperBtn;
    private String startDateInput, endDateInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
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
                    //Toast.makeText(context,"Successfully signing in with: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                }else{
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    //Toast.makeText(context,"Successfully signed out", Toast.LENGTH_SHORT).show();
                }
            }
        };

        syncStartDateEditText = findViewById(R.id.sync_start_date);

        // Add calendar settings for date picker
        syncStartDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            /**
             * Creates calendar settings for date picker and sets background
             * @param View
             */
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog
                        (SyncActivity.this,
                                android.R.style.Theme_Holo_Light_Dialog,
                                onStartDateSetListener,
                                year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });

        onStartDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            /**
             * Sets date text for display in UI
             * @param DatePicker view, year, month, day (int type)
             */
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month +1;
                String dateDisplay = month + "-" + day + "-" + year;
                syncStartDateEditText.setText(dateDisplay);

            }
        };

        syncEndDateEditText = findViewById(R.id.sync_end_date);

        // Add calendar settings for date picker
        syncEndDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            /**
             * Creates calendar settings for date picker and sets background
             * @param View
             */
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog
                        (SyncActivity.this,
                                android.R.style.Theme_Holo_Light_Dialog,
                                onEndDateSetListener,
                                year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });

        onEndDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            /**
             * Sets date text for display in UI
             * @param DatePicker view, year, month, day (int type)
             */
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month +1;
                String dateDisplay = month + "-" + day + "-" + year;
                syncEndDateEditText.setText(dateDisplay);

            }
        };

        syncHelperBtn = findViewById(R.id.sync_helper_btn);
        syncHelperBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDateInput = syncStartDateEditText.getText().toString().trim();
                endDateInput = syncEndDateEditText.getText().toString().trim();
                if(TextUtils.isEmpty(startDateInput) || TextUtils.isEmpty(endDateInput)){
                    Toast.makeText(context, "Please select a sync start and end date", Toast.LENGTH_SHORT).show();
                } else {
                    GrocerySync gSync = new GrocerySync();
                    //gSync.getData(startDateInput, endDateInput);

                }
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
