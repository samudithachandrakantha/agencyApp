package com.hfad.agencyapp.ui.invoice;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.hfad.agencyapp.databinding.ActivityCreateInvoiceBinding;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.ui.adapters.InvoiceItemsAdapter;
import com.hfad.agencyapp.ui.models.Customer;
import com.hfad.agencyapp.ui.models.PaymentType;
import com.hfad.agencyapp.viewmodel.CreateInvoiceViewModel;

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

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
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
                binding.tvSubtotal.setText(getString(com.hfad.agencyapp.R.string.amount_format, currencyFormat.format(subtotal))));

        viewModel.getDiscount().observe(this, discount ->
                binding.tvDiscount.setText(getString(com.hfad.agencyapp.R.string.amount_format, currencyFormat.format(discount))));

        viewModel.getTotal().observe(this, total ->
                binding.tvTotal.setText(getString(com.hfad.agencyapp.R.string.amount_format, currencyFormat.format(total))));

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

        View previewView = LayoutInflater.from(this).inflate(com.hfad.agencyapp.R.layout.dialog_invoice_preview, null);
        TextView tvCustomer = previewView.findViewById(com.hfad.agencyapp.R.id.tvPreviewCustomer);
        TextView tvInvoiceNumber = previewView.findViewById(com.hfad.agencyapp.R.id.tvPreviewInvoiceNumber);
        TextView tvItems = previewView.findViewById(com.hfad.agencyapp.R.id.tvPreviewItems);
        TextView tvSubtotal = previewView.findViewById(com.hfad.agencyapp.R.id.tvPreviewSubtotal);
        TextView tvDiscount = previewView.findViewById(com.hfad.agencyapp.R.id.tvPreviewDiscount);
        TextView tvTotal = previewView.findViewById(com.hfad.agencyapp.R.id.tvPreviewTotal);
        TextView tvPayment = previewView.findViewById(com.hfad.agencyapp.R.id.tvPreviewPayment);

        com.hfad.agencyapp.ui.models.Customer customer = viewModel.getSelectedCustomer().getValue();
        tvCustomer.setText(customer != null ? customer.getBusinessName() : "Unknown");

        String invoiceNumber = "INV-" + (System.currentTimeMillis() / 1000);
        tvInvoiceNumber.setText(invoiceNumber);

        StringBuilder sb = new StringBuilder();
        java.util.List<com.hfad.agencyapp.ui.models.InvoiceItem> items = viewModel.getItems().getValue();
        double subtotal = 0.0;
        double totalDiscount = 0.0;
        if (items != null) {
            for (com.hfad.agencyapp.ui.models.InvoiceItem it : items) {
                double lineSubtotal = it.getQuantity() * it.getUnitPrice();
                double lineDiscount = it.getDiscount();
                double lineTotal = it.getLineTotal();
                subtotal += lineSubtotal;
                totalDiscount += lineDiscount;
                sb.append(it.getProductName())
                        .append("\nQty: ")
                        .append(it.getQuantity())
                        .append(" x Rs. ")
                        .append(currencyFormat.format(it.getUnitPrice()))
                        .append(" = Rs. ")
                        .append(currencyFormat.format(lineSubtotal))
                        .append("\nDiscount: Rs. ")
                        .append(currencyFormat.format(lineDiscount))
                        .append(" (")
                        .append(currencyFormat.format(it.getDiscountPercent()))
                        .append("% of Rs. ")
                        .append(currencyFormat.format(lineSubtotal))
                        .append(")\nLine total: Rs. ")
                        .append(currencyFormat.format(lineTotal))
                        .append("\n");
                String freeIssueSummary = it.getFreeIssueSummary();
                if (!freeIssueSummary.isEmpty()) {
                    sb.append(freeIssueSummary).append("\n");
                }
            }
        }
        tvItems.setText(sb.toString());

        Double total = viewModel.getTotal().getValue();
        tvSubtotal.setText(getString(com.hfad.agencyapp.R.string.amount_format, currencyFormat.format(subtotal)));
        tvDiscount.setText(getString(com.hfad.agencyapp.R.string.amount_format, currencyFormat.format(totalDiscount)));
        tvTotal.setText(getString(com.hfad.agencyapp.R.string.amount_format, currencyFormat.format(total != null ? total : 0.0)));

        tvPayment.setText(viewModel.getPaymentType().getValue().name());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Invoice Preview")
                .setView(previewView)
                .setNegativeButton("Edit", (d, which) -> d.dismiss())
                .setPositiveButton("Save", (d, which) -> {
                    viewModel.saveInvoice(editingInvoiceId > 0 ? editingInvoiceId : null, editingInvoiceNumber);
                    Snackbar.make(binding.getRoot(), "Invoice saved successfully", Snackbar.LENGTH_SHORT).show();
                    d.dismiss();
                    finish();
                })
                .show();
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
                        .setTitle("No customers")
                        .setMessage("No customers found. Please add a customer first.")
                        .setPositiveButton("OK", null)
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
                        .setTitle("No active customers")
                        .setMessage("All customers are blocked. Please unblock a customer first.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            CharSequence[] names = new CharSequence[activeCustomers.size()];
            for (int i = 0; i < activeCustomers.size(); i++) {
                names[i] = activeCustomers.get(i).getBusinessName();
            }

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Select Customer")
                    .setItems(names, (dialog, which) -> viewModel.setSelectedCustomer(activeCustomers.get(which)))
                    .show();
        } catch (java.util.concurrent.ExecutionException | InterruptedException e) {
            Snackbar.make(binding.getRoot(), "Failed to load customers", Snackbar.LENGTH_SHORT).show();
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
                        .setTitle("No products")
                        .setMessage("No products found. Please add a product first.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            CharSequence[] labels = new CharSequence[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Product p = list.get(i);
                labels[i] = p.name + " - Rs. " + currencyFormat.format(p.sellingPrice);
            }

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Add Product")
                    .setItems(labels, (dialog, which) -> {
                        Product selected = list.get(which);
                        viewModel.addItem(
                                String.valueOf(selected.id),
                                selected.name,
                                selected.sellingPrice,
                                selected.discountPercent,
                                selected.buyQtyForFreeIssue,
                                selected.freeIssueQty
                        );
                        String message = selected.name + " added";
                        if (selected.buyQtyForFreeIssue > 0 && selected.freeIssueQty > 0) {
                            message += " • Free issue: buy " + selected.buyQtyForFreeIssue + " get " + selected.freeIssueQty + " free";
                        }
                        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
                    })
                    .show();
        } catch (Exception e) {
            Snackbar.make(binding.getRoot(), "Failed to load products", Snackbar.LENGTH_SHORT).show();
        } finally {
            repo.shutdown();
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
        Snackbar.make(binding.getRoot(), "Invoice saved successfully", Snackbar.LENGTH_SHORT).show();
        finish();
    }

    private void loadInvoiceForEdit(long invoiceId) {
        setTitle("Edit Invoice");
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
                            uiItems.add(new com.hfad.agencyapp.ui.models.InvoiceItem(
                                    String.valueOf(item.productId),
                                    productName,
                                    item.quantity,
                                    item.unitPrice,
                                    0.0,
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
