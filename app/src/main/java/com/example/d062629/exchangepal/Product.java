package com.example.d062629.exchangepal;

import android.graphics.drawable.Drawable;

import java.util.TreeMap;

/**
 * Created by Clemens on 07.07.2017.
 */

public class Product {
    private String name;
    private TreeMap<String, Double> prices;
    private Drawable image;

    public Product(String name, TreeMap<String, Double> prices, Drawable image){
        this.name = name;
        this.prices = prices;
        this.image = image;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeMap<String, Double> getPrices() {
        return prices;
    }

    public Double getPrice(String currency){
        Double price = null;

        try {
            price = prices.get(currency);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

        return price;
    }

    public void setPrices(TreeMap<String, Double> prices) {
        this.prices = prices;
    }
}
