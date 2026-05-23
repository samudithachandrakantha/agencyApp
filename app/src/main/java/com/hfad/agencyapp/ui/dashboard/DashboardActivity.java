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
import com.hfad.agencyapp.ui.auth.LoginActivity;
import com.hfad.agencyapp.ui.invoice.CreateInvoiceActivity;
import com.hfad.agencyapp.ui.insights.InsightsActivity;
import com.hfad.agencyapp.ui.products.ProductsActivity;
import com.hfad.agencyapp.ui.tabs.MainTabsActivity;
import com.hfad.agencyapp.viewmodel.DashboardViewModel;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private RecentInvoiceAdapter invoiceAdapter;
    private DashboardViewModel viewModel;

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

                for (com.hfad.agencyapp.data.entities.Invoice invoice : invoices) {
                    String customerName = invoice.customerName != null && !invoice.customerName.isEmpty()
                            ? invoice.customerName
                            : "Unknown";

                    String paymentStatus = "Pending";
                    if (invoice.paidAmount >= invoice.totalAmount) {
                        paymentStatus = "Paid";
                    } else if (invoice.status != null && invoice.status.equals("CANCELLED")) {
                        paymentStatus = "Cancelled";
                    } else if (invoice.paidAmount > 0) {
                        paymentStatus = "Partial";
                    }

                    if (invoice.paymentMethod != null && !invoice.paymentMethod.isEmpty() && paymentStatus.equals("Pending")) {
                        if (invoice.paymentMethod.equals("CHEQUE")) {
                            paymentStatus = "Cheque";
                        } else if (invoice.paymentMethod.equals("CREDIT")) {
                            paymentStatus = "Credit";
                        }
                    }

                    uiModels.add(new com.hfad.agencyapp.ui.models.RecentInvoiceUiModel(
                            customerName,
                            invoice.invoiceNumber,
                            invoice.id,
                            "Rs. " + currencyFormat.format(invoice.totalAmount),
                            paymentStatus
                    ));
                }
            }

            invoiceAdapter.submitList(uiModels);
            invoiceAdapter.setOnInvoiceClickListener(invoiceDbId -> com.hfad.agencyapp.utils.PreviewUtils.showInvoicePreview(DashboardActivity.this, invoiceDbId));
        });
    }

    private void setupQuickActions() {
        binding.actionNewInvoice.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateInvoiceActivity.class);
            startActivity(intent);
        });
        binding.actionCustomers.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.hfad.agencyapp.ui.customers.CustomersActivity.class);
            startActivity(intent);
        });
        binding.actionProducts.setOnClickListener(v -> startActivity(new Intent(this, ProductsActivity.class)));
        binding.actionSync.setOnClickListener(v -> showFeatureToast("Sync"));

        binding.tvViewAll.setOnClickListener(v -> startActivity(new android.content.Intent(this, com.hfad.agencyapp.ui.invoice.InvoicesActivity.class)));
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

    private void showFeatureToast(String feature) {
        Toast.makeText(this, feature + " coming soon", Toast.LENGTH_SHORT).show();
    }
}

