package com.example.inventorymanagementapp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class SalesHistoryActivity extends AppCompatActivity {

    private RecyclerView rvSalesHistory;
    private SalesAdapter adapter;
    private List<SalesModel> salesList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_history);

        db = FirebaseFirestore.getInstance();
        rvSalesHistory = findViewById(R.id.rvSalesHistory);

        rvSalesHistory.setLayoutManager(new LinearLayoutManager(this));
        salesList = new ArrayList<>();
        adapter = new SalesAdapter(salesList);
        rvSalesHistory.setAdapter(adapter);

        loadSalesHistory();
    }

    private void loadSalesHistory() {
        // Fetching orders, sorted by newest first
        db.collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    salesList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        SalesModel sale = doc.toObject(SalesModel.class);
                        salesList.add(sale);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}