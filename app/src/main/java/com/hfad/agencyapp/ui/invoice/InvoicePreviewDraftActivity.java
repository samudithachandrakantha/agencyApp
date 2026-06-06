package com.hfad.agencyapp.ui.invoice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hfad.agencyapp.R;
import com.hfad.agencyapp.ui.adapters.InvoicePreviewItemAdapter;
import com.hfad.agencyapp.ui.models.InvoicePreviewLineItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class InvoicePreviewDraftActivity extends AppCompatActivity {

    public static final String EXTRA_CUSTOMER_NAME = "extra_customer_name";
    public static final String EXTRA_CONTACT = "extra_contact";
    public static final String EXTRA_ADDRESS = "extra_address";
    public static final String EXTRA_INVOICE_NUMBER = "extra_invoice_number";
    public static final String EXTRA_INVOICE_DATE = "extra_invoice_date";
    public static final String EXTRA_ITEMS_JSON = "extra_items_json"; // JSON array of items
    public static final String EXTRA_SUBTOTAL = "extra_subtotal";
    public static final String EXTRA_DISCOUNT = "extra_discount";
    public static final String EXTRA_TOTAL = "extra_total";
    public static final String EXTRA_PAID_AMOUNT = "extra_paid_amount";
    public static final String EXTRA_BALANCE_DUE = "extra_balance_due";
    public static final String EXTRA_PAYMENT = "extra_payment";
    public static final String EXTRA_CHEQUE_NUMBER = "extra_cheque_number";
    public static final String EXTRA_BANK_NAME = "extra_bank_name";
    public static final String EXTRA_CHEQUE_DATE = "extra_cheque_date";

    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private InvoicePreviewItemAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_preview_draft);

        adapter = new InvoicePreviewItemAdapter();

        androidx.recyclerview.widget.RecyclerView rvItems = findViewById(R.id.rvPreviewItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);

        bindDataFromIntent(getIntent());

        findViewById(R.id.btnPreviewEdit).setOnClickListener(v -> {
            // User wants to go back and edit - return canceled
            setResult(RESULT_CANCELED);
            finish();
        });

        findViewById(R.id.btnPreviewSave).setOnClickListener(v -> {
            // Return OK to indicate 'Save' selected. CreateInvoiceActivity handles actual saving.
            setResult(RESULT_OK);
            finish();
        });
    }

    private void bindDataFromIntent(Intent intent) {
        if (intent == null) return;

        String customer = intent.getStringExtra(EXTRA_CUSTOMER_NAME);
        String contact = intent.getStringExtra(EXTRA_CONTACT);
        String address = intent.getStringExtra(EXTRA_ADDRESS);
        String invoiceNumber = intent.getStringExtra(EXTRA_INVOICE_NUMBER);
        String invoiceDate = intent.getStringExtra(EXTRA_INVOICE_DATE);

        android.widget.TextView tvCustomer = findViewById(R.id.tvPreviewCustomer);
        android.widget.TextView tvContact = findViewById(R.id.tvPreviewContact);
        android.widget.TextView tvAddress = findViewById(R.id.tvPreviewAddress);
        android.widget.TextView tvInvoiceNumber = findViewById(R.id.tvPreviewInvoiceNumber);
        android.widget.TextView tvInvoiceDate = findViewById(R.id.tvPreviewInvoiceDate);

        tvCustomer.setText(customer != null ? customer : getString(R.string.unknown_value));
        tvContact.setText(contact != null ? contact : "-");
        tvAddress.setText(address != null ? address : "-");
        tvInvoiceNumber.setText(invoiceNumber != null ? invoiceNumber : "");
        tvInvoiceDate.setText(invoiceDate != null ? invoiceDate : "");

        // summary fields
        double subtotal = intent.getDoubleExtra(EXTRA_SUBTOTAL, 0.0);
        double discount = intent.getDoubleExtra(EXTRA_DISCOUNT, 0.0);
        double total = intent.getDoubleExtra(EXTRA_TOTAL, 0.0);
        double paid = intent.getDoubleExtra(EXTRA_PAID_AMOUNT, 0.0);
        double balance = intent.getDoubleExtra(EXTRA_BALANCE_DUE, 0.0);

        android.widget.TextView tvSubtotal = findViewById(R.id.tvPreviewSubtotal);
        android.widget.TextView tvDiscount = findViewById(R.id.tvPreviewDiscount);
        android.widget.TextView tvTotal = findViewById(R.id.tvPreviewTotal);
        android.widget.TextView tvPaid = findViewById(R.id.tvPreviewPaidAmount);
        android.widget.TextView tvBalance = findViewById(R.id.tvPreviewBalanceDue);

        tvSubtotal.setText(getString(R.string.amount_format, currencyFormat.format(subtotal)));
        tvDiscount.setText(getString(R.string.amount_format, currencyFormat.format(discount)));
        tvTotal.setText(getString(R.string.amount_format, currencyFormat.format(total)));
        tvPaid.setText(getString(R.string.invoice_preview_paid_label) + ": " + getString(R.string.amount_format, currencyFormat.format(paid)));
        tvBalance.setText(getString(R.string.amount_format, currencyFormat.format(balance)));

        // payment / cheque details
        String payment = intent.getStringExtra(EXTRA_PAYMENT);
        android.widget.TextView tvPayment = findViewById(R.id.tvPreviewPayment);
        tvPayment.setText(payment != null ? ("Payment: " + payment) : "");

        String chequeNumber = intent.getStringExtra(EXTRA_CHEQUE_NUMBER);
        String bankName = intent.getStringExtra(EXTRA_BANK_NAME);
        String chequeDate = intent.getStringExtra(EXTRA_CHEQUE_DATE);

        android.view.View cardCheque = findViewById(R.id.cardPreviewChequeDetails);
        if (chequeNumber == null && bankName == null && chequeDate == null) {
            cardCheque.setVisibility(View.GONE);
        } else {
            cardCheque.setVisibility(View.VISIBLE);
            android.widget.TextView tvChequeNumber = findViewById(R.id.tvPreviewChequeNumber);
            android.widget.TextView tvBankName = findViewById(R.id.tvPreviewBankName);
            android.widget.TextView tvChequeDate = findViewById(R.id.tvPreviewChequeDate);
            tvChequeNumber.setText(chequeNumber != null ? ("Cheque No: " + chequeNumber) : "Cheque No: -");
            tvBankName.setText(bankName != null ? ("Bank: " + bankName) : "Bank: -");
            tvChequeDate.setText(chequeDate != null ? ("Cheque Date: " + chequeDate) : "Cheque Date: -");
        }

        // items JSON -> list
        String itemsJson = intent.getStringExtra(EXTRA_ITEMS_JSON);
        List<InvoicePreviewLineItem> items = new ArrayList<>();
        if (itemsJson != null) {
            try {
                JSONArray arr = new JSONArray(itemsJson);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    String pname = obj.optString("productName", "");
                    String pcode = obj.optString("productCode", "");
                    int qty = obj.optInt("quantity", 0);
                    double unit = obj.optDouble("unitPrice", 0.0);
                    double lineTotal = obj.optDouble("lineTotal", 0.0);
                    double discountAmount = obj.optDouble("discountAmount", 0.0);
                    String freeIssue = obj.optString("freeIssueText", "");
                    items.add(new InvoicePreviewLineItem(pname, pcode, qty, unit, lineTotal, discountAmount, freeIssue));
                }
            } catch (JSONException ignored) {
            }
        }

        adapter.submitList(items);
        android.widget.TextView tvCount = findViewById(R.id.tvPreviewItemCount);
        tvCount.setText(items.size() + " item" + (items.size() == 1 ? "" : "s"));
    }

}

