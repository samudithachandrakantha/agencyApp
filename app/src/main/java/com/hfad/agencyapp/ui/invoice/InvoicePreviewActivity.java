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
import com.hfad.agencyapp.data.entities.Payment;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.databinding.ActivityInvoicePreviewBinding;
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

        binding.btnSettleInvoice.setOnClickListener(v -> showSettleDialog());

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

    private void showSettleDialog() {
        if (invoice == null) {
            return;
        }

        double balanceDue = calculateBalanceDue(invoice);
        if (balanceDue <= 0.0) {
            Snackbar.make(binding.getRoot(), R.string.settle_amount_exceeds_due, Snackbar.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_settle_invoice, null);
        androidx.appcompat.widget.AppCompatCheckBox cbFullPayment = dialogView.findViewById(R.id.cb_full_payment);
        com.google.android.material.textfield.TextInputLayout tilAmount = dialogView.findViewById(R.id.til_settle_amount);
        com.google.android.material.textfield.TextInputEditText etAmount = dialogView.findViewById(R.id.et_settle_amount);

        String fullAmount = currencyFormat.format(balanceDue);
        cbFullPayment.setChecked(true);
        etAmount.setText(fullAmount);
        etAmount.setEnabled(false);
        etAmount.setFocusable(false);

        cbFullPayment.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etAmount.setText(currencyFormat.format(balanceDue));
                etAmount.setEnabled(false);
                etAmount.setFocusable(false);
            } else {
                etAmount.setEnabled(true);
                etAmount.setFocusableInTouchMode(true);
                etAmount.setFocusable(true);
                etAmount.requestFocus();
            }
            tilAmount.setError(null);
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.settle_invoice_title)
                .setView(dialogView)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.mark_paid, null)
                .create();

        dialog.setOnShowListener(d -> {
            android.widget.Button paidButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            paidButton.setOnClickListener(v -> {
                tilAmount.setError(null);
                double payAmount;
                if (cbFullPayment.isChecked()) {
                    payAmount = balanceDue;
                } else {
                    String raw = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
                    try {
                        payAmount = Double.parseDouble(raw);
                    } catch (NumberFormatException ex) {
                        tilAmount.setError(getString(R.string.settle_invalid_amount));
                        return;
                    }
                }

                if (payAmount <= 0.0) {
                    tilAmount.setError(getString(R.string.settle_invalid_amount));
                    return;
                }
                if (payAmount > balanceDue) {
                    tilAmount.setError(getString(R.string.settle_amount_exceeds_due));
                    return;
                }

                applySettlement(payAmount);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void applySettlement(double payAmount) {
        if (invoice == null) {
            return;
        }

        double effectivePaidAmount = calculateEffectivePaidAmount(invoice);
        double updatedPaidAmount = Math.min(invoice.totalAmount, effectivePaidAmount + payAmount);
        invoice.paidAmount = updatedPaidAmount;
        invoice.status = updatedPaidAmount >= invoice.totalAmount ? "COMPLETED" : "PENDING";

        repository.updateInvoice(invoice);
        repository.insertPayment(new Payment(invoice.id, payAmount, System.currentTimeMillis(), "CASH"));

        renderPreview();
        Snackbar.make(binding.getRoot(), R.string.settlement_saved, Snackbar.LENGTH_SHORT).show();
    }

    private double calculateEffectivePaidAmount(Invoice sourceInvoice) {
        if (sourceInvoice == null) {
            return 0.0;
        }
        boolean isCashPayment = "CASH".equalsIgnoreCase(sourceInvoice.paymentMethod);
        return isCashPayment ? sourceInvoice.totalAmount : sourceInvoice.paidAmount;
    }

    private double calculateBalanceDue(Invoice sourceInvoice) {
        if (sourceInvoice == null) {
            return 0.0;
        }
        return Math.max(0.0, sourceInvoice.totalAmount - calculateEffectivePaidAmount(sourceInvoice));
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
        boolean isCashPayment = "CASH".equalsIgnoreCase(invoice.paymentMethod);
        String statusText = invoice.status != null && !invoice.status.isEmpty() ? invoice.status : getString(R.string.unknown_value);
        if (isCashPayment && !"COMPLETED".equalsIgnoreCase(statusText) && !"PAID".equalsIgnoreCase(statusText)) {
            statusText = "COMPLETED";
        }
        binding.tvStatus.setText(statusText);
        binding.tvTotalAmount.setText(getString(R.string.amount_format, currencyFormat.format(invoice.totalAmount)));
        double effectivePaidAmount = calculateEffectivePaidAmount(invoice);
        binding.tvPaidAmount.setText(getString(R.string.amount_format, currencyFormat.format(effectivePaidAmount)));

        double balance = calculateBalanceDue(invoice);
        binding.tvBalanceDue.setText(getString(R.string.amount_format, currencyFormat.format(balance)));

        boolean showSettle = "CREDIT".equalsIgnoreCase(invoice.paymentMethod) && balance > 0.0;
        binding.btnSettleInvoice.setVisibility(showSettle ? View.VISIBLE : View.GONE);

        double subtotal = 0.0;
        double discount = 0.0;
        List<InvoicePreviewLineItem> previewItems = new ArrayList<>();
        for (InvoiceItem item : invoiceItems) {
            double lineSubtotal = item.quantity * item.unitPrice;
            double lineTotal = item.totalPrice > 0 ? item.totalPrice : lineSubtotal;
            double lineDiscount = Math.max(0.0, lineSubtotal - lineTotal);
            subtotal += lineSubtotal;
            discount += lineDiscount;
            String productName = productNames.containsKey(item.productId)
                    ? productNames.get(item.productId)
                    : getString(R.string.unknown_value);
                String freeIssueText = "";
                if (item.freeIssueUnits > 0) {
                freeIssueText = "Free issue: +" + item.freeIssueUnits + " (Buy " + item.freeIssueBuyQty + " get " + item.freeIssueBonusQty + " free)";
                }
            previewItems.add(new InvoicePreviewLineItem(
                    productName,
                    "#" + item.productId,
                    item.quantity,
                    item.unitPrice,
                    lineTotal,
                    freeIssueText
            ));
        }
        adapter.submitList(previewItems);
        binding.tvItemCount.setText(getString(R.string.invoice_items_count, previewItems.size()));
        binding.tvSubtotal.setText(getString(R.string.amount_format, currencyFormat.format(subtotal)));
        binding.tvDiscount.setText(getString(R.string.amount_format, currencyFormat.format(discount)));
        binding.tvTotalAmount.setText(getString(R.string.amount_format, currencyFormat.format(Math.max(0.0, subtotal - discount))));

        if (invoice.note != null && !invoice.note.trim().isEmpty()) {
            binding.noteCard.setVisibility(View.VISIBLE);
            binding.tvNote.setText(invoice.note);
        } else {
            binding.noteCard.setVisibility(View.GONE);
        }

        updateStatusStyling(statusText);
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