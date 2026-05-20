package com.hfad.agencyapp.ui.invoice;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.snackbar.Snackbar;

import com.hfad.agencyapp.R;
import com.hfad.agencyapp.data.Repository;
import com.hfad.agencyapp.data.entities.Invoice;
import com.hfad.agencyapp.data.entities.InvoiceItem;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.databinding.ActivityInvoicePreviewBinding;
import com.hfad.agencyapp.ui.invoice.CreateInvoiceActivity;
import com.hfad.agencyapp.ui.adapters.InvoicePreviewItemAdapter;
import com.hfad.agencyapp.ui.models.InvoicePreviewLineItem;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InvoicePreviewActivity extends AppCompatActivity {

    public static final String EXTRA_INVOICE_ID = "extra_invoice_id";

    private ActivityInvoicePreviewBinding binding;
    private InvoicePreviewItemAdapter adapter;
    private Repository repository;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    private long invoiceId = -1L;
    private Invoice invoice;
    private List<InvoiceItem> invoiceItems = new ArrayList<>();
    private final Map<Long, String> productNames = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInvoicePreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        invoiceId = getIntent().getLongExtra(EXTRA_INVOICE_ID, -1L);
        if (invoiceId <= 0L) {
            finish();
            return;
        }

        repository = Repository.getInstance(this);

        setupToolbar();
        setupRecyclerView();
        setupObservers();
        setupActions();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(null);
            getSupportActionBar().setTitle(getString(R.string.invoice_preview_title));
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupActions() {
        binding.btnEditInvoice.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateInvoiceActivity.class);
            intent.putExtra(CreateInvoiceActivity.EXTRA_INVOICE_ID, invoiceId);
            startActivity(intent);
            finish();
        });

        binding.btnDeleteInvoice.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.delete_invoice_title)
                .setMessage(R.string.delete_invoice_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    repository.deleteInvoiceCascade(invoiceId);
                    Snackbar.make(binding.getRoot(), R.string.invoice_deleted, Snackbar.LENGTH_SHORT).show();
                    finish();
                })
                .show());
    }

    private void setupRecyclerView() {
        adapter = new InvoicePreviewItemAdapter();
        binding.recyclerInvoiceItems.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerInvoiceItems.setAdapter(adapter);
    }

    private void setupObservers() {
        LiveData<Invoice> invoiceLiveData = repository.getInvoiceById(invoiceId);
        invoiceLiveData.observe(this, value -> {
            invoice = value;
            renderPreview();
        });

        repository.getInvoiceItems(invoiceId).observe(this, value -> {
            invoiceItems = value != null ? value : new ArrayList<>();
            renderPreview();
        });

        repository.getAllProducts().observe(this, value -> {
            productNames.clear();
            if (value != null) {
                for (Product product : value) {
                    productNames.put(product.id, product.name != null && !product.name.isEmpty() ? product.name : getString(R.string.unknown_value));
                }
            }
            renderPreview();
        });
    }

    private void renderPreview() {
        if (invoice == null) {
            binding.contentRoot.setVisibility(View.GONE);
            binding.loadingText.setVisibility(View.VISIBLE);
            return;
        }

        binding.loadingText.setVisibility(View.GONE);
        binding.contentRoot.setVisibility(View.VISIBLE);

        String customerName = invoice.customerName != null && !invoice.customerName.isEmpty()
                ? invoice.customerName
                : getString(R.string.unknown_value);
        binding.tvCustomerName.setText(customerName);
        binding.tvInvoiceNumber.setText(invoice.invoiceNumber != null ? invoice.invoiceNumber : getString(R.string.unknown_value));
        binding.tvInvoiceDate.setText(dateFormat.format(invoice.createdAt > 0 ? invoice.createdAt : System.currentTimeMillis()));
        binding.tvPaymentMethod.setText(invoice.paymentMethod != null && !invoice.paymentMethod.isEmpty() ? invoice.paymentMethod : getString(R.string.unknown_value));
        binding.tvStatus.setText(invoice.status != null && !invoice.status.isEmpty() ? invoice.status : getString(R.string.unknown_value));
        binding.tvTotalAmount.setText(getString(R.string.amount_format, currencyFormat.format(invoice.totalAmount)));
        binding.tvPaidAmount.setText(getString(R.string.amount_format, currencyFormat.format(invoice.paidAmount)));

        double balance = Math.max(0.0, invoice.totalAmount - invoice.paidAmount);
        binding.tvBalanceDue.setText(getString(R.string.amount_format, currencyFormat.format(balance)));

        double subtotal = 0.0;
        List<InvoicePreviewLineItem> previewItems = new ArrayList<>();
        for (InvoiceItem item : invoiceItems) {
            double lineTotal = item.totalPrice > 0 ? item.totalPrice : item.quantity * item.unitPrice;
            subtotal += lineTotal;
            String productName = productNames.containsKey(item.productId)
                    ? productNames.get(item.productId)
                    : getString(R.string.unknown_value);
            previewItems.add(new InvoicePreviewLineItem(
                    productName,
                    "#" + item.productId,
                    item.quantity,
                    item.unitPrice,
                    lineTotal
            ));
        }
        adapter.submitList(previewItems);
        binding.tvItemCount.setText(getString(R.string.invoice_items_count, previewItems.size()));
        binding.tvSubtotal.setText(getString(R.string.amount_format, currencyFormat.format(subtotal)));

        if (invoice.note != null && !invoice.note.trim().isEmpty()) {
            binding.noteCard.setVisibility(View.VISIBLE);
            binding.tvNote.setText(invoice.note);
        } else {
            binding.noteCard.setVisibility(View.GONE);
        }

        updateStatusStyling(invoice.status);
    }

    private void updateStatusStyling(String status) {
        String normalized = status != null ? status.trim().toUpperCase(Locale.US) : "";
        if ("COMPLETED".equals(normalized) || "PAID".equals(normalized)) {
            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_paid);
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.badge_paid_text));
        } else if ("CANCELLED".equals(normalized)) {
            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_cheque);
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.badge_cheque_text));
        } else {
            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_credit);
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.badge_credit_text));
        }
    }
}