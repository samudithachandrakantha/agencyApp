package com.hfad.agencyapp.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hfad.agencyapp.databinding.ActivityDashboardBinding;
import com.hfad.agencyapp.R;
import com.hfad.agencyapp.ui.adapters.RecentInvoiceAdapter;
import com.hfad.agencyapp.ui.auth.LoginActivity;
import com.hfad.agencyapp.ui.invoice.CreateInvoiceActivity;
import com.hfad.agencyapp.ui.models.RecentInvoiceUiModel;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private RecentInvoiceAdapter invoiceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.navy_900));

        setupRecyclerView();
        setupQuickActions();
        setupBottomNavigation();
    }

    private void setupRecyclerView() {
        binding.recyclerRecentInvoices.setLayoutManager(new LinearLayoutManager(this));
        invoiceAdapter = new RecentInvoiceAdapter();
        binding.recyclerRecentInvoices.setAdapter(invoiceAdapter);

        List<RecentInvoiceUiModel> sample = new ArrayList<>();
        sample.add(new RecentInvoiceUiModel("ABC Bakers", "INV-2026-001", "Rs. 12,400", "Paid"));
        sample.add(new RecentInvoiceUiModel("Sugar House", "INV-2026-002", "Rs. 8,950", "Credit"));
        sample.add(new RecentInvoiceUiModel("Sweet Oven", "INV-2026-003", "Rs. 5,600", "Cheque"));
        sample.add(new RecentInvoiceUiModel("Royal Cakes", "INV-2026-004", "Rs. 16,220", "Paid"));

        invoiceAdapter.submitList(sample);
    }

    private void setupQuickActions() {
        binding.actionNewInvoice.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateInvoiceActivity.class);
            startActivity(intent);
        });
        binding.actionCustomers.setOnClickListener(v -> showFeatureToast("Customers"));
        binding.actionProducts.setOnClickListener(v -> showFeatureToast("Products"));
        binding.actionSync.setOnClickListener(v -> showFeatureToast("Sync"));

        binding.tvViewAll.setOnClickListener(v -> showFeatureToast("View All Invoices"));
    }

    private void setupBottomNavigation() {
        binding.includeBottomNav.bottomNav.setSelectedItemId(R.id.nav_home);
        binding.includeBottomNav.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            }
            if (id == R.id.nav_invoices) {
                showFeatureToast("Invoices");
                return true;
            }
            if (id == R.id.nav_customers) {
                showFeatureToast("Customers");
                return true;
            }
            if (id == R.id.nav_more) {
                startActivity(new Intent(this, LoginActivity.class));
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



