package com.mitchlthompson.mealqueue;

import android.content.Context;
import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mitchlthompson.mealqueue.adapters.WeekPlanAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Context context;
    private RecyclerView recyclerView;
    private RelativeLayout relativeLayout;
    private WeekPlanAdapter weekPlanAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private ArrayList<String> daysList;
    private ArrayList<String> mealList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        context = getApplicationContext();

        mAuth = FirebaseAuth.getInstance();

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

        daysList = new ArrayList<>(Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"));

        relativeLayout = (RelativeLayout) findViewById(R.id.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.weekplan_recycler);
        recyclerViewLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);

        weekPlanAdapter = new WeekPlanAdapter(context, daysList);
        recyclerView.setAdapter(weekPlanAdapter);
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

}
