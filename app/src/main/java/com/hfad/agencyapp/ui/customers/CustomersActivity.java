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
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

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

    private void showContextMenu(Customer c, View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Edit");
        popup.getMenu().add("Delete");
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Edit")) {
                showAddEditCustomerDialog(c);
                return true;
            } else if (title.equals("Delete")) {
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
     * Show a compact add/edit dialog (top-up box) for customer with only business name, contact person and city.
     * If customer is null -> add mode. Else edit mode.
     */
    private void showAddEditCustomerDialog(Customer customer) {
        // Two-step dialog: first dialog collects required fields (business, contact, city)
        // second dialog collects optional fields (phone, brNumber, idNumber) and performs save.

        View firstView = getLayoutInflater().inflate(R.layout.dialog_add_customer, null);
        com.google.android.material.textfield.TextInputLayout tilBusiness1 = firstView.findViewById(R.id.tilBusinessName);
        com.google.android.material.textfield.TextInputLayout tilContact1 = firstView.findViewById(R.id.tilContactPerson);
        com.google.android.material.textfield.TextInputLayout tilCity1 = firstView.findViewById(R.id.tilCity);
        com.google.android.material.textfield.TextInputEditText etBusiness1 = firstView.findViewById(R.id.etBusinessName);
        com.google.android.material.textfield.TextInputEditText etContact1 = firstView.findViewById(R.id.etContactPerson);
        com.google.android.material.textfield.TextInputEditText etCity1 = firstView.findViewById(R.id.etCity);
        // hide optional fields in first dialog by hiding the parent of the optional EditTexts (if present)
        com.google.android.material.textfield.TextInputEditText etPhone1 = firstView.findViewById(R.id.etPhone);
        com.google.android.material.textfield.TextInputEditText etBr1Edit = firstView.findViewById(R.id.etBrNumber);
        com.google.android.material.textfield.TextInputEditText etId1Edit = firstView.findViewById(R.id.etIdNumber);
        if (etPhone1 != null && etPhone1.getParent() instanceof View) ((View) etPhone1.getParent()).setVisibility(View.GONE);
        if (etBr1Edit != null && etBr1Edit.getParent() instanceof View) ((View) etBr1Edit.getParent()).setVisibility(View.GONE);
        if (etId1Edit != null && etId1Edit.getParent() instanceof View) ((View) etId1Edit.getParent()).setVisibility(View.GONE);

        com.google.android.material.button.MaterialButton btnNext = firstView.findViewById(R.id.btnSaveCustomer);
        com.google.android.material.button.MaterialButton btnCancel1 = firstView.findViewById(R.id.btnCancelCustomer);

        // Pre-fill required fields if editing
        if (customer != null) {
            etBusiness1.setText(customer.getBusinessName());
            etContact1.setText(customer.getContactPerson());
            etCity1.setText(customer.getCity());
        }

        final AlertDialog firstDialog = new MaterialAlertDialogBuilder(this)
                .setView(firstView)
                .create();

        btnCancel1.setOnClickListener(v -> firstDialog.dismiss());
        btnNext.setText("Next");

        btnNext.setOnClickListener(v -> {
            String business = etBusiness1.getText() != null ? etBusiness1.getText().toString().trim() : "";
            String contact = etContact1.getText() != null ? etContact1.getText().toString().trim() : "";
            String city = etCity1.getText() != null ? etCity1.getText().toString().trim() : "";

            boolean valid = true;
            if (business.isEmpty()) { tilBusiness1.setError("Business name required"); valid = false; } else tilBusiness1.setError(null);
            if (contact.isEmpty()) { tilContact1.setError("Contact person required"); valid = false; } else tilContact1.setError(null);
            if (city.isEmpty()) { tilCity1.setError("City required"); valid = false; } else tilCity1.setError(null);

            if (!valid) return;

            // Open second dialog for optional fields. Pass along required values.
            firstDialog.dismiss();
            showOptionalFieldsDialog(customer, business, contact, city);
        });

        firstDialog.show();
    }

    // Helper to show second dialog (optional fields) and save
    private void showOptionalFieldsDialog(Customer customer, String business, String contact, String city) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_customer, null);
        // In this view hide the required-field input layouts to reduce clutter
        com.google.android.material.textfield.TextInputLayout tilBusiness = dialogView.findViewById(R.id.tilBusinessName);
        com.google.android.material.textfield.TextInputLayout tilContact = dialogView.findViewById(R.id.tilContactPerson);
        com.google.android.material.textfield.TextInputLayout tilCity = dialogView.findViewById(R.id.tilCity);
        if (tilBusiness != null) tilBusiness.setVisibility(View.GONE);
        if (tilContact != null) tilContact.setVisibility(View.GONE);
        if (tilCity != null) tilCity.setVisibility(View.GONE);

        com.google.android.material.textfield.TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        com.google.android.material.textfield.TextInputEditText etBrNumber = dialogView.findViewById(R.id.etBrNumber);
        com.google.android.material.textfield.TextInputEditText etIdNumber = dialogView.findViewById(R.id.etIdNumber);
        com.google.android.material.button.MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveCustomer);
        com.google.android.material.button.MaterialButton btnBack = dialogView.findViewById(R.id.btnCancelCustomer);

        // Pre-fill optional fields if editing
        if (customer != null) {
            if (etPhone != null) etPhone.setText(customer.getPhone());
            if (etBrNumber != null) etBrNumber.setText(customer.getBrNumber());
            if (etIdNumber != null) etIdNumber.setText(customer.getIdNumber());
        }

        final AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        btnBack.setOnClickListener(v -> {
            // Go back to first dialog with previously entered required values
            dialog.dismiss();
            // Re-open first dialog prefilled
            showAddEditCustomerDialog_restoreFirst(customer, business, contact, city);
        });

        btnSave.setText("Save");
        btnSave.setOnClickListener(v -> {
            String phone = etPhone != null && etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
            String br = etBrNumber != null && etBrNumber.getText() != null ? etBrNumber.getText().toString().trim() : "";
            String idNum = etIdNumber != null && etIdNumber.getText() != null ? etIdNumber.getText().toString().trim() : "";

            String id = customer != null ? customer.getId() : null;
            Customer c = new Customer(id, business, contact, city,
                    phone.isEmpty() ? null : phone,
                    br.isEmpty() ? null : br,
                    idNum.isEmpty() ? null : idNum);

            // Save on background thread to avoid blocking UI
            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                boolean ok = viewModel.saveCustomer(c);
                runOnUiThread(() -> {
                    if (ok) {
                        Snackbar.make(binding.getRoot(), "Customer saved", Snackbar.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Snackbar.make(binding.getRoot(), "Save failed", Snackbar.LENGTH_SHORT).show();
                    }
                });
            });
        });

        dialog.show();
    }

    // Internal helper used when user goes "Back" from second dialog to first; restores required values
    private void showAddEditCustomerDialog_restoreFirst(Customer customer, String business, String contact, String city) {
        View firstView = getLayoutInflater().inflate(R.layout.dialog_add_customer, null);
        com.google.android.material.textfield.TextInputLayout tilBusiness1 = firstView.findViewById(R.id.tilBusinessName);
        com.google.android.material.textfield.TextInputLayout tilContact1 = firstView.findViewById(R.id.tilContactPerson);
        com.google.android.material.textfield.TextInputLayout tilCity1 = firstView.findViewById(R.id.tilCity);
        com.google.android.material.textfield.TextInputEditText etBusiness1 = firstView.findViewById(R.id.etBusinessName);
        com.google.android.material.textfield.TextInputEditText etContact1 = firstView.findViewById(R.id.etContactPerson);
        com.google.android.material.textfield.TextInputEditText etCity1 = firstView.findViewById(R.id.etCity);
        // hide optional fields in first dialog
        View optPhone1 = firstView.findViewById(R.id.tilPhone);
        View optBr1 = firstView.findViewById(R.id.tilBrNumber);
        View optId1 = firstView.findViewById(R.id.tilIdNumber);
        if (optPhone1 != null) optPhone1.setVisibility(View.GONE);
        if (optBr1 != null) optBr1.setVisibility(View.GONE);
        if (optId1 != null) optId1.setVisibility(View.GONE);

        com.google.android.material.button.MaterialButton btnNext = firstView.findViewById(R.id.btnSaveCustomer);
        com.google.android.material.button.MaterialButton btnCancel1 = firstView.findViewById(R.id.btnCancelCustomer);

        etBusiness1.setText(business);
        etContact1.setText(contact);
        etCity1.setText(city);

        final AlertDialog firstDialog = new MaterialAlertDialogBuilder(this)
                .setView(firstView)
                .create();

        btnCancel1.setOnClickListener(v -> firstDialog.dismiss());
        btnNext.setText("Next");

        btnNext.setOnClickListener(v -> {
            String b = etBusiness1.getText() != null ? etBusiness1.getText().toString().trim() : "";
            String ct = etContact1.getText() != null ? etContact1.getText().toString().trim() : "";
            String cy = etCity1.getText() != null ? etCity1.getText().toString().trim() : "";

            boolean valid = true;
            if (b.isEmpty()) { tilBusiness1.setError("Business name required"); valid = false; } else tilBusiness1.setError(null);
            if (ct.isEmpty()) { tilContact1.setError("Contact person required"); valid = false; } else tilContact1.setError(null);
            if (cy.isEmpty()) { tilCity1.setError("City required"); valid = false; } else tilCity1.setError(null);

            if (!valid) return;

            firstDialog.dismiss();
            showOptionalFieldsDialog(customer, b, ct, cy);
        });

        firstDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}






