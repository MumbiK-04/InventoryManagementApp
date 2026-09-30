package com.example.inventorymanagementapp;

public class ItemModel {

    private String title;
    private String category;
    private double price;
    private double quantity;
    private String ImageUrl;
    private String author;
    private String isbn;

    public ItemModel() {}

    // Getters
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public double getQuantity() { return quantity; }
    public String getImageUrl() { return ImageUrl; }
    public String getAuthor() { return author; }
}
