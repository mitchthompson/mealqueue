package com.mitchlthompson.mealqueue.helpers;

/**
 * Created by mitch on 2/15/2018.
 */

public class Recipe {
    public String name;
    public String cookingTime;
    public String directions;

    public Recipe(){

    }

    public Recipe(String name, String cookingTime, String directions){
        this.name = name;
        this.cookingTime = cookingTime;
        this.directions = directions;
    }

    public String getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(String cookingTime) {
        this.cookingTime = cookingTime;
    }

    public String getDirections() {
        return directions;
    }

    public void setDirections(String directions) {
        this.directions = directions;
    }
}
