package com.mitchlthompson.mealqueue.helpers;

public class Recipe {
    private String id;
    private String name;

    public Recipe(String name, String id) {
        this.id = id;
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public String getId(){
        return id;
    }


    public String toString() {
        return "value = " + this.id + ", name = " + this.name;
    }
}
