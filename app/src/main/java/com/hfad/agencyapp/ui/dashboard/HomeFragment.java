package com.hfad.agencyapp.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hfad.agencyapp.R;
import com.hfad.agencyapp.databinding.ActivityDashboardBinding;
import com.hfad.agencyapp.ui.adapters.RecentInvoiceAdapter;
import com.hfad.agencyapp.ui.invoice.CreateInvoiceActivity;
import com.hfad.agencyapp.ui.products.ProductsActivity;
import com.hfad.agencyapp.ui.profile.ProfileActivity;
import com.hfad.agencyapp.ui.tabs.MainTabsActivity;
import com.hfad.agencyapp.viewmodel.DashboardViewModel;

public class HomeFragment extends Fragment {

    private ActivityDashboardBinding binding;
    private RecentInvoiceAdapter invoiceAdapter;
    private DashboardViewModel viewModel;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        if (binding.includeBottomNav != null) {
            binding.includeBottomNav.getRoot().setVisibility(View.GONE);
        }

        setupRecyclerView();
        setupObservers();
        setupQuickActions();
        setupProfileEntry();
    }

    private void setupRecyclerView() {
        binding.recyclerRecentInvoices.setLayoutManager(new LinearLayoutManager(requireContext()));
        invoiceAdapter = new RecentInvoiceAdapter();
        binding.recyclerRecentInvoices.setAdapter(invoiceAdapter);
    }

    private void setupObservers() {
        viewModel.todaySales.observe(getViewLifecycleOwner(), sales -> {
            double value = sales != null ? sales : 0.0;
            binding.tvTodaySales.setText(getString(R.string.amount_format, new java.text.DecimalFormat("#,##0.00").format(value)));
        });

        viewModel.todayInvoiceCount.observe(getViewLifecycleOwner(), count -> {
            int value = count != null ? count : 0;
            binding.tvInvoiceCount.setText(String.valueOf(value));
        });

        viewModel.invoices.observe(getViewLifecycleOwner(), invoices -> {
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
            invoiceAdapter.setOnInvoiceClickListener(invoiceDbId -> com.hfad.agencyapp.utils.PreviewUtils.showInvoicePreview(requireContext(), invoiceDbId));
        });
    }

    private void setupQuickActions() {
        binding.actionNewInvoice.setOnClickListener(v -> startActivity(new Intent(requireContext(), CreateInvoiceActivity.class)));
        // Customers quick-action removed
        binding.actionProducts.setOnClickListener(v -> startActivity(new Intent(requireContext(), ProductsActivity.class)));
        binding.actionSync.setOnClickListener(v -> android.widget.Toast.makeText(requireContext(), "Sync coming soon", android.widget.Toast.LENGTH_SHORT).show());
        binding.tvViewAll.setOnClickListener(v -> startActivity(MainTabsActivity.createIntent(requireContext(), MainTabsActivity.TAB_INVOICES)));
    }

    private void setupProfileEntry() {
        binding.tvAvatar.setOnClickListener(v -> startActivity(new Intent(requireContext(), ProfileActivity.class)));
    }
}
