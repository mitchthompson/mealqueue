package com.mitchlthompson.mealqueue.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mitchlthompson.mealqueue.MealPlanDayActivity;
import com.mitchlthompson.mealqueue.R;

import java.util.List;
import java.util.Map;

public class WeekPlanAdapter extends RecyclerView.Adapter<WeekPlanAdapter.WeekPlanViewHolder> {

    private static final String TAG = "WeekPlanAdapter";
    private Context context;

    private View view;
    private WeekPlanViewHolder weekPlanViewHolder;
    private LayoutInflater inflater;
    private List<String> dayList;
    private String day;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;
    private DatabaseReference mRefMealPlan;

    private Map<String,Object> mealPlanData;
    private String mealsText;


    public WeekPlanAdapter(Context newContext, List<String> newDayList) {
        this.context = newContext;
        inflater = LayoutInflater.from(context);
        this.dayList = newDayList;
    }

    @Override
    public WeekPlanViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        view = LayoutInflater.from(context).inflate(R.layout.weekplan_items,parent,false);
        weekPlanViewHolder = new WeekPlanViewHolder(view);
        return weekPlanViewHolder;
    }

    @Override
    public void onBindViewHolder(WeekPlanViewHolder holder, final int position) {
        holder.dayTextView.setText(dayList.get(position));
        //holder.mealTextView.setText(getMealData(dayList.get(position)));
        holder.dayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, MealPlanDayActivity.class)
                        .putExtra("Date", dayList.get(position)));

            }
        });

    }

    @Override
    public int getItemCount() {
        return dayList.size();
    }

    public static class WeekPlanViewHolder extends RecyclerView.ViewHolder {
        public TextView dayTextView;
        public TextView mealTextView;
        public Button dayBtn;
        public WeekPlanViewHolder(View v) {
            super(v);
            dayTextView = v.findViewById(R.id.date_day_tv);
            mealTextView = v.findViewById(R.id.meals_tv);
            dayBtn = v.findViewById(R.id.day_btn);
        }
    }

//    public String getMealData(String thisDate){
//        mealsText = "";
//
//        mAuth = FirebaseAuth.getInstance();
//        mFirebaseDatabase = FirebaseDatabase.getInstance();
//        FirebaseUser user = mAuth.getCurrentUser();
//        userID = user.getUid();
//        //mRefMealPlan = mFirebaseDatabase.getReference("/mealplans/" + userID + "/" + weekStart + "/" + thisDate);
//        mRefMealPlan = mFirebaseDatabase.getReference("/mealplans/" + userID + "/").child(weekStart).child(thisDate);
//
//        Log.d("Adapter URL: ", "/mealplans/" + userID + "/" + weekStart + "/" + thisDate);
//
//        mRefMealPlan.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                mealPlanData = (HashMap<String,Object>) dataSnapshot.getValue();
//                if(mealPlanData!=null){
//                    Log.d(TAG,"mealplan null? no!");
//                    for (String key : mealPlanData.keySet()){
//                        Log.d(TAG, "Key: " + key);
//                        mealsText = mealsText + key + "\n";
//                        Log.d(TAG, "Meal text: " + mealsText);
//                    }
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//        return mealsText;
//    }

}
