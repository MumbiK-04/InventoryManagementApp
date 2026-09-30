package com.example.inventorymanagementapp;

import com.google.firebase.firestore.Exclude;

public class Book {

    private String title;
    private String author;
    private double price;
    private double buyingPrice;
    private int quantity;
    private String ImageUrl;
    private String category;

    @Exclude
    private String docId;

    // MANDATORY: Empty Constructor for Firebase
    public Book() {
    }

    // FULL CONSTRUCTOR
    public Book(String title, String author, double price, double buyingPrice, int quantity, String ImageUrl, String category) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.buyingPrice = buyingPrice;
        this.quantity = quantity;
        this.ImageUrl = ImageUrl;
        this.category = category;
    }

    // --- HELPER LOGIC ---

    @Exclude // We don't want to save this "calculation" to Firestore
    public boolean isOutOfStock() {
        return quantity <= 0;
    }

    // --- GETTERS AND SETTERS ---

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getBuyingPrice() { return buyingPrice; }
    public void setBuyingPrice(double buyingPrice) { this.buyingPrice = buyingPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getImageUrl() { return ImageUrl; }
    public void setImageUrl(String ImageUrl) { this.ImageUrl = ImageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Exclude
    public String getDocId() { return docId; }

    @Exclude
    public void setDocId(String docId) { this.docId = docId; }
}