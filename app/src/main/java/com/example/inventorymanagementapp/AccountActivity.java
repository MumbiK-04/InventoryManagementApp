package com.example.inventorymanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountActivity extends AppCompatActivity {

    private TextView tvName, tvEmail;
    private Button btnLogout;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // 1. INITIALIZE VIEWS
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        btnLogout = findViewById(R.id.btnLogout);
        db = FirebaseFirestore.getInstance();

        // 2. SET INITIAL LOADING STATE
        // This replaces the blank space so the user knows the app is working
        tvName.setText("Connecting...");
        tvEmail.setText("Fetching details...");

        // 3. LOAD DATA FROM FIREBASE
        loadUserProfile();

        // 4. LOGOUT LOGIC
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                // Clear the backstack so user can't click 'back' to get into the account
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();

            db.collection("users").document(uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc != null && doc.exists()) {
                        String name = doc.getString("name");
                        String email = doc.getString("email");

                        // Update UI with real data
                        if (name != null && !name.isEmpty()) {
                            tvName.setText(name);
                        } else {
                            tvName.setText("Name not set in DB");
                        }

                        if (email != null) {
                            tvEmail.setText(email);
                        }
                    } else {
                        // Document ID doesn't match the Auth UID
                        tvName.setText("Profile Not Found");
                        tvEmail.setText(user.getEmail()); // Show auth email as fallback
                        Log.e("ACCOUNT_DEBUG", "No Firestore document for UID: " + uid);
                    }
                } else {
                    // Network or Permission Error
                    tvName.setText("Connection Error");
                    tvEmail.setText("Check Internet");
                    Toast.makeText(this, "Failed to reach server", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // No user logged in, redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}