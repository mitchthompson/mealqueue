package com.mitchlthompson.mealqueue.helpers;

import android.text.format.DateFormat;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by mitch on 4/5/2018.
 */

public class myCalendar {
    private Calendar c;
    private List<String> output;

    public myCalendar()
    {
        c = Calendar.getInstance();

        output =  new ArrayList<String>();
    }

//    public List<String> getCalendar()
//    {
        //Get current Day of week and Apply suitable offset to bring the new calendar
        //back to the appropriate this week's monday
        //Log.d("Calender: ", String.valueOf(c.get(Calendar.DAY_OF_WEEK)));
//        switch (c.get(Calendar.DAY_OF_WEEK))
//        {
//            case Calendar.SUNDAY:
//                c.add(Calendar.DATE, -6);
//                break;
//
//            case Calendar.MONDAY:
//                //Don't need to do anything on a Monday
//                //included only for completeness
//                break;
//
//            case Calendar.TUESDAY:
//                c.add(Calendar.DATE,-1);
//                break;
//
//            case Calendar.WEDNESDAY:
//                c.add(Calendar.DATE, -2);
//                break;
//
//            case Calendar.THURSDAY:
//                c.add(Calendar.DATE,-3);
//                break;
//
//            case Calendar.FRIDAY:
//                c.add(Calendar.DATE,-4);
//                break;
//
//            case Calendar.SATURDAY:
//                c.add(Calendar.DATE,-5);
//                break;
//        }
//
//        //Add the Monday to the output
//        output.add(c.getTime().toString());
//        for (int x = 1; x <7; x++)
//        {
//            //Add the remaining days to the output
//            c.add(Calendar.DATE,1);
//            output.add(c.getTime().toString());
//        }
//        return this.output;
//    }

    public String getTodaysDate(){
        String date = c.getTime().toString();
        Log.d("Calendar: ", date);
        String[] splitArray = date.split("\\s+");
        date = splitArray[1] + " "
                        + splitArray[2];
        return date;
    }

    public String getDate(int month, int dayOfMonth){
        String date = "";
        switch (month){
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
}
