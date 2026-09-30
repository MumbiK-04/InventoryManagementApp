package com.example.inventorymanagementapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditBookActivity extends AppCompatActivity {

    EditText etTitle, etAuthor, etPrice, etBuyingPrice, etQuantity;
    Button btnUpdate, btnDelete;
    FirebaseFirestore db;
    String bookId, category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        db = FirebaseFirestore.getInstance();

        // 1. Initialize Views
        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        etPrice = findViewById(R.id.etPrice);
        etBuyingPrice = findViewById(R.id.etBuyingPrice); // Added for profit tracking
        etQuantity = findViewById(R.id.etQuantity);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        // 2. Get Data from Intent
        bookId = getIntent().getStringExtra("docId"); // Changed to match BookAdapter key
        category = getIntent().getStringExtra("category");

        // Safety: If category is null, default to books, but try to detect stationery
        if (category == null) category = "books";

        etTitle.setText(getIntent().getStringExtra("title"));
        etAuthor.setText(getIntent().getStringExtra("author"));
        etPrice.setText(String.valueOf(getIntent().getDoubleExtra("price", 0)));
        etBuyingPrice.setText(String.valueOf(getIntent().getDoubleExtra("buyingPrice", 0)));
        etQuantity.setText(String.valueOf(getIntent().getIntExtra("quantity", 0)));

        // 3. Update Logic
        btnUpdate.setOnClickListener(v -> {
            try {
                Map<String, Object> map = new HashMap<>();
                map.put("title", etTitle.getText().toString().trim());
                map.put("author", etAuthor.getText().toString().trim());
                map.put("price", Double.parseDouble(etPrice.getText().toString().trim()));
                map.put("buyingPrice", Double.parseDouble(etBuyingPrice.getText().toString().trim()));
                map.put("quantity", Integer.parseInt(etQuantity.getText().toString().trim()));

                // Updates the specific document in either 'books' or 'stationery'
                db.collection(category).document(bookId)
                        .update(map)
                        .addOnSuccessListener(a -> {
                            Toast.makeText(this, "Update Successful!", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Delete Logic
        btnDelete.setOnClickListener(v -> {
            db.collection(category).document(bookId)
                    .delete()
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "Item Deleted!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Delete Failed", Toast.LENGTH_SHORT).show());
        });
    }
}