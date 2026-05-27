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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private ActivityDashboardBinding binding;
    private RecentInvoiceAdapter invoiceAdapter;
    private DashboardViewModel viewModel;
    private final SimpleDateFormat invoiceCardDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

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

        binding.includeBottomNav.getRoot().setVisibility(View.GONE);

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
            invoiceAdapter.setOnInvoiceClickListener(invoiceDbId -> com.hfad.agencyapp.utils.PreviewUtils.showInvoicePreview(requireContext(), invoiceDbId));
        });
    }

    private void setupQuickActions() {
        binding.cardTodaySales.setOnClickListener(v -> {
            if (getActivity() instanceof MainTabsActivity) {
                ((MainTabsActivity) getActivity()).switchToTab(MainTabsActivity.TAB_INSIGHTS);
            } else {
                startActivity(MainTabsActivity.createIntent(requireContext(), MainTabsActivity.TAB_INSIGHTS));
            }
        });

        binding.actionNewInvoice.setOnClickListener(v -> startActivity(new Intent(requireContext(), CreateInvoiceActivity.class)));
        // Customers quick-action removed
        binding.actionProducts.setOnClickListener(v -> startActivity(new Intent(requireContext(), ProductsActivity.class)));
        binding.actionSync.setOnClickListener(v -> android.widget.Toast.makeText(requireContext(), "Sync coming soon", android.widget.Toast.LENGTH_SHORT).show());
        binding.tvViewAll.setOnClickListener(v -> startActivity(MainTabsActivity.createIntent(requireContext(), MainTabsActivity.TAB_INVOICES)));
    }

    private void setupProfileEntry() {
        binding.tvAvatar.setOnClickListener(v -> startActivity(new Intent(requireContext(), ProfileActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        // update avatar badge from prefs
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("cheque_prefs", android.content.Context.MODE_PRIVATE);
        int count = prefs.getInt(com.hfad.agencyapp.workers.ChequeNotificationWorker.KEY_COUNT, 0);
        if (count > 0) {
            binding.tvAvatarBadge.setVisibility(View.VISIBLE);
            binding.tvAvatarBadge.setText(String.valueOf(count));
        } else {
            binding.tvAvatarBadge.setVisibility(View.GONE);
        }
    }
}
