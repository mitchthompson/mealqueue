package com.mitchlthompson.mealqueue;


import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mitchlthompson.mealqueue.helpers.GrocerySyncHelper;
import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.squareup.timessquare.CalendarPickerView.SelectionMode.RANGE;

public class SyncCalendarFragment extends Fragment {
    private static final String TAG = "SyncCalendarFragment";

    View viewer;
    private Context context;

    private Button doneBtn, cancelBtn;
    private List selectedDates;
    private ArrayList<String> formattedDates;

    public SyncCalendarFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewer = inflater.inflate(R.layout.fragment_calendar, container,
                false);
        context = getActivity();

        Date today = new Date();
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        final CalendarPickerView calendar = viewer.findViewById(R.id.grocery_sync_calendar);
        calendar.init(today, nextYear.getTime())
                .inMode(RANGE);

        calendar.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                selectedDates = calendar.getSelectedDates();
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });

        formattedDates = new ArrayList<>();
        doneBtn = viewer.findViewById(R.id.done_button);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedDates == null){
                    Toast.makeText(context, "Please select a date range to sync", Toast.LENGTH_LONG).show();
                }else{
                    for(int i=0;i<selectedDates.size();i++){
                        formattedDates.add(DateFormat.getDateInstance(DateFormat.FULL).format(selectedDates.get(i)));
                    }
                    Log.d(TAG, formattedDates.toString());

                    GrocerySyncHelper grocerySyncHelper = new GrocerySyncHelper();
                    grocerySyncHelper.getData(formattedDates);

                    GroceryFragment newFragment = new GroceryFragment();

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_frame, newFragment)
                            .commit();

                }

            }
        });

        cancelBtn = viewer.findViewById(R.id.sync_cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroceryFragment newFragment = new GroceryFragment();

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame, newFragment)
                        .commit();
            }
        });

        return viewer;

    }
}