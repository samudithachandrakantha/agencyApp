package com.hfad.agencyapp.ui.profile;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hfad.agencyapp.data.Repository;
import com.hfad.agencyapp.data.entities.Invoice;
import com.hfad.agencyapp.databinding.ActivityProfileBinding;
import com.hfad.agencyapp.ui.adapters.NotificationsAdapter;
import com.hfad.agencyapp.ui.models.NotificationUiModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final String PROFILE_NAME = "Shanka Distributors";
    private static final String PROFILE_INITIALS = "SD";

    private ActivityProfileBinding binding;
    private Repository repository;
    private NotificationsAdapter adapter;
    
    private final SimpleDateFormat chequeDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final SimpleDateFormat clearanceDateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = Repository.getInstance(this);

        binding.profileBack.setOnClickListener(v -> finish());

        bindProfile();
        setupRecyclerView();
    }

    private void bindProfile() {
        binding.tvProfileAvatar.setText(PROFILE_INITIALS);
        binding.tvProfileName.setText(PROFILE_NAME);
    }

    private void setupRecyclerView() {
        binding.recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationsAdapter();
        binding.recyclerNotifications.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
        clearNotificationBadge();
    }

    private void clearNotificationBadge() {
        try {
            android.content.SharedPreferences prefs = getSharedPreferences("cheque_prefs", MODE_PRIVATE);
            prefs.edit().putInt("cheque_notification_count", 0).apply();
        } catch (Exception ignored) { }
    }

    private void loadNotifications() {
        new Thread(() -> {
            List<Invoice> invoices = repository.getAllInvoicesOnce();
            List<NotificationUiModel> uiModels = new ArrayList<>();
            
            if (invoices != null && !invoices.isEmpty()) {
                // Filter for cheque invoices and sort by chequeDate ascending (earliest first)
                List<Invoice> chequeInvoices = new ArrayList<>();
                for (Invoice invoice : invoices) {
                    if (invoice != null && invoice.chequeDate > 0 && "CHEQUE".equalsIgnoreCase(invoice.paymentMethod)) {
                        chequeInvoices.add(invoice);
                    }
                }
                
                Collections.sort(chequeInvoices, (a, b) -> Long.compare(a.chequeDate, b.chequeDate));
                
                long now = System.currentTimeMillis();
                
                for (Invoice invoice : chequeInvoices) {
                    // Calculate clearance schedule
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(invoice.chequeDate);
                    int dow = cal.get(Calendar.DAY_OF_WEEK);
                    int addDays = (dow == Calendar.FRIDAY || dow == Calendar.SATURDAY) ? 3 : 1;
                    cal.add(Calendar.DATE, addDays);
                    cal.set(Calendar.HOUR_OF_DAY, 14);
                    cal.set(Calendar.MINUTE, 30);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);

                    long clearanceTime = cal.getTimeInMillis();
                    
                    // Determine status
                    String status;
                    if ("COMPLETED".equalsIgnoreCase(invoice.status) || "PAID".equalsIgnoreCase(invoice.status)) {
                        status = "CLEARED";
                    } else if (clearanceTime < now) {
                        status = "OVERDUE";
                    } else {
                        // Check if clearance is today
                        Calendar calToday = Calendar.getInstance();
                        Calendar calClearance = Calendar.getInstance();
                        calClearance.setTimeInMillis(clearanceTime);
                        boolean isToday = calToday.get(Calendar.YEAR) == calClearance.get(Calendar.YEAR) &&
                                          calToday.get(Calendar.DAY_OF_YEAR) == calClearance.get(Calendar.DAY_OF_YEAR);
                        if (isToday) {
                            status = "DUE TODAY";
                        } else {
                            status = "UPCOMING";
                        }
                    }

                    String amountText = "Rs. " + new java.text.DecimalFormat("#,##0.00").format(invoice.totalAmount);
                    String chqDateStr = chequeDateFormat.format(new Date(invoice.chequeDate));
                    String clrDateStr = clearanceDateFormat.format(new Date(clearanceTime));
                    
                    uiModels.add(new NotificationUiModel(
                            invoice.id,
                            "Cheque Clearance Reminder",
                            invoice.customerName != null ? invoice.customerName : "Unknown Customer",
                            invoice.invoiceNumber != null ? invoice.invoiceNumber : "INV-",
                            amountText,
                            chqDateStr,
                            clrDateStr,
                            status
                    ));
                }
            }

            runOnUiThread(() -> {
                adapter.submitList(uiModels);
                if (uiModels.isEmpty()) {
                    binding.recyclerNotifications.setVisibility(View.GONE);
                    binding.layoutEmptyNotifications.setVisibility(View.VISIBLE);
                } else {
                    binding.recyclerNotifications.setVisibility(View.VISIBLE);
                    binding.layoutEmptyNotifications.setVisibility(View.GONE);
                }
            });
        }).start();
    }
}