package com.example.inventorymanagementapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        TextInputEditText etName = findViewById(R.id.etNewUserName);
        TextInputEditText etEmail = findViewById(R.id.etNewUserEmail);
        TextInputEditText etRole = findViewById(R.id.etNewUserRole);
        Button btnSave = findViewById(R.id.btnSaveUser);

        btnSave.setOnClickListener(v -> {
            // Safely get text by checking if the Editable is not null
            String name = (etName.getText() != null) ? etName.getText().toString().trim() : "";
            String email = (etEmail.getText() != null) ? etEmail.getText().toString().trim() : "";
            String role = (etRole.getText() != null) ? etRole.getText().toString().trim() : "Staff";

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> user = new HashMap<>();
            user.put("name", name);
            user.put("email", email);
            user.put("role", role);

            db.collection("users").add(user)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "User Added!", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to the list
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}