package com.example.inventorymanagementapp;

import java.util.List;

public class CountyModel {
    private String county;
    private List<String> sub_counties;

    // 1. THE FIX: Add this constructor to accept the 2 arguments
    public CountyModel(String county, List<String> sub_counties) {
        this.county = county;
        this.sub_counties = sub_counties;
    }

    // 2. Keep an empty constructor (Best practice for JSON/Firebase)
    public CountyModel() {}

    // Getters
    public String getCounty() { return county; }
    public List<String> getSub_counties() { return sub_counties; }
}