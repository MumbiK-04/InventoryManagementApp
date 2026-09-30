package com.example.inventorymanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private ListView cartListView;
    private TextView tvTotal;
    private Button btnCheckout;

    private CartAdapter adapter;
    private final ArrayList<Map<String, Object>> cartItems = new ArrayList<>();
    private FirebaseFirestore db;
    private String uid;
    private double totalPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }

        cartListView = findViewById(R.id.cartListView);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);

        adapter = new CartAdapter(this, cartItems);
        cartListView.setAdapter(adapter);

        loadCart();

        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            } else {
                // Pass the subtotal to the next activity
                Intent intent = new Intent(CartActivity.this, DeliveryDetailsActivity.class);
                intent.putExtra("ITEM_SUBTOTAL", totalPrice);
                startActivity(intent);
            }
        });
    }

    /**
     * PUBLIC method accessed by CartAdapter to refresh UI
     */
    public void loadCart() {
        if (uid == null) return;

        db.collection("users").document(uid).collection("cart").get()
                .addOnSuccessListener(snapshots -> {
                    cartItems.clear();
                    totalPrice = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Map<String, Object> data = new HashMap<>(doc.getData());
                        data.put("docId", doc.getId());

                        double price = 0.0;
                        Object pObj = data.get("price");
                        if (pObj instanceof Number) price = ((Number) pObj).doubleValue();

                        long qty = 1;
                        Object qObj = data.get("quantity");
                        if (qObj instanceof Number) qty = ((Number) qObj).longValue();

                        totalPrice += (price * qty);
                        cartItems.add(data);
                    }

                    tvTotal.setText(String.format(Locale.getDefault(), "Total: Ksh %.2f", totalPrice));
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("FIREBASE", "Error loading cart", e));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the cart whenever the user navigates back to this screen
        loadCart();
    }
}