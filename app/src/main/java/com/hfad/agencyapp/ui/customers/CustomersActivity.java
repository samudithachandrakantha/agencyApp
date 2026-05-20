package com.hfad.agencyapp.ui.customers;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hfad.agencyapp.R;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.snackbar.Snackbar;
import com.hfad.agencyapp.databinding.ActivityCustomersBinding;
import com.hfad.agencyapp.ui.adapters.CustomersAdapter;
import com.hfad.agencyapp.ui.models.Customer;
import com.hfad.agencyapp.viewmodel.CustomerViewModel;

/**
 * Activity that shows list of customers and allows add/edit/delete.
 */
public class CustomersActivity extends AppCompatActivity {
    private ActivityCustomersBinding binding;
    private CustomerViewModel viewModel;
    private CustomersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Customers");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    private void setupRecyclerView() {
        adapter = new CustomersAdapter(new CustomersAdapter.OnCustomerActionListener() {
            @Override
            public void onClick(Customer customer) {
                // Open detail
                Intent intent = new Intent(CustomersActivity.this, CustomerDetailActivity.class);
                intent.putExtra("customer_id", customer.getId());
                startActivity(intent);
            }

            @Override
            public void onLongClick(Customer customer, View anchor) {
                showContextMenu(customer, anchor);
            }
        });
        binding.rvCustomers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCustomers.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        binding.rvCustomers.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getCustomers().observe(this, list -> {
            adapter.submitList(list);
            binding.emptyState.getRoot().setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void setupListeners() {
        binding.fabAddCustomer.setOnClickListener(v -> showAddEditCustomerDialog(null));

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            private Runnable lastTask;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Debounce 300ms
                binding.etSearch.removeCallbacks(lastTask);
                lastTask = () -> viewModel.filter(s.toString());
                binding.etSearch.postDelayed(lastTask, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }

    private void showContextMenu(Customer c, View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Edit");
        popup.getMenu().add("Delete");
        popup.setOnMenuItemClickListener(item -> {
            CharSequence title = item.getTitle();
            if ("Edit".contentEquals(title)) {
                showAddEditCustomerDialog(c);
                return true;
            } else if ("Delete".contentEquals(title)) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Delete Customer")
                        .setMessage("Are you sure you want to delete " + c.getBusinessName() + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            boolean ok = viewModel.deleteCustomer(c.getId());
                            Toast.makeText(this, ok ? "Deleted" : "Delete failed", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    /**
     * Show a compact add/edit dialog for customer with a 3-step flow:
     * required fields -> optional fields -> payment methods.
     * If customer is null -> add mode. Else edit mode.
     */
    private void showAddEditCustomerDialog(Customer customer) {
        showRequiredFieldsDialog(customer, null, null, null, null, null, null, null);
    }

    private void showRequiredFieldsDialog(Customer customer, String business, String contact, String address,
                                          String phone, String br, String idNum, String paymentMethods) {
        View firstView = getLayoutInflater().inflate(R.layout.dialog_add_customer, null);
        com.google.android.material.textfield.TextInputLayout tilBusiness = firstView.findViewById(R.id.tilBusinessName);
        com.google.android.material.textfield.TextInputLayout tilContact = firstView.findViewById(R.id.tilContactPerson);
        com.google.android.material.textfield.TextInputLayout tilAddress = firstView.findViewById(R.id.tilAddress);
        com.google.android.material.textfield.TextInputEditText etBusiness = firstView.findViewById(R.id.etBusinessName);
        com.google.android.material.textfield.TextInputEditText etContact = firstView.findViewById(R.id.etContactPerson);
        com.google.android.material.textfield.TextInputEditText etAddress = firstView.findViewById(R.id.etAddress);
        com.google.android.material.textfield.TextInputEditText etPhone = firstView.findViewById(R.id.etPhone);
        com.google.android.material.textfield.TextInputEditText etBrNumber = firstView.findViewById(R.id.etBrNumber);
        com.google.android.material.textfield.TextInputEditText etIdNumber = firstView.findViewById(R.id.etIdNumber);
        hideParent(etPhone);
        hideParent(etBrNumber);
        hideParent(etIdNumber);

        com.google.android.material.button.MaterialButton btnNext = firstView.findViewById(R.id.btnSaveCustomer);
        com.google.android.material.button.MaterialButton btnCancel = firstView.findViewById(R.id.btnCancelCustomer);
        btnNext.setText(R.string.next);

        if (customer != null) {
            etBusiness.setText(business != null ? business : customer.getBusinessName());
            etContact.setText(contact != null ? contact : customer.getContactPerson());
            etAddress.setText(address != null ? address : customer.getAddress());
        } else {
            etBusiness.setText(business);
            etContact.setText(contact);
            etAddress.setText(address);
        }

        final AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(firstView).create();
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnNext.setOnClickListener(v -> {
            String businessValue = etBusiness.getText() != null ? etBusiness.getText().toString().trim() : "";
            String contactValue = etContact.getText() != null ? etContact.getText().toString().trim() : "";
            String addressValue = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";

            boolean valid = true;
            if (businessValue.isEmpty()) { tilBusiness.setError("Business name required"); valid = false; } else tilBusiness.setError(null);
            if (contactValue.isEmpty()) { tilContact.setError("Contact person required"); valid = false; } else tilContact.setError(null);
            if (addressValue.isEmpty()) { tilAddress.setError("Address required"); valid = false; } else tilAddress.setError(null);

            if (!valid) return;

            dialog.dismiss();
            showOptionalFieldsDialog(customer, businessValue, contactValue, addressValue, phone, br, idNum, paymentMethods);
        });

        dialog.show();
    }

    private void showOptionalFieldsDialog(Customer customer, String business, String contact, String address,
                                          String phone, String br, String idNum, String paymentMethods) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_customer, null);
        com.google.android.material.textfield.TextInputLayout tilBusiness = dialogView.findViewById(R.id.tilBusinessName);
        com.google.android.material.textfield.TextInputLayout tilContact = dialogView.findViewById(R.id.tilContactPerson);
        com.google.android.material.textfield.TextInputLayout tilAddress = dialogView.findViewById(R.id.tilAddress);
        if (tilBusiness != null) tilBusiness.setVisibility(View.GONE);
        if (tilContact != null) tilContact.setVisibility(View.GONE);
        if (tilAddress != null) tilAddress.setVisibility(View.GONE);

        com.google.android.material.textfield.TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        com.google.android.material.textfield.TextInputEditText etBrNumber = dialogView.findViewById(R.id.etBrNumber);
        com.google.android.material.textfield.TextInputEditText etIdNumber = dialogView.findViewById(R.id.etIdNumber);
        com.google.android.material.button.MaterialButton btnNext = dialogView.findViewById(R.id.btnSaveCustomer);
        com.google.android.material.button.MaterialButton btnBack = dialogView.findViewById(R.id.btnCancelCustomer);
        btnNext.setText(R.string.next);
        btnBack.setText(R.string.back);

        if (customer != null) {
            if (etPhone != null) etPhone.setText(phone != null ? phone : customer.getPhone());
            if (etBrNumber != null) etBrNumber.setText(br != null ? br : customer.getBrNumber());
            if (etIdNumber != null) etIdNumber.setText(idNum != null ? idNum : customer.getIdNumber());
            if (paymentMethods == null) paymentMethods = customer.getPaymentMethods();
        } else {
            if (etPhone != null) etPhone.setText(phone);
            if (etBrNumber != null) etBrNumber.setText(br);
            if (etIdNumber != null) etIdNumber.setText(idNum);
        }

        final String finalPaymentMethods = paymentMethods;
        final AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(dialogView).create();

        btnBack.setOnClickListener(v -> {
            dialog.dismiss();
            showRequiredFieldsDialog(customer, business, contact, address,
                    etPhone != null && etPhone.getText() != null ? etPhone.getText().toString().trim() : phone,
                    etBrNumber != null && etBrNumber.getText() != null ? etBrNumber.getText().toString().trim() : br,
                    etIdNumber != null && etIdNumber.getText() != null ? etIdNumber.getText().toString().trim() : idNum,
                    finalPaymentMethods);
        });

        btnNext.setOnClickListener(v -> {
            String phoneValue = etPhone != null && etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
            String brValue = etBrNumber != null && etBrNumber.getText() != null ? etBrNumber.getText().toString().trim() : "";
            String idValue = etIdNumber != null && etIdNumber.getText() != null ? etIdNumber.getText().toString().trim() : "";
            dialog.dismiss();
            showPaymentMethodsDialog(customer, business, contact, address, phoneValue, brValue, idValue, finalPaymentMethods);
        });

        dialog.show();
    }

    private void showPaymentMethodsDialog(Customer customer, String business, String contact, String address,
                                          String phone, String br, String idNum, String paymentMethods) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_customer_payment_methods, null);
        android.widget.CheckBox cbCash = dialogView.findViewById(R.id.cbCash);
        android.widget.CheckBox cbCredit = dialogView.findViewById(R.id.cbCredit);
        android.widget.CheckBox cbCheque = dialogView.findViewById(R.id.cbCheque);
        com.google.android.material.button.MaterialButton btnBack = dialogView.findViewById(R.id.btnBackPayment);
        com.google.android.material.button.MaterialButton btnSave = dialogView.findViewById(R.id.btnSavePayment);

        String initialMethods = paymentMethods;
        if (initialMethods == null || initialMethods.trim().isEmpty()) {
            initialMethods = customer != null ? customer.getPaymentMethods() : null;
        }
        final String methodsForBack = initialMethods;
        applyPaymentMethods(cbCash, cbCredit, cbCheque, methodsForBack);

        final AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(dialogView).create();

        btnBack.setOnClickListener(v -> {
            dialog.dismiss();
            showOptionalFieldsDialog(customer, business, contact, address, phone, br, idNum, methodsForBack);
        });

        btnSave.setOnClickListener(v -> {
            String methods = collectPaymentMethods(cbCash, cbCredit, cbCheque);
            if (methods.isEmpty()) {
                Toast.makeText(this, "Select at least one payment method", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = customer != null ? customer.getId() : null;
            Customer c = new Customer(id, business, contact, address,
                    phone.isEmpty() ? null : phone,
                    br.isEmpty() ? null : br,
                    idNum.isEmpty() ? null : idNum,
                    methods);

            new Thread(() -> {
                boolean ok = viewModel.saveCustomer(c);
                runOnUiThread(() -> {
                    if (ok) {
                        Snackbar.make(binding.getRoot(), "Customer saved", Snackbar.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Snackbar.make(binding.getRoot(), "Save failed", Snackbar.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        dialog.show();
    }

    private void hideParent(View child) {
        if (child != null && child.getParent() instanceof View) {
            ((View) child.getParent()).setVisibility(View.GONE);
        }
    }

    private void applyPaymentMethods(android.widget.CheckBox cbCash,
                                     android.widget.CheckBox cbCredit,
                                     android.widget.CheckBox cbCheque,
                                     String paymentMethods) {
        java.util.Set<String> methods = parsePaymentMethods(paymentMethods);
        cbCash.setChecked(methods.contains("CASH"));
        cbCredit.setChecked(methods.contains("CREDIT"));
        cbCheque.setChecked(methods.contains("CHEQUE"));
    }

    private String collectPaymentMethods(android.widget.CheckBox cbCash,
                                         android.widget.CheckBox cbCredit,
                                         android.widget.CheckBox cbCheque) {
        StringBuilder builder = new StringBuilder();
        if (cbCash.isChecked()) builder.append("CASH,");
        if (cbCredit.isChecked()) builder.append("CREDIT,");
        if (cbCheque.isChecked()) builder.append("CHEQUE,");
        if (builder.length() == 0) return "";
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    private java.util.Set<String> parsePaymentMethods(String paymentMethods) {
        java.util.Set<String> methods = new java.util.HashSet<>();
        if (paymentMethods == null || paymentMethods.trim().isEmpty()) {
            methods.add("CASH");
            methods.add("CREDIT");
            methods.add("CHEQUE");
            return methods;
        }
        for (String method : paymentMethods.split(",")) {
            if (method != null && !method.trim().isEmpty()) {
                methods.add(method.trim().toUpperCase());
            }
        }
        return methods;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}






