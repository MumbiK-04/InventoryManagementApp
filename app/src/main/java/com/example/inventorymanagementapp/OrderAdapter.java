package com.example.inventorymanagementapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    Context context;
    ArrayList<Order> orderList;
    FirebaseFirestore db;

    public OrderAdapter(Context context, ArrayList<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvCustomerName.setText(order.getCustomerName());
        holder.tvMpesaNumber.setText("Phone: " + order.getCustomer());
        holder.tvItemsSummary.setText("Items: " + order.getItemsSummary());
        holder.tvTotalAmount.setText(String.format(Locale.getDefault(), "Ksh %.2f", order.getTotalAmount()));
        holder.tvDeliveryWay.setText("Via: " + (order.getDeliveryWay() != null ? order.getDeliveryWay() : "Pickup"));

        if (order.getDate() != null) {
            holder.tvOrderDate.setText(new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(order.getDate()));
        }

        // Status Styling
        boolean isPaid = "PAID".equalsIgnoreCase(order.getStatus());
        holder.tvStatus.setText(order.getStatus());
        holder.tvStatus.setTextColor(isPaid ? Color.GREEN : Color.BLUE);
        holder.btnUpdateStatus.setVisibility(isPaid ? View.VISIBLE : View.GONE);

        holder.btnUpdateStatus.setOnClickListener(v -> {
            db.collection("orders").document(order.getDocId()).update("status", "DELIVERED");

            // Send Alert Notification to User
            Map<String, Object> notif = new HashMap<>();
            notif.put("message", "Your order is dispatched via " + order.getDeliveryWay() + ". They will call you soon.");
            notif.put("isRead", false);
            db.collection("users").document(order.getCustomerUid()).collection("notifications").add(notif);

            Toast.makeText(context, "User Notified!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() { return orderList.size(); }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvMpesaNumber, tvItemsSummary, tvTotalAmount, tvStatus, tvOrderDate, tvDeliveryWay;
        Button btnUpdateStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvMpesaNumber = itemView.findViewById(R.id.tvMpesaNumber);
            tvItemsSummary = itemView.findViewById(R.id.tvItemsSummary);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvDeliveryWay = itemView.findViewById(R.id.tvDeliveryWay);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }
    }
}
