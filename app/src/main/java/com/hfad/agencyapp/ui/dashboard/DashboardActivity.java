package com.hfad.agencyapp.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hfad.agencyapp.databinding.ActivityDashboardBinding;
import com.hfad.agencyapp.R;
import com.hfad.agencyapp.ui.adapters.RecentInvoiceAdapter;
import com.hfad.agencyapp.ui.invoice.CreateInvoiceActivity;
import com.hfad.agencyapp.ui.products.ProductsActivity;
import com.hfad.agencyapp.ui.profile.ProfileActivity;
import com.hfad.agencyapp.ui.tabs.MainTabsActivity;
import com.hfad.agencyapp.viewmodel.DashboardViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private RecentInvoiceAdapter invoiceAdapter;
    private DashboardViewModel viewModel;
    private final SimpleDateFormat invoiceCardDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.navy_900));

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupQuickActions();
        setupBottomNavigation();
        setupProfileEntry();
    }

    private void setupRecyclerView() {
        binding.recyclerRecentInvoices.setLayoutManager(new LinearLayoutManager(this));
        invoiceAdapter = new RecentInvoiceAdapter();
        binding.recyclerRecentInvoices.setAdapter(invoiceAdapter);
    }

    private void setupObservers() {
        viewModel.todaySales.observe(this, sales -> {
            double value = sales != null ? sales : 0.0;
            binding.tvTodaySales.setText(getString(R.string.amount_format, new java.text.DecimalFormat("#,##0.00").format(value)));
        });

        viewModel.todayInvoiceCount.observe(this, count -> {
            int value = count != null ? count : 0;
            binding.tvInvoiceCount.setText(String.valueOf(value));
        });

        viewModel.invoices.observe(this, invoices -> {
            java.util.List<com.hfad.agencyapp.ui.models.RecentInvoiceUiModel> uiModels = new java.util.ArrayList<>();
            if (invoices != null && !invoices.isEmpty()) {
                java.text.DecimalFormat currencyFormat = new java.text.DecimalFormat("#,##0.00");
                java.text.SimpleDateFormat chequeDisplayFormat = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault());

                for (com.hfad.agencyapp.data.entities.Invoice invoice : invoices) {
                    String customerName = invoice.customerName != null && !invoice.customerName.isEmpty()
                            ? invoice.customerName
                            : "Unknown";
                    String invoiceNumber = invoice.invoiceNumber != null && !invoice.invoiceNumber.isEmpty() ? invoice.invoiceNumber : "-";
                    String invoiceDate = invoice.createdAt > 0 ? invoiceCardDateFormat.format(new Date(invoice.createdAt)) : "-";
                    String invoiceCardSubtitle = invoiceNumber + " | " + invoiceDate;

                    String paymentStatus = "";
                    boolean isPending = false;
                    String dueAmount = "0.00";
                    String chequeDate = "";
                    
                    if (invoice.status != null && invoice.status.equals("CANCELLED")) {
                        paymentStatus = "Cancelled";
                    } else if ((invoice.status != null && (invoice.status.equals("COMPLETED") || invoice.status.equals("PAID")))
                            || invoice.paidAmount >= invoice.totalAmount) {
                        paymentStatus = "Paid";
                    } else if (invoice.paymentMethod != null && invoice.paymentMethod.equals("CASH")) {
                        paymentStatus = "Cash";
                    } else if (invoice.paymentMethod != null && invoice.paymentMethod.equals("CHEQUE")) {
                        paymentStatus = "Pending";
                        isPending = true;
                        // For cheque, show cheque date instead of due amount
                        if (invoice.chequeDate > 0) {
                            chequeDate = chequeDisplayFormat.format(new Date(invoice.chequeDate));
                        }
                    } else if (invoice.paymentMethod != null && invoice.paymentMethod.equals("CREDIT")) {
                        paymentStatus = "Pending";
                        isPending = true;
                        double due = invoice.totalAmount - invoice.paidAmount;
                        dueAmount = currencyFormat.format(Math.max(0, due));
                    } else if (invoice.paidAmount > 0) {
                        paymentStatus = "Partial";
                        isPending = true;
                        double due = invoice.totalAmount - invoice.paidAmount;
                        dueAmount = currencyFormat.format(Math.max(0, due));
                    }

                    uiModels.add(new com.hfad.agencyapp.ui.models.RecentInvoiceUiModel(
                            customerName,
                            invoiceCardSubtitle,
                            invoice.id,
                            "Rs. " + currencyFormat.format(invoice.totalAmount),
                            paymentStatus,
                            dueAmount,
                            isPending,
                            chequeDate
                    ));
                }
            }

            invoiceAdapter.submitList(uiModels);
            invoiceAdapter.setOnInvoiceClickListener(invoiceDbId -> com.hfad.agencyapp.utils.PreviewUtils.showInvoicePreview(DashboardActivity.this, invoiceDbId));
        });
    }

    private void setupQuickActions() {
        binding.cardTodaySales.setOnClickListener(v -> {
            startActivity(MainTabsActivity.createIntent(this, MainTabsActivity.TAB_INSIGHTS));
            finish();
        });

        binding.actionNewInvoice.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateInvoiceActivity.class);
            startActivity(intent);
        });
        // Customers quick-action removed
        binding.actionProducts.setOnClickListener(v -> startActivity(new Intent(this, ProductsActivity.class)));
        binding.actionSync.setOnClickListener(v -> showFeatureToast());

        binding.tvViewAll.setOnClickListener(v -> startActivity(new android.content.Intent(this, com.hfad.agencyapp.ui.invoice.InvoicesActivity.class)));
    }

    private void setupProfileEntry() {
        binding.tvAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.content.SharedPreferences prefs = getSharedPreferences("cheque_prefs", MODE_PRIVATE);
        int count = prefs.getInt(com.hfad.agencyapp.workers.ChequeNotificationWorker.KEY_COUNT, 0);
        if (count > 0) {
            binding.tvAvatarBadge.setVisibility(android.view.View.VISIBLE);
            binding.tvAvatarBadge.setText(String.valueOf(count));
        } else {
            binding.tvAvatarBadge.setVisibility(android.view.View.GONE);
        }
    }

    private void setupBottomNavigation() {
        binding.includeBottomNav.bottomNav.setSelectedItemId(R.id.nav_home);
        binding.includeBottomNav.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(MainTabsActivity.createIntent(this, MainTabsActivity.TAB_HOME));
                finish();
                return true;
            }
            if (id == R.id.nav_invoices) {
                startActivity(MainTabsActivity.createIntent(this, MainTabsActivity.TAB_INVOICES));
                finish();
                return true;
            }
            if (id == R.id.nav_customers) {
                startActivity(MainTabsActivity.createIntent(this, MainTabsActivity.TAB_CUSTOMERS));
                finish();
                return true;
            }
            if (id == R.id.nav_insights) {
                startActivity(MainTabsActivity.createIntent(this, MainTabsActivity.TAB_INSIGHTS));
                finish();
                return true;
            }
            return false;
        });
    }

    private void showFeatureToast() {
        Toast.makeText(this, "Sync coming soon", Toast.LENGTH_SHORT).show();
    }
}

