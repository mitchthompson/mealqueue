package com.mitchlthompson.mealqueue.helpers;


public class Recipe {
    public String name;
    public String ID;

    public Recipe(){

    }

    public Recipe(String name, String ID){
        this.name = name;
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
