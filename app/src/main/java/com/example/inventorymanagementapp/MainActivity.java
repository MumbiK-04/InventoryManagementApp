package com.example.inventorymanagementapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView tvStockStatus, tvTotalProfit, tvTopItem, tvMarginPercent, tvPendingDeliveries;
    private TextView tvHeroPendingCount, tvHeroStockHealth;
    private ProgressBar progressMargin;
    private LineChart salesChart;
    private PieChart profitPieChart;
    private FirebaseFirestore db;

    private TextView navInventory, navAdd, navOrders, navCustomers, navReports;
    private CardView navStockCRUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- UI Initializations ---
        tvStockStatus = findViewById(R.id.tvStockStatus);
        tvTotalProfit = findViewById(R.id.tvTotalProfit);
        tvTopItem = findViewById(R.id.tvTopItem);
        tvMarginPercent = findViewById(R.id.tvMarginPercent);
        progressMargin = findViewById(R.id.progressMargin);
        salesChart = findViewById(R.id.salesChartSnippet);
        tvPendingDeliveries = findViewById(R.id.tvPendingDeliveries);
        profitPieChart = findViewById(R.id.profitPieChart);

        // --- New Hero Card Initializations ---
        tvHeroPendingCount = findViewById(R.id.tvHeroPendingCount);
        tvHeroStockHealth = findViewById(R.id.tvHeroStockHealth);

        ImageView btnLogout = findViewById(R.id.btnLogout);
        salesChart.getDescription().setEnabled(false);
        profitPieChart.getDescription().setEnabled(false);

        // --- Navigation Initializations ---
        navInventory = findViewById(R.id.navInventory);
        navAdd = findViewById(R.id.navAdd);
        navOrders = findViewById(R.id.navOrders);
        navCustomers = findViewById(R.id.navCustomers);
        navReports = findViewById(R.id.navReports);
        navStockCRUD = findViewById(R.id.navStockCRUD);

        db = FirebaseFirestore.getInstance();

        // --- Theme Colors ---
        tvTotalProfit.setTextColor(Color.parseColor("#4A148C"));
        tvMarginPercent.setTextColor(Color.parseColor("#BA68C8"));

        // Navigation Actions
        navInventory.setOnClickListener(v -> startActivity(new Intent(this, InventoryActivity.class)));
        navAdd.setOnClickListener(v -> startActivity(new Intent(this, AddBookActivity.class)));
        navOrders.setOnClickListener(v -> startActivity(new Intent(this, OrdersActivity.class)));
        navCustomers.setOnClickListener(v -> startActivity(new Intent(this, UserListActivity.class)));
        navReports.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));

        if (navStockCRUD != null) {
            navStockCRUD.setOnClickListener(v -> startActivity(new Intent(this, InventoryActivity.class)));
        }

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        loadStockLevels();
        loadRealSalesData();
        detectUndeliveredGoods();
    }

    private void loadStockLevels() {
        db.collection("books").addSnapshotListener((snapshots, e) -> {
            if (snapshots != null) {
                int lowStockItems = 0;
                for (QueryDocumentSnapshot doc : snapshots) {
                    Long qty = doc.getLong("quantity");
                    if (qty != null && qty < 5) lowStockItems++;
                }

                if (lowStockItems > 0) {
                    String status = lowStockItems + " LOW";
                    tvStockStatus.setText(status);
                    tvHeroStockHealth.setText(status); // Update Hero Card
                    tvHeroStockHealth.setTextColor(Color.RED);
                    tvStockStatus.setTextColor(Color.RED);
                } else {
                    tvStockStatus.setText("Healthy");
                    tvHeroStockHealth.setText("Healthy"); // Update Hero Card
                    tvHeroStockHealth.setTextColor(Color.parseColor("#4A148C"));
                    tvStockStatus.setTextColor(Color.parseColor("#4CAF50"));
                }
            }
        });
    }

    private void detectUndeliveredGoods() {
        db.collection("orders")
                .whereEqualTo("status", "PAID")
                .addSnapshotListener((snapshots, e) -> {
                    if (snapshots != null) {
                        int pending = snapshots.size();

                        tvPendingDeliveries.setText(String.format(Locale.getDefault(), "Pending: %d", pending));
                        tvPendingDeliveries.setVisibility(pending > 0 ? View.VISIBLE : View.GONE);

                        tvHeroPendingCount.setText(String.valueOf(pending));
                    }
                });
    }

    @SuppressLint("DefaultLocale")
    private void loadRealSalesData() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        Date startOfWeek = cal.getTime();

        db.collection("orders")
                .whereGreaterThanOrEqualTo("date", startOfWeek)
                .orderBy("date", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    double totalRevenue = 0, actualProfit = 0;
                    HashMap<String, Integer> itemTracker = new HashMap<>();
                    HashMap<String, Double> profitByitem = new HashMap<>();
                    List<Entry> lineEntries = new ArrayList<>();
                    int dayCounter = 1;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Double price = doc.getDouble("price");
                        Double profit = doc.getDouble("orderProfit");
                        String item = doc.getString("item");

                        if (price != null) {
                            totalRevenue += price;
                            lineEntries.add(new Entry(dayCounter++, price.floatValue()));
                        }

                        if (profit != null) {
                            actualProfit += profit;
                            if (item != null) {
                                Double currentCatProfit = profitByitem.get(item);
                                double safeProfit = (currentCatProfit != null) ? currentCatProfit : 0.0;
                                profitByitem.put(item, safeProfit + profit);

                                Integer currentCount = itemTracker.get(item);
                                int safeCount = (currentCount != null) ? currentCount : 0;
                                itemTracker.put(item, safeCount + 1);
                            }
                        }
                    }

                    tvTotalProfit.setText(String.format("Ksh %.2f", actualProfit));
                    int margin = totalRevenue > 0 ? (int) ((actualProfit / totalRevenue) * 100) : 0;
                    progressMargin.setProgress(margin);
                    tvMarginPercent.setText(String.format("%d%%", margin));

                    updateTopItem(itemTracker);
                    drawGraph(lineEntries);
                    preparePieChart(profitByitem);
                });
    }

    private void preparePieChart(Map<String, Double> data) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }
        drawPieChart(entries);
    }

    private void drawPieChart(List<PieEntry> entries) {
        if (entries.isEmpty()) {
            profitPieChart.setNoDataText("No data available");
            profitPieChart.invalidate();
            return;
        }

        PieDataSet set = new PieDataSet(entries, "");
        int[] lilacPalette = {
                Color.parseColor("#E1BEE7"), Color.parseColor("#CE93D8"),
                Color.parseColor("#BA68C8"), Color.parseColor("#9C27B0"),
                Color.parseColor("#7B1FA2")
        };
        set.setColors(lilacPalette);
        set.setValueTextSize(12f);
        set.setValueTextColor(Color.WHITE);

        PieData data = new PieData(set);
        profitPieChart.setData(data);
        profitPieChart.setHoleColor(Color.WHITE);
        profitPieChart.setCenterText("Profit Share");
        profitPieChart.animateXY(800, 800);
        profitPieChart.invalidate();
    }

    private void updateTopItem(HashMap<String, Integer> tracker) {
        String topSeller = "None";
        int max = 0;
        for (Map.Entry<String, Integer> entry : tracker.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                topSeller = entry.getKey();
            }
        }
        tvTopItem.setText(topSeller);
    }

    private void drawGraph(List<Entry> entries) {
        if (entries.isEmpty()) entries.add(new Entry(1, 0));
        LineDataSet dataSet = new LineDataSet(entries, "Weekly Sales");
        dataSet.setColor(Color.parseColor("#BA68C8"));
        dataSet.setCircleColor(Color.parseColor("#4A148C"));
        dataSet.setLineWidth(3f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#F3E5F5"));

        salesChart.setData(new LineData(dataSet));
        salesChart.invalidate();
    }
}