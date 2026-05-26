package com.hfad.agencyapp.ui.tabs;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import com.hfad.agencyapp.R;
import com.hfad.agencyapp.databinding.ActivityMainTabsBinding;
import com.hfad.agencyapp.ui.customers.CustomersFragment;
import com.hfad.agencyapp.ui.dashboard.HomeFragment;
import com.hfad.agencyapp.ui.insights.InsightsFragment;
import com.hfad.agencyapp.ui.invoice.InvoicesFragment;

public class MainTabsActivity extends AppCompatActivity {

    public static final String EXTRA_TAB = "extra_tab";
    public static final String TAB_HOME = "home";
    public static final String TAB_INVOICES = "invoices";
    public static final String TAB_CUSTOMERS = "customers";
    public static final String TAB_INSIGHTS = "insights";

    private ActivityMainTabsBinding binding;
    private String currentTab = TAB_HOME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainTabsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentTab = getIntent().getStringExtra(EXTRA_TAB);
        if (currentTab == null || currentTab.trim().isEmpty()) {
            currentTab = TAB_HOME;
        }

        binding.includeBottomNav.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchTab(TAB_HOME, HomeFragment.newInstance());
                return true;
            }
            if (id == R.id.nav_invoices) {
                switchTab(TAB_INVOICES, InvoicesFragment.newInstance());
                return true;
            }
            if (id == R.id.nav_customers) {
                switchTab(TAB_CUSTOMERS, CustomersFragment.newInstance());
                return true;
            }
            if (id == R.id.nav_insights) {
                switchTab(TAB_INSIGHTS, InsightsFragment.newInstance());
                return true;
            }
            return false;
        });

        if (TAB_INVOICES.equals(currentTab)) {
            binding.includeBottomNav.bottomNav.setSelectedItemId(R.id.nav_invoices);
            showFragment(InvoicesFragment.newInstance());
        } else if (TAB_CUSTOMERS.equals(currentTab)) {
            binding.includeBottomNav.bottomNav.setSelectedItemId(R.id.nav_customers);
            showFragment(CustomersFragment.newInstance());
        } else if (TAB_INSIGHTS.equals(currentTab)) {
            binding.includeBottomNav.bottomNav.setSelectedItemId(R.id.nav_insights);
            showFragment(InsightsFragment.newInstance());
        } else {
            binding.includeBottomNav.bottomNav.setSelectedItemId(R.id.nav_home);
            showFragment(HomeFragment.newInstance());
        }
    }

    private void switchTab(String tab, androidx.fragment.app.Fragment fragment) {
        currentTab = tab;
        showFragment(fragment);
    }

    private void showFragment(androidx.fragment.app.Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void switchToTab(String tab) {
        if (TAB_INVOICES.equals(tab)) {
            binding.includeBottomNav.bottomNav.setSelectedItemId(R.id.nav_invoices);
            showFragment(InvoicesFragment.newInstance());
        } else if (TAB_CUSTOMERS.equals(tab)) {
            binding.includeBottomNav.bottomNav.setSelectedItemId(R.id.nav_customers);
            showFragment(CustomersFragment.newInstance());
        } else if (TAB_INSIGHTS.equals(tab)) {
            binding.includeBottomNav.bottomNav.setSelectedItemId(R.id.nav_insights);
            showFragment(InsightsFragment.newInstance());
        } else {
            binding.includeBottomNav.bottomNav.setSelectedItemId(R.id.nav_home);
            showFragment(HomeFragment.newInstance());
        }
    }

    public static Intent createIntent(android.content.Context context, String tab) {
        Intent intent = new Intent(context, MainTabsActivity.class);
        intent.putExtra(EXTRA_TAB, tab);
        return intent;
    }
}
