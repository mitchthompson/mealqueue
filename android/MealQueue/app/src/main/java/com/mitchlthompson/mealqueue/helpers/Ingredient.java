package com.mitchlthompson.mealqueue.helpers;

public class Ingredient {
    private String id;
    private String name;
    private String amount;

    public Ingredient(String name, String amount, String id) {
        this.id = id;
        this.name = name;
        this.amount = amount;
    }

    public String getName(){
        return name;
    }

    public String getId(){
        return id;
    }

    public String getAmount(){ return amount;}


    public String toString() {
        return "value = " + this.id + ", name = " + this.name;
    }
}
