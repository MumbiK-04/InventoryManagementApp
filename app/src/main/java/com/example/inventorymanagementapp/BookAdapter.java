package com.example.inventorymanagementapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final Context context;
    private ArrayList<Book> list;
    private final String userRole;

    public BookAdapter(Context context, ArrayList<Book> list, String userRole) {
        this.context = context;
        this.list = list;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = list.get(position);

        // UI Binding
        holder.tvTitle.setText(book.getTitle() != null ? book.getTitle() : "No Title");
        holder.tvAuthor.setText(book.getAuthor() != null ? book.getAuthor() : "N/A");
        holder.tvPrice.setText(String.format(Locale.getDefault(), "Ksh %.2f", book.getPrice()));

        // Image Loading
        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(book.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.imgProduct);
        }

        // --- STOCK LOGIC (Works for both Books & Stationery) ---
        int stockQuantity = book.getQuantity();
        boolean isOutOfStock = stockQuantity <= 0;

        if ("admin".equalsIgnoreCase(userRole)) {
            // ADMIN VIEW: Show stock count on button
            holder.tvOutOfStock.setVisibility(isOutOfStock ? View.VISIBLE : View.GONE);
            holder.btnAdd.setText("Edit (Stock: " + stockQuantity + ")");
            holder.btnAdd.setBackgroundColor(Color.parseColor("#BA68C8"));
            holder.btnAdd.setEnabled(true);

            holder.btnAdd.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditBookActivity.class);
                intent.putExtra("docId", book.getDocId());
                intent.putExtra("title", book.getTitle());
                intent.putExtra("author", book.getAuthor());
                intent.putExtra("price", book.getPrice());
                intent.putExtra("buyingPrice", book.getBuyingPrice());
                intent.putExtra("quantity", book.getQuantity());
                intent.putExtra("ImageUrl", book.getImageUrl());
                intent.putExtra("category", book.getCategory());
                context.startActivity(intent);
            });
        } else {
            // BUYER VIEW: Disable button if out of stock
            if (isOutOfStock) {
                holder.tvOutOfStock.setVisibility(View.VISIBLE);
                holder.btnAdd.setText("Sold Out");
                holder.btnAdd.setBackgroundColor(Color.LTGRAY);
                holder.btnAdd.setEnabled(false);
                holder.itemView.setAlpha(0.6f);
            } else {
                holder.tvOutOfStock.setVisibility(View.GONE);
                holder.btnAdd.setText("Add to Cart");
                holder.btnAdd.setBackgroundColor(Color.parseColor("#BA68C8"));
                holder.btnAdd.setEnabled(true);
                holder.itemView.setAlpha(1.0f);
                holder.btnAdd.setOnClickListener(v -> addToCart(book));
            }
        }
    }

    private void addToCart(Book book) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).collection("cart")
                .document(book.getDocId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        Long qtyInCartLong = documentSnapshot.getLong("quantity");
                        long qtyInCart = (qtyInCartLong != null) ? qtyInCartLong : 0;

                        if (qtyInCart >= book.getQuantity()) {
                            Toast.makeText(context, "No more stock available!", Toast.LENGTH_SHORT).show();
                        } else {
                            documentSnapshot.getReference().update("quantity", qtyInCart + 1);
                            Toast.makeText(context, "Cart updated!", Toast.LENGTH_SHORT).show();
                        }
                    } else {

                        db.collection("users").document(uid).collection("cart")
                                .document(book.getDocId())
                                .set(createCartMap(book));
                        Toast.makeText(context, "Added to Cart!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private Map<String, Object> createCartMap(Book book) {
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("title", book.getTitle());
        cartItem.put("price", book.getPrice());
        cartItem.put("docId", book.getDocId());
        cartItem.put("ImageUrl", book.getImageUrl());
        cartItem.put("category", book.getCategory());
        cartItem.put("buyingPrice", book.getBuyingPrice());
        cartItem.put("quantity", 1);
        return cartItem;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<Book> filteredList) {
        this.list = filteredList;
        notifyDataSetChanged();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvPrice, tvOutOfStock;
        ImageView imgProduct;
        Button btnAdd;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            tvOutOfStock = itemView.findViewById(R.id.tvOutOfStock);
        }
    }
}