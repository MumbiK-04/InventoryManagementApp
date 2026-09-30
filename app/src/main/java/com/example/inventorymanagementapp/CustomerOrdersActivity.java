package com.example.inventorymanagementapp;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class CustomerOrdersActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        // Start listening for the Admin's update immediately
        startAlertListener();

    }

    private void startAlertListener() {
        if (uid == null) return;



        db.collection("users").document(uid).collection("notifications")
                .whereEqualTo("isRead", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;

                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            String message = doc.getString("message");

                            // SHOW THE POP-UP ALERT
                            new AlertDialog.Builder(CustomerOrdersActivity.this)
                                    .setTitle("Delivery Update!")
                                    .setMessage(message)
                                    .setCancelable(false)
                                    .setPositiveButton("OK, I've Noted", (dialog, which) -> {
                                        // Mark as read so it doesn't pop up again
                                        doc.getReference().update("isRead", true);
                                        dialog.dismiss();
                                    })
                                    .show();
                        }
                    }
                });
    }
}
