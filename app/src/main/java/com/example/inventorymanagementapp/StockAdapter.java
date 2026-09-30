package com.example.inventorymanagementapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Book> itemList;
    private final OnItemClickListener editListener;
    private final OnItemClickListener deleteListener;
    private final String userRole;

    public interface OnItemClickListener {
        void onItemClick(Book item);
    }


    @SuppressWarnings("unused")
    public StockAdapter(Context context, ArrayList<Book> itemList, String userRole,
                        OnItemClickListener editListener, OnItemClickListener deleteListener) {
        this.context = context;
        this.itemList = itemList;
        this.userRole = userRole;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_stock_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book item = itemList.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvPrice.setText(String.format("Ksh %s", item.getPrice()));
        holder.tvCategory.setText(item.getCategory());

        if ("cashier".equals(this.userRole)) {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);

            holder.btnEdit.setOnClickListener(v -> editListener.onItemClick(item));
            holder.btnDelete.setOnClickListener(v -> deleteListener.onItemClick(item));
        }
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvCategory;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvStockTitle);
            tvPrice = itemView.findViewById(R.id.tvStockPrice);
            tvCategory = itemView.findViewById(R.id.tvStockCategory);
            btnEdit = itemView.findViewById(R.id.btnEditStock);
            btnDelete = itemView.findViewById(R.id.btnDeleteStock);
        }
    }
}