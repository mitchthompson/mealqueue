package com.mitchlthompson.mealqueue;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import java.text.DateFormat;
import java.util.Date;

/**
 * This is the activity that launches after the user login is successful or the user returns to app
 * while still logged-in. It handles the logic for the BottomNavigation and associated fragments.
 *
 * @author Mitchell Thompson
 * @version 1.0
 * @see HomeFragment
 * @see RecipesFragment
 * @see GroceryFragment
 */

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Context context;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;

    private BottomNavigationView mMainNav;
    private FrameLayout mMainFrame;
    private HomeFragment homeFragment;
    private RecipesFragment recipesFragment;
    private GroceryFragment groceryFragment;

    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();


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

        mMainFrame = findViewById(R.id.main_frame);
        mMainNav = findViewById(R.id.main_nav);

        homeFragment = new HomeFragment();
        if(getIntent().hasExtra("Date")) {
            Bundle bundle = getIntent().getExtras();
            date = bundle.getString("Date");
        }else{
            Date today = new Date();
            date = DateFormat.getDateInstance(DateFormat.FULL).format(today);
        }
        Bundle fragmentBundle = new Bundle();
        fragmentBundle.putString("Date", date);
        homeFragment.setArguments(fragmentBundle);

        recipesFragment = new RecipesFragment();
        groceryFragment = new GroceryFragment();

        //Checks for bundle extras that would only exist if user created new recipe
        if(getIntent().hasExtra("New Recipe")) {
            Bundle bundle = getIntent().getExtras();
            String recipeName = bundle.getString("New Recipe");
            Log.d(TAG, "New recipe: " + recipeName);
            setFragment(recipesFragment);
            mMainNav.setSelectedItemId(R.id.nav_recipes);
        } else {
            setFragment(homeFragment);
            mMainNav.setSelectedItemId(R.id.nav_home);
        }

        //Listener for handling reselection events on bottom navigation items.
        mMainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                clearBackStack();
                switch(item.getItemId()){
                    case R.id.nav_home:
                        setFragment(homeFragment);
                        return true;

                    case R.id.nav_recipes:
                        setFragment(recipesFragment);
                        return true;

                    case R.id.nav_grocery:
                        setFragment(groceryFragment);
                        return true;

                    default:
                        return false;
                }
            }
        });

    }

    /**
     * Initialize the contents of the Activity's options menu.
     * @param menu The options menu in which you place your items.
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        Drawable yourdrawable = menu.getItem(0).getIcon();
        yourdrawable.mutate();
        yourdrawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * This is called whenever an item in the options menu is selected.
     * @param item 	MenuItem: The menu item that was selected.
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
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

    /**
     * This is called whenever an option is selected in the BottomNavigation. It replaces the current
     * fragment with the fragment that was passed in as a param. This is called from the
     * mMainNav.setOnNavigationItemSelectedListener in the OnCreate method.
     * @param fragment The fragment to be set
     * @return boolean
     */
    private void setFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.slide_up,
                R.animator.slide_down);
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onPause(){
        super.onPause();

    }

    @Override
    public void onResume(){
        super.onResume();
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

    /**
     * This method clears the fragment backstack if it isn't already empty
     */
    private void clearBackStack() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

}
