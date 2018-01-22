package com.mitchlthompson.mealqueue.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mitchlthompson.mealqueue.R;

import java.util.ArrayList;

public class WeekPlanAdapter extends RecyclerView.Adapter<WeekPlanAdapter.WeekPlanViewHolder> {

    private Context context;
    private View view;
    private WeekPlanViewHolder weekPlanViewHolder;
    private LayoutInflater inflater;
    private ArrayList<String> dayList = new ArrayList();

    public WeekPlanAdapter(Context newContext, ArrayList<String> newData) {
        this.context = newContext;
        inflater = LayoutInflater.from(context);
        this.dayList = newData;
    }

    @Override
    public WeekPlanViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        view = LayoutInflater.from(context).inflate(R.layout.weekplan_items,parent,false);
        weekPlanViewHolder = new WeekPlanViewHolder(view);
        return weekPlanViewHolder;
    }

    @Override
    public void onBindViewHolder(WeekPlanViewHolder holder, int position) {
        holder.dayTextView.setText(dayList.get(position));
    }

    @Override
    public int getItemCount() {
        return dayList.size();
    }

    public static class WeekPlanViewHolder extends RecyclerView.ViewHolder {
        public TextView dayTextView;
        public WeekPlanViewHolder(View v) {
            super(v);
            dayTextView = (TextView) v.findViewById(R.id.day_text);
        }
    }
}
