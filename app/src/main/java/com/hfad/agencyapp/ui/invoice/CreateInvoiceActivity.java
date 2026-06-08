package com.hfad.agencyapp.ui.invoice;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hfad.agencyapp.R;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.hfad.agencyapp.databinding.ActivityCreateInvoiceBinding;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.ui.adapters.InvoicePreviewItemAdapter;
import com.hfad.agencyapp.ui.adapters.InvoiceItemsAdapter;
import com.hfad.agencyapp.ui.models.ChequeDetails;
import com.hfad.agencyapp.ui.models.Customer;
import com.hfad.agencyapp.ui.models.InvoicePreviewLineItem;
import com.hfad.agencyapp.ui.models.PaymentType;
import com.hfad.agencyapp.viewmodel.CreateInvoiceViewModel;
import com.hfad.agencyapp.utils.Constants;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Activity for creating invoices.
 */
public class CreateInvoiceActivity extends AppCompatActivity {

    public static final String EXTRA_INVOICE_ID = "extra_invoice_id";

    private ActivityCreateInvoiceBinding binding;
    private CreateInvoiceViewModel viewModel;
    private InvoiceItemsAdapter adapter;
    private final InvoicePreviewItemAdapter previewAdapter = new InvoicePreviewItemAdapter();
    private DecimalFormat currencyFormat;
    private SimpleDateFormat dateFormat;
    private long editingInvoiceId = -1L;
    private String editingInvoiceNumber;
    private boolean customerSelectionLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCreateInvoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CreateInvoiceViewModel.class);

        editingInvoiceId = getIntent().getLongExtra(EXTRA_INVOICE_ID, -1L);

        currencyFormat = new DecimalFormat("#,##0.00");
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        ensureDefaultInvoicePreferences();

        setupToolbar();
        setupRecyclerView();
        setupObservers();
        setupClickListeners();
        setupToggleGroup();
        setupTextInputListeners();

        if (editingInvoiceId > 0) {
            loadInvoiceForEdit(editingInvoiceId);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9876 && resultCode == RESULT_OK) {
            // User tapped Save in the full-screen preview
            viewModel.saveInvoice(editingInvoiceId > 0 ? editingInvoiceId : null, editingInvoiceNumber);
            Snackbar.make(binding.getRoot(), getString(R.string.invoice_saved_successfully), Snackbar.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Set navigation icon color to white
        android.graphics.drawable.Drawable navIcon = binding.toolbar.getNavigationIcon();
        if (navIcon != null) {
            DrawableCompat.setTint(navIcon, Color.WHITE);
        }
        
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_invoice, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_out_of_stock_behavior) {
            showOutOfStockBehaviorDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        adapter = new InvoiceItemsAdapter(new InvoiceItemsAdapter.OnItemActionListener() {
            @Override
            public void onQuantityChange(int position, int newQuantity) {
                viewModel.updateQuantity(position, newQuantity);
            }

            @Override
            public void onRemove(int position) {
                viewModel.removeItem(position);
            }
        });

        binding.rvInvoiceItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvInvoiceItems.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getItems().observe(this, items -> {
            adapter.submitList(items);
            binding.tvNoItems.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getSubtotal().observe(this, subtotal ->
                binding.tvSubtotal.setText(getString(R.string.amount_format, currencyFormat.format(subtotal))));

        viewModel.getDiscount().observe(this, discount ->
                binding.tvDiscount.setText(getString(R.string.amount_format, currencyFormat.format(discount))));

        viewModel.getTotal().observe(this, total ->
                binding.tvTotal.setText(getString(R.string.amount_format, currencyFormat.format(total))));

        viewModel.getSelectedCustomer().observe(this, customer -> {
            if (customer != null) {
                binding.tvCustomerName.setText(customer.getBusinessName());
                binding.tvCustomerError.setVisibility(View.GONE);
                applyAllowedPaymentMethods(customer);
            } else {
                applyAllowedPaymentMethods(null);
            }
        });

        viewModel.getPaymentType().observe(this, paymentType -> {
            boolean isCheque = paymentType == PaymentType.CHEQUE;
            binding.containerChequeDetails.setVisibility(isCheque ? View.VISIBLE : View.GONE);
        });
    }

    private void setupClickListeners() {
        binding.customerRow.setOnClickListener(v -> {
            if (!customerSelectionLocked) {
                openCustomerPicker();
            }
        });
        binding.btnAddItem.setOnClickListener(v -> openProductPicker());
        binding.etChequeDate.setOnClickListener(v -> openDatePicker());
        binding.tilChequeDate.setEndIconOnClickListener(v -> openDatePicker());
        binding.btnSaveInvoice.setOnClickListener(v -> showPreview());
    }

    private void setCustomerSelectionLocked(boolean locked) {
        customerSelectionLocked = locked;
        binding.customerRow.setEnabled(!locked);
        binding.customerRow.setClickable(!locked);
        binding.customerRow.setFocusable(!locked);
        binding.customerRow.setAlpha(locked ? 0.75f : 1.0f);
        binding.ivChevron.setVisibility(locked ? View.GONE : View.VISIBLE);
    }

    private void showPreview() {
        String error = viewModel.validateInvoice();

        if (!error.isEmpty()) {
            if (error.toLowerCase().contains("customer")) {
                binding.tvCustomerError.setVisibility(View.VISIBLE);
            } else if (error.toLowerCase().contains("item")) {
                binding.tvItemsError.setVisibility(View.VISIBLE);
            } else if (error.toLowerCase().contains("cheque")) {
                binding.tvChequeError.setVisibility(View.VISIBLE);
            }
            Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            return;
        }

        binding.tvCustomerError.setVisibility(View.GONE);
        binding.tvItemsError.setVisibility(View.GONE);
        binding.tvChequeError.setVisibility(View.GONE);

        // Build preview list & launch full-screen preview activity instead of showing a dialog
        List<com.hfad.agencyapp.ui.models.InvoiceItem> items = viewModel.getItems().getValue();
        List<InvoicePreviewLineItem> previewItems = new java.util.ArrayList<>();
        double subtotal = 0.0;
        double totalDiscount = 0.0;
        if (items != null) {
            for (com.hfad.agencyapp.ui.models.InvoiceItem it : items) {
                double lineSubtotal = it.getQuantity() * it.getUnitPrice();
                double lineDiscount = it.getDiscount();
                double lineTotal = it.getLineTotal();
                subtotal += lineSubtotal;
                totalDiscount += lineDiscount;
                previewItems.add(new InvoicePreviewLineItem(
                        it.getProductName(),
                        "#" + it.getProductId(),
                        it.getQuantity(),
                        it.getUnitPrice(),
                        lineTotal,
                        lineDiscount,
                        it.getFreeIssueSummary()
                ));
            }
        }

        org.json.JSONArray arr = new org.json.JSONArray();
        for (InvoicePreviewLineItem li : previewItems) {
            org.json.JSONObject o = new org.json.JSONObject();
            try {
                o.put("productName", li.productName);
                o.put("productCode", li.productCode);
                o.put("quantity", li.quantity);
                o.put("unitPrice", li.unitPrice);
                o.put("lineTotal", li.lineTotal);
                o.put("discountAmount", li.discountAmount);
                o.put("freeIssueText", li.freeIssueText != null ? li.freeIssueText : "");
            } catch (org.json.JSONException ignored) {
            }
            arr.put(o);
        }

        Double total = viewModel.getTotal().getValue();
        double invoiceTotal = total != null ? total : 0.0;
        PaymentType paymentType = viewModel.getPaymentType().getValue() != null ? viewModel.getPaymentType().getValue() : PaymentType.CASH;
        double paidAmount = paymentType == PaymentType.CASH ? invoiceTotal : 0.0;
        double balanceDue = Math.max(0.0, invoiceTotal - paidAmount);

        android.content.Intent intent = new android.content.Intent(this, InvoicePreviewDraftActivity.class);
        com.hfad.agencyapp.ui.models.Customer customer = viewModel.getSelectedCustomer().getValue();
        String customerName = customer != null && customer.getBusinessName() != null && !customer.getBusinessName().trim().isEmpty()
                ? customer.getBusinessName()
                : getString(R.string.unknown_value);
        String contact = customer != null ? customer.getContactPerson() : null;
        String address = customer != null ? customer.getAddress() : null;

        intent.putExtra(InvoicePreviewDraftActivity.EXTRA_CUSTOMER_NAME, customerName);
        intent.putExtra(InvoicePreviewDraftActivity.EXTRA_CONTACT, contact);
        intent.putExtra(InvoicePreviewDraftActivity.EXTRA_ADDRESS, address);
        String invoiceNumber = "INV-" + (System.currentTimeMillis() / 1000);
        intent.putExtra(InvoicePreviewDraftActivity.EXTRA_INVOICE_NUMBER, invoiceNumber);
        intent.putExtra(InvoicePreviewDraftActivity.EXTRA_INVOICE_DATE, getString(R.string.invoice_generator_date, dateFormat.format(new java.util.Date())));
        intent.putExtra(InvoicePreviewDraftActivity.EXTRA_ITEMS_JSON, arr.toString());
        intent.putExtra(InvoicePreviewDraftActivity.EXTRA_SUBTOTAL, subtotal);
        intent.putExtra(InvoicePreviewDraftActivity.EXTRA_DISCOUNT, totalDiscount);
        intent.putExtra(InvoicePreviewDraftActivity.EXTRA_TOTAL, invoiceTotal);
        intent.putExtra(InvoicePreviewDraftActivity.EXTRA_PAID_AMOUNT, paidAmount);
        intent.putExtra(InvoicePreviewDraftActivity.EXTRA_BALANCE_DUE, balanceDue);
        intent.putExtra(InvoicePreviewDraftActivity.EXTRA_PAYMENT, paymentType.name());

        ChequeDetails chequeDetails = viewModel.getChequeDetails().getValue();
        if (chequeDetails != null) {
            intent.putExtra(InvoicePreviewDraftActivity.EXTRA_CHEQUE_NUMBER, chequeDetails.getChequeNumber());
            intent.putExtra(InvoicePreviewDraftActivity.EXTRA_BANK_NAME, chequeDetails.getBankName());
            intent.putExtra(InvoicePreviewDraftActivity.EXTRA_CHEQUE_DATE, chequeDetails.getChequeDate() != null ? dateFormat.format(chequeDetails.getChequeDate()) : null);
        }

        startActivityForResult(intent, 9876);
    }

    private void setupToggleGroup() {
        binding.togglePaymentType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                PaymentType type;
                if (checkedId == binding.btnCash.getId()) {
                    type = PaymentType.CASH;
                } else if (checkedId == binding.btnCredit.getId()) {
                    type = PaymentType.CREDIT;
                } else {
                    type = PaymentType.CHEQUE;
                }
                viewModel.setPaymentType(type);
            }
        });
        applyAllowedPaymentMethods(viewModel.getSelectedCustomer().getValue());
    }

    private void setupTextInputListeners() {
        binding.etChequeNumber.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setChequeNumber(s.toString());
                binding.tilChequeNumber.setError(null);
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        binding.etBankName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setBankName(s.toString());
                binding.tilBankName.setError(null);
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void openCustomerPicker() {
        com.hfad.agencyapp.data.CustomerRepository repo = new com.hfad.agencyapp.data.CustomerRepository(this);
        try {
            List<Customer> list = repo.getAllCustomersAsync().get();
            if (list == null || list.isEmpty()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.no_customers_title)
                        .setMessage(R.string.no_customers_dialog_message)
                        .setPositiveButton(R.string.ok, null)
                        .show();
                return;
            }

            java.util.List<Customer> activeCustomers = new java.util.ArrayList<>();
            for (Customer c : list) {
                if (!c.isBlocked()) {
                    activeCustomers.add(c);
                }
            }

            if (activeCustomers.isEmpty()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.no_active_customers_title)
                        .setMessage(R.string.no_active_customers_message)
                        .setPositiveButton(R.string.ok, null)
                        .show();
                return;
            }

            CharSequence[] names = new CharSequence[activeCustomers.size()];
            for (int i = 0; i < activeCustomers.size(); i++) {
                names[i] = activeCustomers.get(i).getBusinessName();
            }

            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.select_customer_dialog_title)
                    .setItems(names, (dialog, which) -> viewModel.setSelectedCustomer(activeCustomers.get(which)))
                    .show();
        } catch (java.util.concurrent.ExecutionException | InterruptedException e) {
            Snackbar.make(binding.getRoot(), getString(R.string.failed_load_customers), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void applyAllowedPaymentMethods(Customer customer) {
        Set<PaymentType> allowed = parseAllowedPaymentTypes(customer != null ? customer.getPaymentMethods() : null);

        binding.btnCash.setVisibility(allowed.contains(PaymentType.CASH) ? View.VISIBLE : View.GONE);
        binding.btnCredit.setVisibility(allowed.contains(PaymentType.CREDIT) ? View.VISIBLE : View.GONE);
        binding.btnCheque.setVisibility(allowed.contains(PaymentType.CHEQUE) ? View.VISIBLE : View.GONE);

        PaymentType current = viewModel.getPaymentType().getValue();
        if (current == null || !allowed.contains(current)) {
            PaymentType fallback = allowed.contains(PaymentType.CASH)
                    ? PaymentType.CASH
                    : (allowed.contains(PaymentType.CREDIT) ? PaymentType.CREDIT : PaymentType.CHEQUE);
            viewModel.setPaymentType(fallback);
            checkPaymentButton(fallback);
        } else {
            checkPaymentButton(current);
        }
    }

    private void checkPaymentButton(PaymentType type) {
        if (type == PaymentType.CASH) {
            binding.togglePaymentType.check(binding.btnCash.getId());
        } else if (type == PaymentType.CREDIT) {
            binding.togglePaymentType.check(binding.btnCredit.getId());
        } else if (type == PaymentType.CHEQUE) {
            binding.togglePaymentType.check(binding.btnCheque.getId());
        }
    }

    private Set<PaymentType> parseAllowedPaymentTypes(String paymentMethods) {
        Set<PaymentType> allowed = new HashSet<>();
        if (paymentMethods == null || paymentMethods.trim().isEmpty()) {
            allowed.addAll(Arrays.asList(PaymentType.CASH, PaymentType.CREDIT, PaymentType.CHEQUE));
            return allowed;
        }
        for (String token : paymentMethods.split(",")) {
            if (token == null || token.trim().isEmpty()) continue;
            try {
                allowed.add(PaymentType.valueOf(token.trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (allowed.isEmpty()) {
            allowed.addAll(Arrays.asList(PaymentType.CASH, PaymentType.CREDIT, PaymentType.CHEQUE));
        }
        return allowed;
    }

    private void openProductPicker() {
        com.hfad.agencyapp.data.ProductRepository repo = new com.hfad.agencyapp.data.ProductRepository(this);
        try {
            List<Product> list = repo.searchAsync("%").get();
            if (list == null || list.isEmpty()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.no_products_title)
                        .setMessage(R.string.no_products_message)
                        .setPositiveButton(R.string.ok, null)
                        .show();
                return;
            }

            CharSequence[] labels = new CharSequence[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Product p = list.get(i);
                labels[i] = p.name + " - " + getString(R.string.price_label_currency, currencyFormat.format(p.sellingPrice));
            }

            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.add_product_title)
                    .setItems(labels, (dialog, which) -> {
                        Product selected = list.get(which);
                        if (selected.stock <= 0) {
                            handleOutOfStockSelection(selected);
                        } else {
                            addSelectedProductToInvoice(selected);
                        }
                    })
                    .show();
        } catch (Exception e) {
            Snackbar.make(binding.getRoot(), getString(R.string.failed_load_products), Snackbar.LENGTH_SHORT).show();
        } finally {
            repo.shutdown();
        }
    }

    private void addSelectedProductToInvoice(Product selected) {
        viewModel.addItem(
                String.valueOf(selected.id),
                selected.name,
                selected.sellingPrice,
                selected.discountPercent,
                selected.buyQtyForFreeIssue,
                selected.freeIssueQty
        );
        String message = getString(R.string.product_added_message, selected.name);
        if (selected.buyQtyForFreeIssue > 0 && selected.freeIssueQty > 0) {
            message = getString(R.string.product_added_free_issue_message, selected.name, selected.buyQtyForFreeIssue, selected.freeIssueQty);
        }
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    private void handleOutOfStockSelection(Product selected) {
        String behavior = getOutOfStockBehavior();

        if (Constants.OUT_OF_STOCK_AUTO_ALLOW.equals(behavior)) {
            addSelectedProductToInvoice(selected);
            return;
        }

        if (Constants.OUT_OF_STOCK_BLOCK.equals(behavior)) {
            Snackbar.make(binding.getRoot(), getString(R.string.out_of_stock_blocked_message, selected.name), Snackbar.LENGTH_LONG).show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.out_of_stock_title)
                .setMessage(getString(R.string.out_of_stock_message, selected.name))
                .setPositiveButton(R.string.add, (d, w) -> addSelectedProductToInvoice(selected))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showOutOfStockBehaviorDialog() {
        final String[] values = {
                Constants.OUT_OF_STOCK_AUTO_ALLOW,
                Constants.OUT_OF_STOCK_PROMPT,
                Constants.OUT_OF_STOCK_BLOCK
        };
        final CharSequence[] labels = {
                getString(R.string.out_of_stock_behavior_auto_allow),
                getString(R.string.out_of_stock_behavior_prompt),
                getString(R.string.out_of_stock_behavior_block)
        };

        String current = getOutOfStockBehavior();
        int checkedIndex = 1;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                checkedIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.out_of_stock_behavior_title)
                .setMessage(R.string.out_of_stock_behavior_summary)
                .setSingleChoiceItems(labels, checkedIndex, (dialog, which) -> {
                    setOutOfStockBehavior(values[which]);
                    dialog.dismiss();
                    Snackbar.make(binding.getRoot(), getString(R.string.out_of_stock_behavior_saved_message, labels[which]), Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private String getOutOfStockBehavior() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_APP_SETTINGS, MODE_PRIVATE);
        return prefs.getString(Constants.KEY_OUT_OF_STOCK_BEHAVIOR, Constants.OUT_OF_STOCK_PROMPT);
    }

    private void setOutOfStockBehavior(String behavior) {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_APP_SETTINGS, MODE_PRIVATE);
        prefs.edit().putString(Constants.KEY_OUT_OF_STOCK_BEHAVIOR, behavior).apply();
    }

    private void ensureDefaultInvoicePreferences() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_APP_SETTINGS, MODE_PRIVATE);
        if (!prefs.contains(Constants.KEY_OUT_OF_STOCK_BEHAVIOR)) {
            prefs.edit().putString(Constants.KEY_OUT_OF_STOCK_BEHAVIOR, Constants.OUT_OF_STOCK_PROMPT).apply();
        }
    }

    private void openDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Date selectedDate = new Date(selection);
            viewModel.setChequeDate(selectedDate);
            binding.etChequeDate.setText(dateFormat.format(selectedDate));
            binding.tilChequeDate.setError(null);
        });
        datePicker.show(getSupportFragmentManager(), "date_picker");
    }

    private void saveInvoice() {
        String error = viewModel.validateInvoice();

        if (!error.isEmpty()) {
            if (error.toLowerCase().contains("customer")) {
                binding.tvCustomerError.setVisibility(View.VISIBLE);
            } else if (error.toLowerCase().contains("item")) {
                binding.tvItemsError.setVisibility(View.VISIBLE);
            } else if (error.toLowerCase().contains("cheque")) {
                binding.tvChequeError.setVisibility(View.VISIBLE);
            }
            Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            return;
        }

        binding.tvCustomerError.setVisibility(View.GONE);
        binding.tvItemsError.setVisibility(View.GONE);
        binding.tvChequeError.setVisibility(View.GONE);

        viewModel.saveInvoice(editingInvoiceId > 0 ? editingInvoiceId : null, editingInvoiceNumber);
        Snackbar.make(binding.getRoot(), getString(R.string.invoice_saved_successfully), Snackbar.LENGTH_SHORT).show();
        finish();
    }

    private void loadInvoiceForEdit(long invoiceId) {
        setTitle(R.string.edit_invoice_title);
        com.hfad.agencyapp.data.Repository repo = com.hfad.agencyapp.data.Repository.getInstance(this);
        repo.getInvoiceById(invoiceId).observe(this, invoice -> {
            if (invoice == null) {
                return;
            }

            editingInvoiceNumber = invoice.invoiceNumber;
            setCustomerSelectionLocked(true);

            com.hfad.agencyapp.data.CustomerRepository customerRepository = new com.hfad.agencyapp.data.CustomerRepository(this);
            try {
                com.hfad.agencyapp.ui.models.Customer customer = null;

                if (invoice.customerId > 0) {
                    customer = customerRepository.getByIdAsync(String.valueOf(invoice.customerId)).get();
                }

                if (customer == null && invoice.customerName != null && !invoice.customerName.trim().isEmpty()) {
                    java.util.List<com.hfad.agencyapp.ui.models.Customer> customers = customerRepository.getAllCustomersAsync().get();
                    if (customers != null) {
                        String targetName = normalizeCustomerName(invoice.customerName);
                        for (com.hfad.agencyapp.ui.models.Customer candidate : customers) {
                            if (candidate == null) {
                                continue;
                            }

                            if (matchesInvoiceCustomer(targetName, candidate.getBusinessName())
                                    || matchesInvoiceCustomer(targetName, candidate.getContactPerson())) {
                                customer = candidate;
                                break;
                            }
                        }
                    }
                }

                if (customer == null) {
                    customer = new com.hfad.agencyapp.ui.models.Customer(
                            String.valueOf(invoice.customerId > 0 ? invoice.customerId : 0),
                            invoice.customerName != null ? invoice.customerName : "",
                            invoice.customerName != null ? invoice.customerName : "",
                            ""
                    );
                }

                viewModel.setSelectedCustomer(customer);
            } catch (Exception ignored) {
            }

            if (invoice.paymentMethod != null) {
                try {
                    viewModel.setPaymentType(PaymentType.valueOf(invoice.paymentMethod));
                } catch (IllegalArgumentException ignored) {
                }
            }

            repo.getAllProducts().observe(this, products -> {
                java.util.Map<Long, Product> productMap = new java.util.HashMap<>();
                if (products != null) {
                    for (Product product : products) {
                        productMap.put(product.id, product);
                    }
                }

                repo.getInvoiceItems(invoiceId).observe(this, items -> {
                    java.util.List<com.hfad.agencyapp.ui.models.InvoiceItem> uiItems = new java.util.ArrayList<>();
                    if (items != null) {
                        for (com.hfad.agencyapp.data.entities.InvoiceItem item : items) {
                            Product product = productMap.get(item.productId);
                            String productName = product != null && product.name != null ? product.name : String.valueOf(item.productId);
                            double lineSubtotal = item.quantity * item.unitPrice;
                            double discountPercent = 0.0;
                            if (lineSubtotal > 0.0 && item.totalPrice >= 0.0 && item.totalPrice < lineSubtotal) {
                                discountPercent = ((lineSubtotal - item.totalPrice) * 100.0) / lineSubtotal;
                            } else if (product != null && product.discountPercent > 0.0) {
                                discountPercent = product.discountPercent;
                            }
                            uiItems.add(new com.hfad.agencyapp.ui.models.InvoiceItem(
                                    String.valueOf(item.productId),
                                    productName,
                                    item.quantity,
                                    item.unitPrice,
                                    Math.max(0.0, discountPercent),
                                    item.freeIssueBuyQty,
                                    item.freeIssueBonusQty
                            ));
                        }
                    }
                    viewModel.setItems(uiItems);
                });
            });
        });
    }

    private String normalizeCustomerName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.US);
    }

    private boolean matchesInvoiceCustomer(String invoiceName, String customerField) {
        if (invoiceName.isEmpty() || customerField == null || customerField.trim().isEmpty()) {
            return false;
        }
        return invoiceName.equals(normalizeCustomerName(customerField));
    }
}
