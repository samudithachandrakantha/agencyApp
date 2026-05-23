package com.hfad.agencyapp.ui.invoice;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hfad.agencyapp.R;
import com.hfad.agencyapp.databinding.ActivityInvoicesBinding;
import com.hfad.agencyapp.ui.customers.CustomersActivity;
import com.hfad.agencyapp.ui.adapters.RecentInvoiceAdapter;
import com.hfad.agencyapp.ui.insights.InsightsActivity;
import com.hfad.agencyapp.ui.tabs.MainTabsActivity;
import com.hfad.agencyapp.viewmodel.DashboardViewModel;

import java.util.ArrayList;

public class InvoicesActivity extends AppCompatActivity {

    private ActivityInvoicesBinding binding;
    private RecentInvoiceAdapter adapter;
    private DashboardViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInvoicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up toolbar with back button and navy styling to match app
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        adapter = new RecentInvoiceAdapter();
        binding.recyclerAllInvoices.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerAllInvoices.setAdapter(adapter);

        binding.includeBottomNav.bottomNav.setSelectedItemId(R.id.nav_invoices);
        binding.includeBottomNav.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(MainTabsActivity.createIntent(this, MainTabsActivity.TAB_HOME));
                finish();
                return true;
            }
            if (id == R.id.nav_invoices) {
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

        viewModel.invoices.observe(this, invoices -> {
            java.util.List<com.hfad.agencyapp.ui.models.RecentInvoiceUiModel> ui = new ArrayList<>();
            if (invoices != null && !invoices.isEmpty()) {
                java.text.DecimalFormat fmt = new java.text.DecimalFormat("#,##0.00");
                for (com.hfad.agencyapp.data.entities.Invoice inv : invoices) {
                    String customer = inv.customerName != null && !inv.customerName.isEmpty() ? inv.customerName : "Unknown";
                    String status = "Pending";
                    if (inv.paidAmount >= inv.totalAmount) status = "Paid";
                    ui.add(new com.hfad.agencyapp.ui.models.RecentInvoiceUiModel(customer, inv.invoiceNumber, inv.id, "Rs. " + fmt.format(inv.totalAmount), status));
                }
            }
            adapter.submitList(ui);
            adapter.setOnInvoiceClickListener(invoiceDbId -> com.hfad.agencyapp.utils.PreviewUtils.showInvoicePreview(InvoicesActivity.this, invoiceDbId));
        });
    }
}
