package com.example.inventorymanagementapp;

import com.google.firebase.firestore.Exclude;
import java.util.Date;

public class Order {
    private String customer;
    private String customerName;
    private String customerUid;
    private String item;
    private String orderId;
    private String status;
    private String deliveryWay;
    private String fullAddress;
    private double price;
    private long quantity;
    private Date date;

    @Exclude
    private String docId;

    public Order() {}

    // --- GETTERS AND SETTERS ---

    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }

    public String getCustomerName() {
        return (customerName != null && !customerName.isEmpty()) ? customerName : "Guest Customer";
    }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerUid() { return customerUid; }
    public void setCustomerUid(String customerUid) { this.customerUid = customerUid; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDeliveryWay() { return deliveryWay; }
    public void setDeliveryWay(String deliveryWay) { this.deliveryWay = deliveryWay; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    @Exclude
    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }

    // --- HELPER METHODS FOR ADAPTER ---

    public double getTotalAmount() { return price; }

    public String getItemsSummary() {
        return (quantity > 1) ? item + " (x" + quantity + ")" : item;
    }

    public String getMpesaNumber() {
        // This extracts the phone number from the "Name (07...) string if needed
        if (customer != null && customer.contains("(") && customer.contains(")")) {
            return customer.substring(customer.indexOf("(") + 1, customer.indexOf(")"));
        }
        return "N/A";
    }
}