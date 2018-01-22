package com.mitchlthompson.mealqueue.models;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.RelativeLayout;

import com.mitchlthompson.mealqueue.R;
import com.mitchlthompson.mealqueue.adapters.GroceryListAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class GroceryActivity extends AppCompatActivity {
    private Context context;
    private RecyclerView recyclerView;
    private RelativeLayout relativeLayout;
    private GroceryListAdapter groceryListAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private ArrayList<String> groceryList;
    private ArrayList<String> mealList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery);
        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        context = getApplicationContext();


        groceryList = new ArrayList<>(Arrays.asList("Tomato", "Cheese", "Green Beans",
                "Jelly", "Eggs", "Peaches", "Ice Cream"));

        relativeLayout = (RelativeLayout) findViewById(R.id.action_grocery);
        recyclerView = (RecyclerView) findViewById(R.id.grocery_recycler);
        recyclerViewLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);

        groceryListAdapter = new GroceryListAdapter(context, groceryList);
        recyclerView.setAdapter(groceryListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}