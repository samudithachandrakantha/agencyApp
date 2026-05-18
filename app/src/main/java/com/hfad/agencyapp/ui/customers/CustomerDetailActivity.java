package com.hfad.agencyapp.ui.customers;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.hfad.agencyapp.databinding.ActivityCustomerDetailBinding;

/**
 * Simple customer detail activity placeholder. Full implementation with ViewPager2 can be added later.
 */
public class CustomerDetailActivity extends AppCompatActivity {
    private ActivityCustomerDetailBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        String id = getIntent().getStringExtra("customer_id");
        binding.tvTitle.setText("Customer Details");
        // TODO: load details using ViewModel
    }
}

