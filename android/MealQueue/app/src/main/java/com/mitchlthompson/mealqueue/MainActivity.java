package com.mitchlthompson.mealqueue;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mitchlthompson.mealqueue.adapters.MealPlanRecipeAdapter;
import com.mitchlthompson.mealqueue.adapters.WeekPlanAdapter;
import com.mitchlthompson.mealqueue.helpers.myCalendar;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
    private DatabaseReference mRefMealPlan2;

    private RecyclerView recyclerView;
    private WeekPlanAdapter weekPlanAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private String weekStart;
    private List dates;
    private String todaysDate;
    private CalendarView calenderView;
    private myCalendar c;

    private Map<String,Object> mealPlanData;

    private TextView dateTextView;
    private TextView mealsTextView;
    private Button addPlanBtn;


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

        Calendar c = Calendar.getInstance();
        //Log.d(TAG, String.valueOf(c.get(Calendar.MONTH)) + " " + String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
        todaysDate = getDate(c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        Log.d(TAG, todaysDate);

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

        dateTextView = findViewById(R.id.date_tv);
        dateTextView.setText(todaysDate);
        mealsTextView = findViewById(R.id.meals_tv);
        addPlanBtn = findViewById(R.id.add_plan_btn);
        addPlanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, MealPlanDayActivity.class)
                        .putExtra("WeekStart", weekStart)
                        .putExtra("Date", todaysDate));
            }
        });

        getMeals(todaysDate);

        calenderView = findViewById(R.id.calendarView);
        calenderView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                todaysDate = getDate(month, dayOfMonth);
                dateTextView.setText(todaysDate);
                mealsTextView.setText("");

                mRefMealPlan2 = mFirebaseDatabase.getReference("/mealplans/" + userID + "/").child(todaysDate);

                mRefMealPlan2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mealPlanData = (HashMap<String,Object>) dataSnapshot.getValue();
                        String meals = "";
                        if(mealPlanData!=null) {
                            for (String key : mealPlanData.keySet()) {
                                Log.d(TAG, key);
                                meals = meals + key + "\n\n";
                            }
                            if (meals != "") {
                                mealsTextView.setText(meals);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

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

    public String getDate(int month, int dayOfMonth) {
        String date = "";
        switch (month) {
            case 0:
                date = "Jan";
                break;

            case 1:
                date = "Feb";
                break;

            case 2:
                date = "Mar";
                break;

            case 3:
                date = "Apr";
                break;
        }
        date = date + " " + dayOfMonth;
        return date;
    }

    public List formatDates(List inputDates){
        for(int x=0; x<inputDates.size();x++){
            //Log.d(TAG, dates.get(x).toString());
            String[] splitArray = inputDates.get(x).toString().split("\\s+");
            inputDates.set(x, splitArray[1] + " "
                            + splitArray[2]);
        }
        return inputDates;
    }

    public void getMeals(String date) {
        mRefMealPlan = mFirebaseDatabase.getReference("/mealplans/" + userID + "/").child(date);

        mRefMealPlan.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mealPlanData = (HashMap<String,Object>) dataSnapshot.getValue();
                String meals = "";
                if(mealPlanData!=null) {
                    for (String key : mealPlanData.keySet()) {
                        Log.d(TAG, key);
                        meals = meals + key + "\n\n";
                    }
                    if (meals != "") {
                        mealsTextView.setText(meals);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
