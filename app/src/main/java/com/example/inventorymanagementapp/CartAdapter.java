package com.example.inventorymanagementapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Map;

public class CartAdapter extends BaseAdapter {
    private final CartActivity context;
    private final ArrayList<Map<String, Object>> cartList;
    private final FirebaseFirestore db;
    private final String uid;

    public CartAdapter(CartActivity context, ArrayList<Map<String, Object>> cartList) {
        this.context = context;
        this.cartList = cartList;
        this.db = FirebaseFirestore.getInstance();
        this.uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
    }

    @Override public int getCount() { return cartList.size(); }
    @Override public Object getItem(int pos) { return cartList.get(pos); }
    @Override public long getItemId(int pos) { return pos; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.cart_item_row, parent, false);
        }

        Map<String, Object> item = cartList.get(position);
        TextView title = convertView.findViewById(R.id.tvRowTitle);
        TextView qtyTxt = convertView.findViewById(R.id.tvRowQty);
        Button plus = convertView.findViewById(R.id.btnPlus);
        Button minus = convertView.findViewById(R.id.btnMinus);
        ImageButton delete = convertView.findViewById(R.id.btnDeleteRow);

        title.setText((String) item.get("title"));
        qtyTxt.setText(String.valueOf(item.get("quantity")));
        String docId = (String) item.get("docId");

        plus.setOnClickListener(v -> {
            if (docId != null) {
                db.collection("users").document(uid).collection("cart").document(docId)
                        .update("quantity", FieldValue.increment(1))
                        .addOnSuccessListener(a -> context.loadCart());
            }
        });

        minus.setOnClickListener(v -> {
            long q = 1;
            if (item.get("quantity") instanceof Number) q = ((Number) item.get("quantity")).longValue();
            if (q > 1 && docId != null) {
                db.collection("users").document(uid).collection("cart").document(docId)
                        .update("quantity", FieldValue.increment(-1))
                        .addOnSuccessListener(a -> context.loadCart());
            }
        });

        delete.setOnClickListener(v -> {
            if (docId != null) {
                db.collection("users").document(uid).collection("cart").document(docId)
                        .delete()
                        .addOnSuccessListener(a -> context.loadCart());
            }
        });

        return convertView;
    }
}
