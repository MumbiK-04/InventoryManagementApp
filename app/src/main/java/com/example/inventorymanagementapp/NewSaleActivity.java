package com.example.inventorymanagementapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NewSaleActivity extends AppCompatActivity {

    Spinner spinnerProducts;
    TextInputEditText etQuantity;
    Button btnCompleteSale;
    FirebaseFirestore db;

    ArrayList<String> productNames = new ArrayList<>();
    ArrayList<String> productIds = new ArrayList<>();
    ArrayList<Double> productPrices = new ArrayList<>();
    ArrayList<String> collectionPaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sale);

        db = FirebaseFirestore.getInstance();
        spinnerProducts = findViewById(R.id.spinnerProducts);
        etQuantity = findViewById(R.id.etQuantity);
        btnCompleteSale = findViewById(R.id.btnCompleteSale);

        loadAllProducts();

        btnCompleteSale.setOnClickListener(v -> processSale());
    }

    private void loadAllProducts() {
        productNames.clear();
        productIds.clear();
        productPrices.clear();
        collectionPaths.clear();

        // Fetch Books
        db.collection("books").get().addOnSuccessListener(bookSnaps -> {
            for (QueryDocumentSnapshot doc : bookSnaps) {
                productNames.add(doc.getString("title") + " (Book)");
                productIds.add(doc.getId());
                productPrices.add(doc.getDouble("price"));
                collectionPaths.add("books");
            }

            // Fetch Stationery
            db.collection("stationery").get().addOnSuccessListener(statSnaps -> {
                for (QueryDocumentSnapshot doc : statSnaps) {
                    productNames.add(doc.getString("title") + " (Stationery)");
                    productIds.add(doc.getId());
                    productPrices.add(doc.getDouble("price"));
                    collectionPaths.add("stationery");
                }
                setupSpinner();
            });
        });
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, productNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProducts.setAdapter(adapter);
    }

    private void processSale() {
        if (productIds.isEmpty()) return;

        int selectedIndex = spinnerProducts.getSelectedItemPosition();
        String productId = productIds.get(selectedIndex);
        String productName = productNames.get(selectedIndex);
        double price = productPrices.get(selectedIndex);
        String collectionPath = collectionPaths.get(selectedIndex);

        String qtyStr = (etQuantity.getText() != null) ? etQuantity.getText().toString().trim() : "";
        if (qtyStr.isEmpty()) {
            etQuantity.setError("Enter quantity");
            return;
        }

        int qty = Integer.parseInt(qtyStr);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            return;
        }

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DocumentReference itemRef = db.collection(collectionPath).document(productId);

        db.runTransaction(transaction -> {
            Double currentQty = transaction.get(itemRef).getDouble("quantity");
            if (currentQty != null && currentQty >= qty) {
                transaction.update(itemRef, "quantity", currentQty - qty);

                Map<String, Object> sale = new HashMap<>();
                sale.put("item", productName);
                sale.put("total", price * qty);
                sale.put("dateString", todayDate);
                sale.put("servedBy", currentUser.getEmail());
                sale.put("timestamp", com.google.firebase.Timestamp.now());

                db.collection("orders").add(sale);
                return null;
            } else {
                throw new RuntimeException("Insufficient Stock!");
            }
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Sale successful!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
