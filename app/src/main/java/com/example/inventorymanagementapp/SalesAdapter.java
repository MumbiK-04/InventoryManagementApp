package com.example.inventorymanagementapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.ViewHolder> {
    private final List<SalesModel> salesList;

    public SalesAdapter(List<SalesModel> salesList) {
        this.salesList = salesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Using Android's built-in two-line list item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SalesModel sale = salesList.get(position);

        // 1. Safety check for Item Name
        String itemName = (sale.getItem() != null) ? sale.getItem() : "Unknown Item";
        holder.text1.setText(itemName);

        // 2. Professional Currency Formatting (Ksh 1,900.00)
        String totalFormatted = String.format(Locale.getDefault(), "Ksh %.2f", sale.getTotal());

        // 3. Displaying the "Served By" info as requested for your reports
        String cashier = (sale.getServedBy() != null) ? sale.getServedBy() : "Unknown Cashier";
        holder.text2.setText(totalFormatted + " | Served by: " + cashier);
    }

    @Override
    public int getItemCount() {
        return salesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // These IDs belong to the android.R.layout.simple_list_item_2 layout
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
