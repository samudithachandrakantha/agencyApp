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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        popup.getMenu().add("Call");
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
            } else if (title.equals("Call")) {
                // dial if phone number exists
                String phone = c.getPhoneNumber();
                if (phone == null || phone.trim().isEmpty()) {
                    Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(android.net.Uri.parse("tel:" + phone));
                    startActivity(intent);
                }
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
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_customer, null);
        com.google.android.material.textfield.TextInputLayout tilBusiness = dialogView.findViewById(R.id.tilBusinessName);
        com.google.android.material.textfield.TextInputLayout tilContact = dialogView.findViewById(R.id.tilContactPerson);
        com.google.android.material.textfield.TextInputLayout tilCity = dialogView.findViewById(R.id.tilCity);
        com.google.android.material.textfield.TextInputEditText etBusiness = dialogView.findViewById(R.id.etBusinessName);
        com.google.android.material.textfield.TextInputEditText etContact = dialogView.findViewById(R.id.etContactPerson);
        com.google.android.material.textfield.TextInputEditText etCity = dialogView.findViewById(R.id.etCity);
        com.google.android.material.button.MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveCustomer);
        com.google.android.material.button.MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelCustomer);

        if (customer != null) {
            etBusiness.setText(customer.getBusinessName());
            etContact.setText(customer.getContactPerson());
            etCity.setText(customer.getCity());
        }

        final AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String business = etBusiness.getText() != null ? etBusiness.getText().toString().trim() : "";
            String contact = etContact.getText() != null ? etContact.getText().toString().trim() : "";
            String city = etCity.getText() != null ? etCity.getText().toString().trim() : "";

            boolean valid = true;
            if (business.isEmpty()) { tilBusiness.setError("Business name required"); valid = false; } else tilBusiness.setError(null);
            if (contact.isEmpty()) { tilContact.setError("Contact person required"); valid = false; } else tilContact.setError(null);
            if (city.isEmpty()) { tilCity.setError("City required"); valid = false; } else tilCity.setError(null);

            if (!valid) return;

            String id = customer != null ? customer.getId() : null;
            Customer c = new Customer(id, business, contact, "", "", city);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}







