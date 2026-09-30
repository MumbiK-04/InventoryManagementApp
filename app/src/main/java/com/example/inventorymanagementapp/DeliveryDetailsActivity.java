package com.example.inventorymanagementapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class DeliveryDetailsActivity extends AppCompatActivity {

    private TextInputEditText etCounty, etDetailedAddress;
    private AutoCompleteTextView etSubCounty;
    private Button btnConfirmDelivery;
    private RadioGroup rgFulfillment;

    private double itemSubtotal = 0.0;
    private double deliveryFee = 0.0;
    private double totalAmount = 0.0;

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_details);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        etCounty = findViewById(R.id.etCounty);
        etSubCounty = findViewById(R.id.etSubCounty);
        etDetailedAddress = findViewById(R.id.etDetailedAddress);
        btnConfirmDelivery = findViewById(R.id.btnConfirmDelivery);
        rgFulfillment = findViewById(R.id.rgFulfillment);

        btnConfirmDelivery.setEnabled(false);
        btnConfirmDelivery.setAlpha(0.5f);

        itemSubtotal = getIntent().getDoubleExtra("ITEM_SUBTOTAL", 0.0);

        rgFulfillment.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isPickup = (checkedId == R.id.rbPickup);
            if (isPickup) {
                etCounty.setText("Nairobi");
                etSubCounty.setText("Tausi CBD Store (Main)");
                etDetailedAddress.setText("Mfangano Street, Nairobi CBD");
                etSubCounty.setAdapter(null);
                deliveryFee = 0.0;
                etCounty.setEnabled(false);
                etSubCounty.setEnabled(false);
                etDetailedAddress.setEnabled(false);
            } else {
                etCounty.setEnabled(true);
                etSubCounty.setEnabled(true);
                etDetailedAddress.setEnabled(true);
                etCounty.setText("");
                etSubCounty.setText("");
                etDetailedAddress.setText("");
            }
            updatePricingAndLogistics();
            validateForm();
        });

        setupListeners();
    }

    private void setupListeners() {
        TextWatcher commonWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePricingAndLogistics();
                validateForm();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        };

        etCounty.addTextChangedListener(commonWatcher);
        etSubCounty.addTextChangedListener(commonWatcher);
        etDetailedAddress.addTextChangedListener(commonWatcher);

        btnConfirmDelivery.setOnClickListener(v -> showOrderSummaryDialog());
    }

    private void updatePricingAndLogistics() {
        String county = etCounty.getText() != null ? etCounty.getText().toString().trim().toLowerCase() : "";
        String sub = etSubCounty.getText() != null ? etSubCounty.getText().toString().trim().toLowerCase() : "";
        String[] options;

        if (rgFulfillment.getCheckedRadioButtonId() == R.id.rbPickup) {
            deliveryFee = 0.0;
        } else if (county.equals("nairobi")) {
            options = new String[]{"CBD", "Westlands", "Kasarani", "Lang'ata", "Embakasi"};
            deliveryFee = 200.0;
            updateAdapter(options);
        } else if (county.equals("kiambu")) {
            options = new String[]{"Thika", "Ruiru", "Kikuyu", "Kiambu Town"};
            deliveryFee = sub.contains("thika") ? 300.0 : 200.0;
            updateAdapter(options);
        } else if (county.equals("nyeri") || county.equals("murang'a") || county.equals("kirinyaga")) {
            options = new String[]{"2NK Sacco", "4NTE Sacco", "G4S Courier", "Wells Fargo"};
            deliveryFee = 300.0;
            updateAdapter(options);
        } else {
            options = new String[]{"Madiwa Sacco", "North Rift Shuttle", "Guardian Angel", "Easy Coach", "G4S Courier"};
            deliveryFee = county.isEmpty() ? 0.0 : 450.0;
            updateAdapter(options);
        }

        totalAmount = itemSubtotal + deliveryFee;
        btnConfirmDelivery.setText(String.format(Locale.getDefault(), "Pay KSh %d", (int) totalAmount));
    }

    private void updateAdapter(String[] options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, options);
        etSubCounty.setAdapter(adapter);
    }

    private void validateForm() {
        boolean isReady = etCounty.getText() != null && !etCounty.getText().toString().trim().isEmpty() &&
                etSubCounty.getText() != null && !etSubCounty.getText().toString().trim().isEmpty() &&
                etDetailedAddress.getText() != null && !etDetailedAddress.getText().toString().trim().isEmpty();
        btnConfirmDelivery.setEnabled(isReady);
        btnConfirmDelivery.setAlpha(isReady ? 1.0f : 0.5f);
    }

    private void showOrderSummaryDialog() {
        String orderSummary = "Items Subtotal: KSh " + itemSubtotal +
                "\nDelivery Fee: KSh " + deliveryFee +
                "\nTotal Amount: KSh " + totalAmount;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Order Summary");

        final EditText input = new EditText(this);
        input.setHint("07XXXXXXXX");
        input.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

        builder.setMessage(orderSummary + "\n\nEnter M-Pesa Number:");

        builder.setPositiveButton("Pay Now", (dialog, which) -> {
            String phone = input.getText().toString().trim();
            if (phone.length() >= 10) {
                initiateDarajaPayment(phone);
            } else {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @SuppressWarnings("deprecation")
    private void initiateDarajaPayment(String phone) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Initiating M-Pesa STK Push...");
        pd.setCancelable(false);
        pd.show();

        DarajaApiClient daraja = new DarajaApiClient();
        daraja.triggerStkPush(phone, String.valueOf((int) totalAmount), new DarajaApiClient.MpesaListener() {
            @Override
            public void onSuccess() {
                pd.dismiss();
                moveCartToOrders(phone);
            }

            @Override
            public void onError(String error) {
                pd.dismiss();
                Toast.makeText(DeliveryDetailsActivity.this, "M-Pesa Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void moveCartToOrders(String phone) {
        if (uid == null) return;

        final String orderId = "TAUSI-" + (new Random().nextInt(9000) + 1000);
        final String county = etCounty.getText() != null ? etCounty.getText().toString() : "N/A";
        final String subCounty = etSubCounty.getText() != null ? etSubCounty.getText().toString() : "N/A";
        final String deliveryWay = etSubCounty.getText().toString(); // e.g. "2NK Sacco"
        final String exactAddress = etDetailedAddress.getText().toString();

        // 1. FIRST: Fetch the actual name from the Customer's Profile
        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {

            String realName = userDoc.getString("name");
            if (realName == null || realName.isEmpty()) {
                realName = "Registered Customer"; // Fallback
            }

            final String customerDisplayName = realName;

            // 2. SECOND: Move the items from Cart to the Admin's Global Orders
            db.collection("users").document(uid).collection("cart").get()
                    .addOnSuccessListener(snapshots -> {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Map<String, Object> item = doc.getData();
                            Map<String, Object> adminOrder = new HashMap<>();

                            String title = String.valueOf(item.get("title") != null ? item.get("title") : "Item");

                            // USE THE REAL NAME FETCHED ABOVE
                            adminOrder.put("customer", customerDisplayName + " (" + phone + ")");
                            adminOrder.put("customerName", customerDisplayName);
                            adminOrder.put("customerUid", uid); // CRITICAL: This allows the Admin to send the Alert back

                            adminOrder.put("date", new Date());
                            adminOrder.put("item", title);
                            adminOrder.put("orderId", orderId);
                            adminOrder.put("price", item.get("price"));
                            adminOrder.put("quantity", item.get("quantity"));
                            adminOrder.put("deliveryLocation", county + ", " + subCounty);
                            adminOrder.put("status", "PAID");
                            adminOrder.put("deliveryWay", deliveryWay);
                            adminOrder.put("fullAddress", county + " - " + exactAddress);

                            // Save to the "orders" collection that the Admin sees
                            db.collection("orders").add(adminOrder);

                            // Inventory Update Logic
                            String cat = String.valueOf(item.get("category"));
                            String path = cat.equalsIgnoreCase("stationery") ? "stationery" : "books";
                            long qty = 1;
                            if (item.get("quantity") instanceof Number) qty = ((Number) item.get("quantity")).longValue();

                            db.collection(path).document(doc.getId()).update("quantity", FieldValue.increment(-qty));

                            // Clear the customer's cart
                            db.collection("users").document(uid).collection("cart").document(doc.getId()).delete();
                        }
                        showFinalSuccessMessage();
                    });

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Profile Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    private void showFinalSuccessMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Order Placed");
        String message = (rgFulfillment.getCheckedRadioButtonId() == R.id.rbPickup)
                ? "Payment verified. Collect items at the CBD store."
                : "Payment verified. Admin will dispatch items shortly.";

        builder.setMessage(message);
        builder.setPositiveButton("Finish", (dialog, which) -> finish());
        builder.setCancelable(false);
        builder.show();
    }
}