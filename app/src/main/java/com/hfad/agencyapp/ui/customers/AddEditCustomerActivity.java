package com.hfad.agencyapp.ui.customers;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.hfad.agencyapp.R;
import com.hfad.agencyapp.databinding.ActivityAddEditCustomerBinding;
import com.hfad.agencyapp.ui.models.Customer;
import com.hfad.agencyapp.viewmodel.CustomerViewModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Activity for adding or editing a customer.
 */
public class AddEditCustomerActivity extends AppCompatActivity {
    private ActivityAddEditCustomerBinding binding;
    private CustomerViewModel viewModel;
    private String customerId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditCustomerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        customerId = getIntent().getStringExtra("customer_id");
        if (customerId != null) {
            getSupportActionBar().setTitle("Edit Customer");
            loadCustomer(customerId);
        } else {
            getSupportActionBar().setTitle("Add Customer");
        }
    }

    private void loadCustomer(String id) {
        Customer c = viewModel.getById(id);
        if (c != null) {
            binding.etBusinessName.setText(c.getBusinessName());
            binding.etContactPerson.setText(c.getContactPerson());
            binding.etAddress.setText(c.getAddress());
            // optional fields
            if (binding.getRoot().findViewById(com.hfad.agencyapp.R.id.etPhone) != null) {
                binding.etPhone.setText(c.getPhone());
            }
            if (binding.getRoot().findViewById(com.hfad.agencyapp.R.id.etBrNumber) != null) {
                binding.etBrNumber.setText(c.getBrNumber());
            }
            if (binding.getRoot().findViewById(com.hfad.agencyapp.R.id.etIdNumber) != null) {
                binding.etIdNumber.setText(c.getIdNumber());
            }
            applyPaymentMethods(c.getPaymentMethods());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            saveCustomer();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveCustomer() {
        String business = safeText(binding.etBusinessName);
        String contact = safeText(binding.etContactPerson);
        String address = safeText(binding.etAddress);
        String phone = "";
        String br = "";
        String idNum = "";
        if (binding.getRoot().findViewById(com.hfad.agencyapp.R.id.etPhone) != null) {
            phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        }
        if (binding.getRoot().findViewById(com.hfad.agencyapp.R.id.etBrNumber) != null) {
            br = binding.etBrNumber.getText() != null ? binding.etBrNumber.getText().toString().trim() : "";
        }
        if (binding.getRoot().findViewById(com.hfad.agencyapp.R.id.etIdNumber) != null) {
            idNum = binding.etIdNumber.getText() != null ? binding.etIdNumber.getText().toString().trim() : "";
        }

        boolean valid = true;
        if (TextUtils.isEmpty(business)) {
            binding.tilBusinessName.setError("Business name required");
            valid = false;
        } else {
            binding.tilBusinessName.setError(null);
        }
        if (TextUtils.isEmpty(contact)) {
            binding.tilContactPerson.setError("Contact person required");
            valid = false;
        } else {
            binding.tilContactPerson.setError(null);
        }
        if (TextUtils.isEmpty(address)) {
            binding.tilAddress.setError("Address required");
            valid = false;
        } else {
            binding.tilAddress.setError(null);
        }

        String paymentMethods = collectPaymentMethods();
        if (paymentMethods.isEmpty()) {
            Toast.makeText(this, "Select at least one payment method", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (!valid) return;

        Customer c = new Customer(customerId, business, contact, address,
                phone.isEmpty() ? null : phone,
                br.isEmpty() ? null : br,
                idNum.isEmpty() ? null : idNum,
                paymentMethods);

        new Thread(() -> {
            boolean ok = viewModel.saveCustomer(c);
            runOnUiThread(() -> {
                if (ok) {
                    Snackbar.make(binding.getRoot(), "Customer saved", Snackbar.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private String safeText(com.google.android.material.textfield.TextInputEditText editText) {
        return editText != null && editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void applyPaymentMethods(String paymentMethods) {
        Set<String> methods = parsePaymentMethods(paymentMethods);
        binding.cbCash.setChecked(methods.contains("CASH"));
        binding.cbCredit.setChecked(methods.contains("CREDIT"));
        binding.cbCheque.setChecked(methods.contains("CHEQUE"));
    }

    private String collectPaymentMethods() {
        StringBuilder builder = new StringBuilder();
        if (binding.cbCash.isChecked()) builder.append("CASH,");
        if (binding.cbCredit.isChecked()) builder.append("CREDIT,");
        if (binding.cbCheque.isChecked()) builder.append("CHEQUE,");
        if (builder.length() == 0) return "";
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    private Set<String> parsePaymentMethods(String paymentMethods) {
        Set<String> methods = new HashSet<>();
        if (paymentMethods == null || paymentMethods.trim().isEmpty()) {
            methods.addAll(Arrays.asList("CASH", "CREDIT", "CHEQUE"));
            return methods;
        }
        for (String method : paymentMethods.split(",")) {
            if (!TextUtils.isEmpty(method)) {
                methods.add(method.trim().toUpperCase());
            }
        }
        return methods;
    }
}

