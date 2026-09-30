package com.example.inventorymanagementapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class BuyerDashboardActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    BookAdapter adapter;
    ArrayList<Book> productList;
    FirebaseFirestore db;
    String uid;
    EditText searchBar;
    LinearLayout navHome, navCart, navChat, navProfile, navDelivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_dashboard);

        // 1. Initialize Firebase & Auth
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        // 2. Initialize Views
        searchBar = findViewById(R.id.searchBar);
        recyclerView = findViewById(R.id.booksRecyclerView);
        navHome = findViewById(R.id.navHome);
        navCart = findViewById(R.id.navCart);
        navChat = findViewById(R.id.navChat);
        navProfile = findViewById(R.id.navProfile);
        navDelivery = findViewById(R.id.navDelivery);

        // 3. Setup RecyclerView
        productList = new ArrayList<>();
        adapter = new BookAdapter(this, productList, "buyer");
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // 4. Start Listeners & Data Load
        loadData();
        setupNavigation();
        listenForAdminDispatches(); // Start listening for "Mark Delivered" updates

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
    }

    private void listenForAdminDispatches() {
        if (uid == null) return;

        db.collection("users").document(uid).collection("notifications")
                .whereEqualTo("isRead", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("NOTIF_ERROR", e.getMessage());
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            String message = doc.getString("message");

                            new AlertDialog.Builder(BuyerDashboardActivity.this)
                                    .setTitle("Order Dispatched! 🚚")
                                    .setMessage(message)
                                    .setCancelable(false)
                                    .setPositiveButton("I Have Noted", (dialog, which) -> {
                                        doc.getReference().update("isRead", true);
                                        dialog.dismiss();
                                    })
                                    .show();
                        }
                    }
                });
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> {
            searchBar.setText("");
            loadData();
        });

        navCart.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
            overridePendingTransition(0, 0);
        });

        navChat.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatActivity.class));
            overridePendingTransition(0, 0);
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, AccountActivity.class));
            overridePendingTransition(0, 0);
        });

        if (navDelivery != null) {
            navDelivery.setOnClickListener(v -> {
                startActivity(new Intent(this, DeliveryDetailsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    private void filter(String text) {
        ArrayList<Book> filteredList = new ArrayList<>();
        for (Book item : productList) {
            if (item.getTitle() != null && item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.filterList(filteredList);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadData() {
        productList.clear();
        adapter.notifyDataSetChanged();

        // Fetch Books
        db.collection("books").get().addOnSuccessListener(snapshots -> {
            for (QueryDocumentSnapshot doc : snapshots) {
                try {
                    Book item = doc.toObject(Book.class);
                    item.setDocId(doc.getId());
                    productList.add(item);
                } catch (Exception e) {
                    Log.e("FIRESTORE_ERROR", "Error mapping: " + e.getMessage());
                }
            }
            adapter.notifyDataSetChanged();
        });

        // Fetch Stationery
        db.collection("stationery").get().addOnSuccessListener(snapshots -> {
            for (QueryDocumentSnapshot doc : snapshots) {
                try {
                    Book item = doc.toObject(Book.class);
                    item.setDocId(doc.getId());
                    if (item.getAuthor() == null || item.getAuthor().isEmpty()) {
                        item.setAuthor("Stationery");
                    }
                    productList.add(item);
                } catch (Exception e) {
                    Log.e("FIRESTORE_ERROR", "Error: " + e.getMessage());
                }
            }
            adapter.notifyDataSetChanged();
        });
    }
}