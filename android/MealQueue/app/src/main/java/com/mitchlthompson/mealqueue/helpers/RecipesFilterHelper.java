package com.mitchlthompson.mealqueue.helpers;

import android.widget.Filter;

import com.mitchlthompson.mealqueue.adapters.RecipeAdapter;

import java.util.ArrayList;

/**
 * Created by mitch on 5/30/2018.
 */

public class RecipesFilterHelper extends Filter {

    static ArrayList<String> currentList;
    static RecipeAdapter adapter;

    public static RecipesFilterHelper newInstance(ArrayList<String> currentList, RecipeAdapter adapter){
        RecipesFilterHelper.adapter = adapter;
        RecipesFilterHelper.currentList = currentList;
        return new RecipesFilterHelper();
    }

    /**
     * Checks for text entry in the search field and filters it
     * @param constraint
     * @return filterResults
     */
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults();

        if(constraint != null && constraint.length() > 0){

            //change to upper case
            constraint = constraint.toString().toUpperCase();

            //hold filters we find
            ArrayList<String> foundFilters = new ArrayList<>();

            String recipeName;

            //iterate through current list
            for (int i = 0; i < currentList.size(); i++){

                recipeName = currentList.get(i);

                //search
                if (recipeName.toUpperCase().contains(constraint)){

                    //add to new array if found
                    foundFilters.add(recipeName);
                }
            }

            //set results to filter list
            filterResults.count = foundFilters.size();
            filterResults.values = foundFilters;

        } else {

            //no item found, list remains intact
            filterResults.count = currentList.size();
            filterResults.values = currentList;
        }

        return filterResults;
    }

    //updates the recycler view to show the new filtered list
    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

        adapter.setRecipeName((ArrayList<String>) filterResults.values);
        adapter.notifyDataSetChanged();

    }
}
