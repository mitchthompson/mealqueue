package com.mitchlthompson.mealqueue.models;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.RelativeLayout;

import com.mitchlthompson.mealqueue.R;
import com.mitchlthompson.mealqueue.adapters.WeekPlanAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

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


        daysList = new ArrayList<>(Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday"));

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
}
