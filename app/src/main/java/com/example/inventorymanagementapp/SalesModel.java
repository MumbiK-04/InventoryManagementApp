package com.example.inventorymanagementapp;

public class SalesModel {
    private String item;
    private double total;
    private String servedBy;
    private String dateString;

    public SalesModel() {}

    public String getItem() { return item; }
    public double getTotal() { return total; }
    public String getServedBy() { return servedBy; }
    public String getDateString() { return dateString; }
}
