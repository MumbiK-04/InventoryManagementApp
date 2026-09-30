package com.example.inventorymanagementapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class CustomersActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> customerList;
    ArrayAdapter<String> adapter;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers);

        // 1. Initialize UI components
        listView = findViewById(R.id.customersListView);
        customerList = new ArrayList<>();

        // Simple list item layout for display
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, customerList);
        listView.setAdapter(adapter);

        // 2. Initialize Firestore
        db = FirebaseFirestore.getInstance();

        Toast.makeText(this, "Loading Customers...", Toast.LENGTH_SHORT).show();

        // 3. Fetch only users with 'buyer' role
        db.collection("users")
                .whereEqualTo("role", "buyer")
                .get()
                .addOnSuccessListener(snapshots -> {
                    customerList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        // Safe extraction to prevent NullPointerExceptions
                        String name = doc.getString("fullName");
                        String email = doc.getString("email");

                        if (name == null) name = "Unknown User";
                        if (email == null) email = "No Email Provided";

                        customerList.add(name + "\n" + email);
                    }
                    adapter.notifyDataSetChanged();

                    // UI Feedback based on results
                    if (customerList.isEmpty()) {
                        Toast.makeText(this, "No Customers Found yet.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Found " + customerList.size() + " Customers", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        // ^ Expression Lambda used for cleaner code
    }
}