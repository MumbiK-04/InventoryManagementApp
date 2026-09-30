package com.example.inventorymanagementapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddBookActivity extends AppCompatActivity {

    // Added etBuyingPrice to the declaration
    TextInputEditText etISBN, etTitle, etAuthor, etPrice, etBuyingPrice, etQuantity;
    Button btnSave;
    RadioGroup rgCategory;
    RadioButton rbStationery;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        db = FirebaseFirestore.getInstance();
        etISBN = findViewById(R.id.etISBN);
        etTitle = findViewById(R.id.etBookTitle);
        etAuthor = findViewById(R.id.etAuthor);
        etPrice = findViewById(R.id.etPrice);           // What the customer pays
        etBuyingPrice = findViewById(R.id.etBuyingPrice); // NEW: What the Admin paid
        etQuantity = findViewById(R.id.etQuantity);

        btnSave = findViewById(R.id.btnSaveBook);
        rgCategory = findViewById(R.id.rgCategory);
        rbStationery = findViewById(R.id.rbStationery);

        btnSave.setOnClickListener(v -> {
            // SAFE EXTRACTION: Added null checks to prevent "Method invocation toString may produce NullPointerException"
            String isbn = (etISBN.getText() != null) ? etISBN.getText().toString().trim() : "";
            String title = (etTitle.getText() != null) ? etTitle.getText().toString().trim() : "";
            String author = (etAuthor.getText() != null) ? etAuthor.getText().toString().trim() : "";
            String priceStr = (etPrice.getText() != null) ? etPrice.getText().toString().trim() : "";
            String buyingPriceStr = (etBuyingPrice.getText() != null) ? etBuyingPrice.getText().toString().trim() : "";
            String qtyStr = (etQuantity.getText() != null) ? etQuantity.getText().toString().trim() : "";

            // Updated validation to include the new field
            if (isbn.isEmpty() || title.isEmpty() || priceStr.isEmpty() || buyingPriceStr.isEmpty() || qtyStr.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String collection = rbStationery.isChecked() ? "stationery" : "books";
                        // Create the Data Map for Books/Stationery
            Map<String, Object> item = new HashMap<>();
            item.put("category", collection);
            item.put("isbn", isbn);
            item.put("title", title);
            item.put("author", author);
            item.put("price", Double.parseDouble(priceStr));             // Selling Price
            item.put("buyingPrice", Double.parseDouble(buyingPriceStr)); // NEW: Buying Price for Profit
            item.put("quantity", Integer.parseInt(qtyStr));

            // Optimized Firebase call using Modern Java 8 Lambdas
            db.collection(collection)
                    .add(item)
                    .addOnSuccessListener(a -> {
                        // Keeping Statement Lambda here because we have 2 actions:
                        Toast.makeText(this, "Added to " + collection, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error adding item", Toast.LENGTH_SHORT).show());
            // ^ Expression Lambda: Used here because it is a single action
        });
    }
}