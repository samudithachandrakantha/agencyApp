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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

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
            binding.etPhone.setText(c.getPhoneNumber());
            binding.etCity.setText(c.getCity());
            binding.etAddress.setText(c.getAddress());
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
        String business = binding.etBusinessName.getText().toString().trim();
        String contact = binding.etContactPerson.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String city = binding.etCity.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();

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
        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            binding.tilPhone.setError("Valid phone required");
            valid = false;
        } else {
            binding.tilPhone.setError(null);
        }
        if (TextUtils.isEmpty(city)) {
            binding.tilCity.setError("City required");
            valid = false;
        } else {
            binding.tilCity.setError(null);
        }

        if (!valid) return;

        Customer c = new Customer(customerId, business, contact, phone, address, city);
        boolean ok = viewModel.saveCustomer(c);
        if (ok) {
            Snackbar.make(binding.getRoot(), "Customer saved", Snackbar.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
        }
    }
}

