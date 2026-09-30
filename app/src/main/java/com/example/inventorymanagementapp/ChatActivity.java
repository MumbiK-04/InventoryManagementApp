package com.example.inventorymanagementapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    ListView chatList;
    EditText inputMsg;
    Button btnSend;
    ArrayList<String> messages;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatList = findViewById(R.id.chatList);
        inputMsg = findViewById(R.id.inputMsg);
        btnSend = findViewById(R.id.btnSend);

        // Safety Check
        if (chatList == null || inputMsg == null || btnSend == null) {
            Toast.makeText(this, "Error: Layout IDs missing!", Toast.LENGTH_LONG).show();
            return;
        }

        messages = new ArrayList<>();
        messages.add("Bot: Hello! Ask me about Price, Delivery, Location, or Payment.");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        chatList.setAdapter(adapter);

        btnSend.setOnClickListener(v -> {
            String msg = inputMsg.getText().toString().trim(); // .trim() removes extra spaces
            if (!msg.isEmpty()) {

                // 1. Show User Message
                messages.add("You: " + msg);

                // 2. Generate Bot Response
                String response = getBotResponse(msg.toLowerCase());
                messages.add("Bot: " + response);

                // 3. Update List & Clear Input
                adapter.notifyDataSetChanged();
                inputMsg.setText("");

                // Scroll to bottom
                chatList.smoothScrollToPosition(messages.size() - 1);
            }
        });
    }

    // --- SMART LOGIC HERE ---
    private String getBotResponse(String question) {

        if (question.contains("hi") || question.contains("hello")) {
            return "Welcome to Tausi! How can I help you today?";
        }

        else if (question.contains("price") || question.contains("cost") || question.contains("much")) {
            return "Prices are listed on the Home Dashboard. Click on any item to see details.";
        }

        else if (question.contains("location") || question.contains("where")) {
            return "We are located in Nairobi CBD, along Moi Avenue.";
        }

        else if (question.contains("pay") || question.contains("mpesa")) {
            return "We accept M-Pesa (Till No: 123456) and Cash on Delivery.";
        }

        else if (question.contains("deliver") || question.contains("shipping")) {
            return "Yes! We deliver countrywide. Delivery fee is Ksh 200 within Nairobi.";
        }

        else if (question.contains("time") || question.contains("open") || question.contains("hours")) {
            return "We are open Monday to Saturday, 8:00 AM to 6:00 PM.";
        }

        else if (question.contains("return") || question.contains("refund")) {
            return "You can return damaged items within 48 hours of delivery.";
        }

        else {
            return "I am not sure about that. An agent will call you shortly for help.";
        }
    }
}