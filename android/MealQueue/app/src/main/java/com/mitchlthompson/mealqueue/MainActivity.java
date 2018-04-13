package com.mitchlthompson.mealqueue;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mitchlthompson.mealqueue.adapters.WeekPlanAdapter;
import com.mitchlthompson.mealqueue.helpers.myCalendar;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Context context;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;
    private DatabaseReference mRefMealPlan;

    private String weekStart;
    private String date;

    private Button mondayBtn;
    private TextView mondayTV;
    private TextView mondayMeal1;

    private Map<String,Object> mealPlanData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        context = getApplicationContext();

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        myCalendar c = new myCalendar();
        List dates = formatDates(c.getCalendar());
        //Log.d(TAG, dates.toString());
        weekStart = dates.get(0).toString();

        mondayTV = findViewById(R.id.monday_tv);
        mondayTV.setText(dates.get(0).toString());
        mondayMeal1 = findViewById(R.id.monday_meal_1);

        mondayBtn = findViewById(R.id.monday_btn);
        date = dates.get(0).toString();
        mondayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MealPlanDayActivity.class)
                        .putExtra("WeekStart", weekStart)
                        .putExtra("Date", date));

            }
        });

        mRefMealPlan = mFirebaseDatabase.getReference("/mealplans/" + userID + "/" + weekStart + "/" + date);

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


        mRefMealPlan.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mealPlanData = (HashMap<String,Object>) dataSnapshot.getValue();
                if(mealPlanData!=null){
                    parseMealPlanData(mealPlanData);
                }
                //Log.d(TAG, "mealplandata " + mealPlanData.toString());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Defines the actions after user selection of the menu items in the toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                // User chose the "Log Out" item...
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                return true;

            // TODO Remove following action items from actionbar here and in res/main_menu.xml

            case R.id.action_login:
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                return true;

            case R.id.action_calendar:
                startActivity(new Intent(MainActivity.this, CalendarActivity.class));
                return true;

            case R.id.action_recipes:
                startActivity(new Intent(MainActivity.this, RecipesActivity.class));
                return true;

            case R.id.action_grocery:
                startActivity(new Intent(MainActivity.this, GroceryActivity.class));
                return true;

            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
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

    public List formatDates(List inputDates){
        for(int x=0; x<inputDates.size();x++){
            //Log.d(TAG, dates.get(x).toString());
            String[] splitArray = inputDates.get(x).toString().split("\\s+");
            inputDates.set(x,
                    splitArray[0] + " "
                            + splitArray[1] + " "
                            + splitArray[2]);
        }
        return inputDates;
    }

    public void parseMealPlanData(Map<String,Object> inputMap){
        String mealsText = "";
        for (String key : inputMap.keySet()){
            Log.d(TAG, key.toString());
            mealsText = mealsText + key.toString() + "\n";
            //Log.d(TAG,inputMap.get(key)+" ");
            //Log.d(TAG,inputMap.get(key)+" "+key);
        }
        mondayMeal1.setText(mealsText);
    }

}
