package com.example.inventorymanagementapp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {

    FirebaseFirestore db;
    Button btnDownloadPdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        db = FirebaseFirestore.getInstance();
        btnDownloadPdf = findViewById(R.id.btnDownloadPdf);

        btnDownloadPdf.setOnClickListener(v -> showPeriodSelection());
    }

    private void showPeriodSelection() {
        String[] options = {"Last 7 Days", "Last 30 Days", "All Time"};
        new AlertDialog.Builder(this)
                .setTitle("Select Report Period")
                .setItems(options, (dialog, which) -> {
                    Calendar cal = Calendar.getInstance();
                    if (which == 0) cal.add(Calendar.DAY_OF_YEAR, -7);
                    else if (which == 1) cal.add(Calendar.DAY_OF_YEAR, -30);
                    else cal.set(2024, 0, 1);

                    fetchDataAndDownload(cal.getTime(), options[which]);
                }).show();
    }

    private void fetchDataAndDownload(Date startDate, String label) {
        db.collection("orders")
                .whereGreaterThanOrEqualTo("date", startDate)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Order> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Order o = doc.toObject(Order.class);
                        list.add(o);
                    }
                    if (list.isEmpty()) {
                        Toast.makeText(this, "No orders found for this period", Toast.LENGTH_SHORT).show();
                    } else {
                        generateAndSavePDF(list, label);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void generateAndSavePDF(List<Order> orders, String periodLabel) {
        PdfDocument pdf = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        // A4 Page Settings
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // 1. Header Section
        titlePaint.setTextSize(22f);
        titlePaint.setFakeBoldText(true);
        titlePaint.setColor(Color.parseColor("#4A148C"));
        canvas.drawText("TAUSI BOOKS BUSINESS REPORT", 80, 60, titlePaint);

        paint.setTextSize(12f);
        paint.setColor(Color.BLACK);
        canvas.drawText("Period: " + periodLabel, 50, 95, paint);
        canvas.drawText("Generated: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()), 50, 115, paint);

        // 2. Table Headers
        int y = 160;
        paint.setFakeBoldText(true);
        canvas.drawText("Date", 40, y, paint);
        canvas.drawText("Customer", 100, y, paint);
        canvas.drawText("Phone Number", 230, y, paint);
        canvas.drawText("Item Purchased", 360, y, paint);
        canvas.drawText("Price (Ksh)", 500, y, paint);
        canvas.drawLine(40, y + 8, 560, y + 8, paint);

        // 3. Table Rows (Data Extraction)
        y += 35;
        paint.setFakeBoldText(false);
        double totalRev = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

        for (Order o : orders) {
            if (y > 800) break; // Simple page break check

            // Date String
            String dateStr = o.getDate() != null ? sdf.format(o.getDate()) : "N/A";

            // Extracting Name and Phone from "Buyer (0712...)"
            String rawCustomer = o.getCustomer() != null ? o.getCustomer() : "Unknown";
            String nameOnly = rawCustomer;
            String phoneOnly = "---";

            if (rawCustomer.contains("(") && rawCustomer.contains(")")) {
                try {
                    nameOnly = rawCustomer.substring(0, rawCustomer.indexOf("(")).trim();
                    phoneOnly = rawCustomer.substring(rawCustomer.indexOf("(") + 1, rawCustomer.indexOf(")"));
                } catch (Exception e) {
                    nameOnly = rawCustomer;
                }
            }

            // Draw text for the row
            canvas.drawText(dateStr, 40, y, paint);
            canvas.drawText(shorten(nameOnly, 16), 100, y, paint);
            canvas.drawText(phoneOnly, 230, y, paint);
            canvas.drawText(shorten(o.getItem(), 20), 360, y, paint);
            canvas.drawText(String.valueOf(o.getPrice()), 500, y, paint);

            totalRev += o.getPrice();
            y += 25;
        }

        // 4. Footer Summary
        canvas.drawLine(40, y, 560, y, paint);
        y += 40;
        paint.setFakeBoldText(true);
        paint.setTextSize(15f);
        canvas.drawText("TOTAL SALES REVENUE: Ksh " + totalRev, 280, y, paint);

        pdf.finishPage(page);

        // 5. Save the File to Downloads
        String fileName = "Tausi_Sales_Report_" + System.currentTimeMillis() + ".pdf";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);


        try {
            // Attempting to write the report to the Downloads folder
            pdf.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF saved to Downloads folder!", Toast.LENGTH_LONG).show();

            // Trigger Media Scanner so user sees file immediately in File Manager
            MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);

        } catch (IOException e) {
            // FIX 1: 'Log' is now resolved because of the import above
            Log.e("PDF_EXPORT", "Error saving report: " + e.getMessage(), e);

            Toast.makeText(this, "Export Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();

        } finally {
            // FIX 2: Removed the 'if (pdf != null)' check.
            // Since 'pdf' was initialized at the start of the method, the IDE knows it can't be null here.
            pdf.close();
        }
    }

    private String shorten(String s, int limit) {
        if (s == null || s.isEmpty()) return "N/A";
        return s.length() > limit ? s.substring(0, limit - 2) + ".." : s;
    }
}
