package com.hfad.agencyapp.ui.customers;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.hfad.agencyapp.databinding.ActivityCustomerDetailBinding;
import com.hfad.agencyapp.ui.models.Customer;
import com.hfad.agencyapp.viewmodel.CustomerViewModel;

/**
 * Activity that displays detailed information about a selected customer.
 */
public class CustomerDetailActivity extends AppCompatActivity {
    private ActivityCustomerDetailBinding binding;
    private CustomerViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Customer Details");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        String customerId = getIntent().getStringExtra("customer_id");
        if (customerId != null) {
            loadCustomerDetails(customerId);
        }
    }

    private void loadCustomerDetails(String customerId) {
        Customer customer = viewModel.getById(customerId);
        if (customer != null) {
            displayCustomerDetails(customer);
        }
    }

    private void displayCustomerDetails(Customer customer) {
        // Business Information
        binding.tvBusinessName.setText(customer.getBusinessName() != null ? customer.getBusinessName() : "-");
        binding.tvContactPerson.setText(customer.getContactPerson() != null ? customer.getContactPerson() : "-");
        binding.tvAddress.setText(customer.getAddress() != null ? customer.getAddress() : "-");

        // Contact Information
        binding.tvPhone.setText(customer.getPhone() != null && !customer.getPhone().isEmpty() ? customer.getPhone() : "-");

        // Additional Details
        binding.tvBrNumber.setText(customer.getBrNumber() != null && !customer.getBrNumber().isEmpty() ? customer.getBrNumber() : "-");
        binding.tvIdNumber.setText(customer.getIdNumber() != null && !customer.getIdNumber().isEmpty() ? customer.getIdNumber() : "-");

        // Payment Methods
        String paymentMethods = formatPaymentMethods(customer.getPaymentMethods());
        binding.tvPaymentMethods.setText(paymentMethods);
    }

    private String formatPaymentMethods(String paymentMethods) {
        if (paymentMethods == null || paymentMethods.trim().isEmpty()) {
            return "Cash, Credit, Cheque";
        }
        // Convert CSV format (CASH,CREDIT,CHEQUE) to readable format
        String[] methods = paymentMethods.split(",");
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < methods.length; i++) {
            String method = methods[i].trim();
            // Capitalize first letter, lowercase rest
            formatted.append(method.substring(0, 1).toUpperCase())
                    .append(method.substring(1).toLowerCase());
            if (i < methods.length - 1) {
                formatted.append(", ");
            }
        }
        return formatted.toString();
    }
}


