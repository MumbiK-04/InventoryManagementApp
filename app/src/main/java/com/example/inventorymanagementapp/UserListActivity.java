package com.example.inventorymanagementapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<String[]> userList;
    private UserAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_list);

        db = FirebaseFirestore.getInstance();

        // 1. Setup RecyclerView
        recyclerView = findViewById(R.id.userRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();

        // 2. Setup Adapter with Delete Listener
        adapter = new UserAdapter(userList, (position, userId) -> {
            deleteUser(position, userId);
        });

        recyclerView.setAdapter(adapter);

        // 3. Setup Floating Action Button (Plus Icon)
        FloatingActionButton fabAddUser = findViewById(R.id.fabAddUser);
        fabAddUser.setOnClickListener(v -> {
            startActivity(new Intent(UserListActivity.this, AddUserActivity.class));
        });

        fetchUsers();
    }

    private void fetchUsers() {
        db.collection("users").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Toast.makeText(this, "Error loading users", Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshots != null) {
                userList.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    String name = doc.getString("name");
                    String email = doc.getString("email");
                    String role = doc.getString("role");
                    String id = doc.getId();

                    userList.add(new String[]{
                            name != null ? name : "Unknown",
                            email != null ? email : "No Email",
                            role != null ? role : "Staff",
                            id
                    });
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void deleteUser(int position, String userId) {
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                    // Note: addSnapshotListener will automatically refresh the list,
                    // but manual removal is faster for UI responsiveness.
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                });
    }

    // --- ADAPTER CLASS ---
    static class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

        private final ArrayList<String[]> list;
        private final OnDeleteClickListener deleteListener;

        public interface OnDeleteClickListener {
            void onDeleteClick(int position, String userId);
        }

        public UserAdapter(ArrayList<String[]> list, OnDeleteClickListener deleteListener) {
            this.list = list;
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new UserHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull UserHolder holder, int position) {
            String[] user = list.get(position);

            holder.name.setText(user[0]);
            holder.email.setText(user[1]);
            holder.role.setText(user[2]);

            // Delete Click Action
            holder.btnDelete.setOnClickListener(v -> {
                deleteListener.onDeleteClick(position, user[3]);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class UserHolder extends RecyclerView.ViewHolder {
            TextView name, email, role;
            ImageView btnDelete;

            public UserHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tvUserName);
                email = itemView.findViewById(R.id.tvUserEmail);
                role = itemView.findViewById(R.id.tvUserRole);
                btnDelete = itemView.findViewById(R.id.btnDeleteUser);
            }
        }
    }
}