package com.hfad.agencyapp.ui.invoice;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.datepicker.MaterialDatePicker;

import com.hfad.agencyapp.databinding.ActivityInvoicesBinding;
import com.hfad.agencyapp.ui.adapters.RecentInvoiceAdapter;
import com.hfad.agencyapp.viewmodel.DashboardViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class InvoicesFragment extends Fragment {

    private ActivityInvoicesBinding binding;
    private RecentInvoiceAdapter adapter;
    private DashboardViewModel viewModel;
    private List<com.hfad.agencyapp.data.entities.Invoice> invoicesCache = new ArrayList<>();
    private String currentQuery = "";
    private long dateRangeStartMillis = 0L;
    private long dateRangeEndMillis = 0L;
    private boolean hasDateRangeFilter = false;
    private final SimpleDateFormat dateRangeDisplayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public static InvoicesFragment newInstance() {
        return new InvoicesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityInvoicesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        if (binding.includeBottomNav != null) {
            binding.includeBottomNav.getRoot().setVisibility(View.GONE);
        }

        if (binding.toolbar != null) {
            binding.toolbar.setNavigationOnClickListener(v -> {
                if (getActivity() instanceof com.hfad.agencyapp.ui.tabs.MainTabsActivity) {
                    ((com.hfad.agencyapp.ui.tabs.MainTabsActivity) getActivity()).switchToTab(com.hfad.agencyapp.ui.tabs.MainTabsActivity.TAB_HOME);
                } else {
                    startActivity(com.hfad.agencyapp.ui.tabs.MainTabsActivity.createIntent(requireContext(), com.hfad.agencyapp.ui.tabs.MainTabsActivity.TAB_HOME));
                    requireActivity().finish();
                }
            });
            binding.toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == com.hfad.agencyapp.R.id.action_search) {
                    showSearchBar();
                    return true;
                }
                if (item.getItemId() == com.hfad.agencyapp.R.id.action_date_filter) {
                    showDateRangePicker();
                    return true;
                }
                return false;
            });
            try { binding.toolbar.setTitleTextColor(requireContext().getColor(com.hfad.agencyapp.R.color.white)); } catch (Exception ignored) {}
            binding.toolbar.setTitle("Invoices");
        }

        adapter = new RecentInvoiceAdapter();
        binding.recyclerAllInvoices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerAllInvoices.setAdapter(adapter);

        binding.filtersContainer.setVisibility(View.GONE);
        binding.cardDateRangeFilter.setVisibility(View.GONE);
        binding.btnClearDateRangeFilter.setOnClickListener(v -> clearDateRangeFilter());

        setupSearch();
        binding.tilSearch.setEndIconOnClickListener(v -> hideSearchBar());

        viewModel.invoices.observe(getViewLifecycleOwner(), invoices -> {
            invoicesCache = invoices == null ? new ArrayList<>() : invoices;
            renderInvoices();
            adapter.setOnInvoiceClickListener(invoiceDbId -> com.hfad.agencyapp.utils.PreviewUtils.showInvoicePreview(requireContext(), invoiceDbId));
        });
    }

    private void setupSearch() {
        if (binding.etSearch == null) return;
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s == null ? "" : s.toString();
                renderInvoices();
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void showSearchBar() {
        binding.filtersContainer.setVisibility(View.VISIBLE);
        binding.tilSearch.setVisibility(View.VISIBLE);
        binding.etSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
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
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
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

        picker.show(getParentFragmentManager(), "invoice_date_range_picker");
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
        List<com.hfad.agencyapp.data.entities.Invoice> toShow = filterInvoices(invoicesCache, currentQuery, hasDateRangeFilter, dateRangeStartMillis, dateRangeEndMillis);
        if (toShow != null && !toShow.isEmpty()) {
            java.text.DecimalFormat fmt = new java.text.DecimalFormat("#,##0.00");
            for (com.hfad.agencyapp.data.entities.Invoice inv : toShow) {
                String customer = inv.customerName != null && !inv.customerName.isEmpty() ? inv.customerName : "Unknown";
                String status = "Pending";
                if (inv.paidAmount >= inv.totalAmount) status = "Paid";
                ui.add(new com.hfad.agencyapp.ui.models.RecentInvoiceUiModel(customer, inv.invoiceNumber, inv.id, "Rs. " + fmt.format(inv.totalAmount), status));
            }
        }
        adapter.submitList(ui);
    }

    private List<com.hfad.agencyapp.data.entities.Invoice> filterInvoices(List<com.hfad.agencyapp.data.entities.Invoice> source,
                                                                        String q,
                                                                        boolean useDateRange,
                                                                        long startMillis,
                                                                        long endMillis) {
        if (q == null) q = "";
        String qq = q.trim().toLowerCase();
        List<com.hfad.agencyapp.data.entities.Invoice> out = new ArrayList<>();
        if (source == null) return out;
        for (com.hfad.agencyapp.data.entities.Invoice inv : source) {
            if (inv == null) continue;
            if (useDateRange && (inv.createdAt < startMillis || inv.createdAt > endMillis)) continue;
            String customer = inv.customerName == null ? "" : inv.customerName.toLowerCase();
            String num = inv.invoiceNumber == null ? "" : inv.invoiceNumber.toLowerCase();
            if (qq.isEmpty() || customer.contains(qq) || num.contains(qq)) out.add(inv);
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
