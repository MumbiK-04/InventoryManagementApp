package com.example.inventorymanagementapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class InventoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton fabAdd;
    FirebaseFirestore db;
    BookAdapter adapter;
    ArrayList<Book> bookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // 1. INITIALIZE VIEWS
        recyclerView = findViewById(R.id.inventoryRecyclerView);
        fabAdd = findViewById(R.id.fabAdd);

        // 2. SETUP RECYCLERVIEW
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookList = new ArrayList<>();

        // Passing "admin" enables the "Edit" buttons in BookAdapter
        adapter = new BookAdapter(this, bookList, "admin");
        recyclerView.setAdapter(adapter);

        // 3. FIRESTORE
        db = FirebaseFirestore.getInstance();
        loadInventory();

        // 4. ADD BUTTON CLICK
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(InventoryActivity.this, AddBookActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data whenever you return to this screen
        loadInventory();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadInventory() {
        // Clear once at the start
        bookList.clear();
        adapter.notifyDataSetChanged();

        // PART A: Load Books
        db.collection("books").get()
                .addOnSuccessListener(snapshots -> {
                    for (QueryDocumentSnapshot doc : snapshots) {
                        try {
                            Book book = doc.toObject(Book.class);
                            book.setDocId(doc.getId());
                            bookList.add(book);
                        } catch (Exception e) {
                            // Skip items that don't match the Book.java structure
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading books", Toast.LENGTH_SHORT).show());

        // PART B: Load Stationery (Ensures Admin sees everything)
        db.collection("stationery").get()
                .addOnSuccessListener(snapshots -> {
                    for (QueryDocumentSnapshot doc : snapshots) {
                        try {
                            Book item = doc.toObject(Book.class);
                            item.setDocId(doc.getId());
                            if (item.getAuthor() == null) item.setAuthor("Stationery");
                            bookList.add(item);
                        } catch (Exception e) {
                            // Skip items with errors
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}