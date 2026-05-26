package com.hfad.agencyapp.ui.insights;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.hfad.agencyapp.R;
import com.hfad.agencyapp.databinding.ActivityInsightsBinding;
import com.hfad.agencyapp.ui.customers.CustomersActivity;
import com.hfad.agencyapp.ui.invoice.InvoicesActivity;
import com.hfad.agencyapp.ui.tabs.MainTabsActivity;
import com.hfad.agencyapp.viewmodel.DashboardViewModel;

public class InsightsActivity extends AppCompatActivity {

    private ActivityInsightsBinding binding;
    private DashboardViewModel viewModel;
    private InsightsDashboardBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInsightsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Insights");
        }
        binding.toolbar.setNavigationOnClickListener(v -> {
            startActivity(MainTabsActivity.createIntent(this, MainTabsActivity.TAB_HOME));
            finish();
        });

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binder = new InsightsDashboardBinder(binding, viewModel, this, this);
        binder.attach();

        binding.includeBottomNav.bottomNav.setSelectedItemId(R.id.nav_insights);
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
                return true;
            }
            return false;
        });
    }
}
