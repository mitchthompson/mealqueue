package com.mitchlthompson.mealqueue.helpers;

import android.util.Log;
import android.widget.Filter;

import com.mitchlthompson.mealqueue.adapters.RecipeAdapter;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by mitch on 5/30/2018.
 */

public class RecipesFilterHelper extends Filter {

    static ArrayList<Recipe> currentList;
    static RecipeAdapter adapter;

    public static RecipesFilterHelper newInstance(ArrayList<Recipe> newCurrentList, RecipeAdapter adapter){
        RecipesFilterHelper.adapter = adapter;
        RecipesFilterHelper.currentList = newCurrentList;
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
            ArrayList<Recipe> foundFilters = new ArrayList<>();

            String recipeName;
            String recipeID;

            for(Recipe r: currentList){
                recipeName = r.getName();
                recipeID = r.getId();

                //search
                if (recipeName.toUpperCase().contains(constraint)){

                    //add to new array if found
                    foundFilters.add(new Recipe(recipeName, recipeID));
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

        if(filterResults != null && filterResults.values != null){
            adapter.setRecipeName((ArrayList<Recipe>) filterResults.values);
            adapter.notifyDataSetChanged();
        }

    }
}
