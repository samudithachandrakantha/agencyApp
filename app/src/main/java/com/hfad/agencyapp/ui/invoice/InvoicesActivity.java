package com.hfad.agencyapp.ui.invoice;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.datepicker.MaterialDatePicker;

import com.hfad.agencyapp.R;
import com.hfad.agencyapp.databinding.ActivityInvoicesBinding;
import com.hfad.agencyapp.ui.adapters.RecentInvoiceAdapter;
import com.hfad.agencyapp.ui.tabs.MainTabsActivity;
import com.hfad.agencyapp.viewmodel.DashboardViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class InvoicesActivity extends AppCompatActivity {

    private ActivityInvoicesBinding binding;
    private RecentInvoiceAdapter adapter;
    private DashboardViewModel viewModel;
    private List<com.hfad.agencyapp.data.entities.Invoice> invoicesCache = new ArrayList<>();
    private String currentQuery = "";
    private long dateRangeStartMillis = 0L;
    private long dateRangeEndMillis = 0L;
    private boolean hasDateRangeFilter = false;
    private final SimpleDateFormat dateRangeDisplayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final SimpleDateFormat invoiceCardDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

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
        binding.toolbar.setNavigationOnClickListener(v -> {
            startActivity(MainTabsActivity.createIntent(this, MainTabsActivity.TAB_HOME));
            finish();
        });

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        adapter = new RecentInvoiceAdapter();
        binding.recyclerAllInvoices.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerAllInvoices.setAdapter(adapter);

        binding.filtersContainer.setVisibility(View.GONE);
        binding.cardDateRangeFilter.setVisibility(View.GONE);
        binding.btnClearDateRangeFilter.setOnClickListener(v -> clearDateRangeFilter());

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
            invoicesCache = invoices == null ? new ArrayList<>() : invoices;
            renderInvoices();
            adapter.setOnInvoiceClickListener(invoiceDbId -> com.hfad.agencyapp.utils.PreviewUtils.showInvoicePreview(InvoicesActivity.this, invoiceDbId));
        });
        
        // Add search text change listener
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s == null ? "" : s.toString();
                renderInvoices();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Add end icon click listener to close search bar
        binding.tilSearch.setEndIconOnClickListener(v -> hideSearchBar());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invoice_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(MainTabsActivity.createIntent(this, MainTabsActivity.TAB_HOME));
            finish();
            return true;
        }
        if (id == R.id.action_search) {
            showSearchBar();
            return true;
        }
        if (id == R.id.action_date_filter) {
            showDateRangePicker();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSearchBar() {
        binding.filtersContainer.setVisibility(View.VISIBLE);
        binding.tilSearch.setVisibility(View.VISIBLE);
        binding.etSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideSearchBar() {
        binding.tilSearch.setVisibility(View.GONE);
        binding.etSearch.setText("");
        currentQuery = "";
        if (!hasDateRangeFilter) {
            binding.filtersContainer.setVisibility(View.GONE);
        }
        renderInvoices();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);
        }
    }

    private void showDateRangePicker() {
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select invoice date range")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null || selection.first == null || selection.second == null) return;
            dateRangeStartMillis = startOfDay(selection.first);
            dateRangeEndMillis = endOfDay(selection.second);
            hasDateRangeFilter = true;
            updateDateRangeFilterBar();
            renderInvoices();
        });

        picker.show(getSupportFragmentManager(), "invoice_date_range_picker");
    }

    private void clearDateRangeFilter() {
        hasDateRangeFilter = false;
        dateRangeStartMillis = 0L;
        dateRangeEndMillis = 0L;
        binding.cardDateRangeFilter.setVisibility(View.GONE);
        if (binding.tilSearch.getVisibility() != View.VISIBLE) {
            binding.filtersContainer.setVisibility(View.GONE);
        }
        renderInvoices();
    }

    private void updateDateRangeFilterBar() {
        if (!hasDateRangeFilter) {
            binding.cardDateRangeFilter.setVisibility(View.GONE);
            if (binding.tilSearch.getVisibility() != View.VISIBLE) {
                binding.filtersContainer.setVisibility(View.GONE);
            }
            return;
        }
        String label = dateRangeDisplayFormat.format(new Date(dateRangeStartMillis)) + " - " + dateRangeDisplayFormat.format(new Date(dateRangeEndMillis));
        binding.tvDateRangeFilter.setText(label);
        binding.filtersContainer.setVisibility(View.VISIBLE);
        binding.cardDateRangeFilter.setVisibility(View.VISIBLE);
    }

    private void renderInvoices() {
        java.util.List<com.hfad.agencyapp.ui.models.RecentInvoiceUiModel> ui = new ArrayList<>();
        List<com.hfad.agencyapp.data.entities.Invoice> filtered = filterInvoices(invoicesCache, currentQuery, hasDateRangeFilter, dateRangeStartMillis, dateRangeEndMillis);
        if (!filtered.isEmpty()) {
            java.text.DecimalFormat fmt = new java.text.DecimalFormat("#,##0.00");
            java.text.SimpleDateFormat chequeDisplayFormat = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault());
            for (com.hfad.agencyapp.data.entities.Invoice inv : filtered) {
                String customer = inv.customerName != null && !inv.customerName.isEmpty() ? inv.customerName : "Unknown";
                String invoiceNumber = inv.invoiceNumber != null && !inv.invoiceNumber.isEmpty() ? inv.invoiceNumber : "-";
                String invoiceDate = inv.createdAt > 0 ? invoiceCardDateFormat.format(new Date(inv.createdAt)) : "-";
                String invoiceCardSubtitle = invoiceNumber + " | " + invoiceDate;
                String status = "";
                boolean isPending = false;
                String dueAmount = "0.00";
                String chequeDate = "";
                
                if (inv.status != null && inv.status.equals("CANCELLED")) {
                    status = "Cancelled";
                } else if ((inv.status != null && (inv.status.equals("COMPLETED") || inv.status.equals("PAID")))
                        || inv.paidAmount >= inv.totalAmount) {
                    status = "Paid";
                } else if (inv.paymentMethod != null && inv.paymentMethod.equals("CASH")) {
                    status = "Cash";
                } else if (inv.paymentMethod != null && inv.paymentMethod.equals("CHEQUE")) {
                    status = "Pending";
                    isPending = true;
                    // For cheque, show cheque date instead of due amount
                    if (inv.chequeDate > 0) {
                        chequeDate = chequeDisplayFormat.format(new Date(inv.chequeDate));
                    }
                } else if (inv.paymentMethod != null && inv.paymentMethod.equals("CREDIT")) {
                    status = "Pending";
                    isPending = true;
                    double due = inv.totalAmount - inv.paidAmount;
                    dueAmount = fmt.format(Math.max(0, due));
                } else if (inv.paidAmount > 0) {
                    status = "Partial";
                    isPending = true;
                    double due = inv.totalAmount - inv.paidAmount;
                    dueAmount = fmt.format(Math.max(0, due));
                }
                ui.add(new com.hfad.agencyapp.ui.models.RecentInvoiceUiModel(customer, invoiceCardSubtitle, inv.id, "Rs. " + fmt.format(inv.totalAmount), status, dueAmount, isPending, chequeDate));
            }
        }
        adapter.submitList(ui);
    }

    private List<com.hfad.agencyapp.data.entities.Invoice> filterInvoices(List<com.hfad.agencyapp.data.entities.Invoice> source,
                                                                         String q,
                                                                         boolean useDateRange,
                                                                         long startMillis,
                                                                         long endMillis) {
        if (source == null) return new ArrayList<>();
        String qq = q == null ? "" : q.trim().toLowerCase(Locale.US);
        List<com.hfad.agencyapp.data.entities.Invoice> out = new ArrayList<>();
        for (com.hfad.agencyapp.data.entities.Invoice inv : source) {
            if (inv == null) continue;
            if (useDateRange && (inv.createdAt < startMillis || inv.createdAt > endMillis)) continue;
            String customer = inv.customerName == null ? "" : inv.customerName.toLowerCase(Locale.US);
            String number = inv.invoiceNumber == null ? "" : inv.invoiceNumber.toLowerCase(Locale.US);
            if (qq.isEmpty() || customer.contains(qq) || number.contains(qq)) {
                out.add(inv);
            }
        }
        return out;
    }

    private long startOfDay(long timeInMillis) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        calendar.setTimeInMillis(timeInMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long endOfDay(long timeInMillis) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        calendar.setTimeInMillis(timeInMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
}
