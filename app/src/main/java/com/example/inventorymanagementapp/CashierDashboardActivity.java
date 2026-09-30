package com.example.inventorymanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CashierDashboardActivity extends AppCompatActivity {

    private TextView tvDailyTotal, tvLowStockAlert, tvCashierEmail;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashier_dashboard);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI Elements
        tvDailyTotal = findViewById(R.id.tvDailyTotal);
        tvLowStockAlert = findViewById(R.id.tvLowStockAlert);
        tvCashierEmail = findViewById(R.id.tvCashierEmail);

        Button btnQuickSale = findViewById(R.id.btnQuickSale);
        Button btnViewStock = findViewById(R.id.btnViewStock);
        Button btnSalesHistory = findViewById(R.id.btnSalesHistory);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Set Cashier Identity
        if (mAuth.getCurrentUser() != null) {
            tvCashierEmail.setText(mAuth.getCurrentUser().getEmail());
        }

        // --- Navigation Logic ---

        // 1. Quick Sale Page
        btnQuickSale.setOnClickListener(v ->
                startActivity(new Intent(this, NewSaleActivity.class)));

        // 2. Search Products (Stock Page - Read Only)
        btnViewStock.setOnClickListener(v ->
                startActivity(new Intent(this, StockActivity.class)));

        // 3. Sales History (Read-only + Download Receipts)
        btnSalesHistory.setOnClickListener(v ->
                startActivity(new Intent(this, SalesHistoryActivity.class)));

        // 4. Secure Logout
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        fetchDashboardData();
    }

    private void fetchDashboardData() {
        // Get Today's Date in String format for the query
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // 1. Calculate Total Amount Sold Today
        db.collection("orders")
                .whereEqualTo("dateString", todayDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double total = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Double amount = doc.getDouble("total");
                        if (amount != null) total += amount;
                    }
                    tvDailyTotal.setText(String.format("Today's Total: Ksh %.2f", total));
                });

        // 2. Automatic Low Stock Detection
        db.collection("books")
                .whereLessThan("quantity", 5) // Alert if stock is below 5
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null && !value.isEmpty()) {
                        tvLowStockAlert.setVisibility(View.VISIBLE);
                        tvLowStockAlert.setText(String.format("Low Stock Alert: %d items need restocking!", value.size()));
                    } else {
                        tvLowStockAlert.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data whenever cashier returns to dashboard
        fetchDashboardData();
    }
}
