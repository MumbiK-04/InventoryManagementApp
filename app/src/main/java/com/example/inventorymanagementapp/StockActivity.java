package com.example.inventorymanagementapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class StockActivity extends AppCompatActivity {

    private RecyclerView rvStockList;
    private ItemAdapter adapter;
    private List<ItemModel> itemList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        // 1. Initialize Firebase & UI
        db = FirebaseFirestore.getInstance();
        rvStockList = findViewById(R.id.rvStockList);

        progressBar = findViewById(R.id.progressBar);

        // 2. Setup Grid Layout (2 columns to match Customer Dashboard)
        rvStockList.setLayoutManager(new GridLayoutManager(this, 2));

        itemList = new ArrayList<>();
        adapter = new ItemAdapter(itemList, this);
        rvStockList.setAdapter(adapter);

        // 3. Fetch Data
        loadInventoryData();
    }

    private void loadInventoryData() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        itemList.clear();

        // 1. Fetch Books
        db.collection("books").get().addOnSuccessListener(bookSnapshots -> {
            for (QueryDocumentSnapshot doc : bookSnapshots) {
                ItemModel item = doc.toObject(ItemModel.class);
                itemList.add(item);
            }

            // 2.  Fetch Stationery (Nested call or separate method)
            db.collection("stationery").get().addOnSuccessListener(statSnapshots -> {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                for (QueryDocumentSnapshot doc : statSnapshots) {
                    ItemModel item = doc.toObject(ItemModel.class);
                    itemList.add(item);
                }

                if (itemList.isEmpty()) {
                    Toast.makeText(this, "Inventory is empty!", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged(); // Refresh the grid

            }).addOnFailureListener(e -> Toast.makeText(this, "Stationery Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        }).addOnFailureListener(e -> Toast.makeText(this, "Books Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh stock levels whenever the cashier returns to this screen
        loadInventoryData();
    }
}