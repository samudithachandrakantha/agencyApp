package com.hfad.agencyapp.ui.products;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.hfad.agencyapp.R;
import com.hfad.agencyapp.data.ProductRepository;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.databinding.ActivityProductDetailBinding;

import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ActivityProductDetailBinding binding;
    private ProductRepository repository;
    private long productId = -1;
    private Product product;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productId = getIntent().getLongExtra(ProductsActivity.EXTRA_PRODUCT_ID, -1);
        repository = new ProductRepository(this);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnEditProduct.setOnClickListener(v -> openEdit());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (productId > 0) {
            loadProduct();
        }
    }

    private void loadProduct() {
        new Thread(() -> {
            try {
                product = repository.getByIdAsync(productId).get();
            } catch (Exception e) {
                product = null;
            }
            if (product == null) {
                runOnUiThread(() -> {
                    Snackbar.make(binding.getRoot(), "Product not found", Snackbar.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }
            runOnUiThread(() -> {
                binding.toolbar.setTitle(product.name);
                binding.tvCurrentStock.setText(String.valueOf(product.stock));
                binding.tvTotalSold.setText("0");
                binding.tvRevenue.setText(String.format(Locale.getDefault(), "Rs. %.2f", 0.0));

                binding.tvBrand.setText(getString(R.string.product_detail_brand, safe(product.brand)));
                binding.tvCostPrice.setText(getString(R.string.product_detail_cost, product.costPrice));
                binding.tvPrice.setText(getString(R.string.product_detail_selling, product.sellingPrice));
                binding.tvDiscount.setText(getString(R.string.product_detail_discount, product.discountPercent));
                if (product.buyQtyForFreeIssue > 0 && product.freeIssueQty > 0) {
                    binding.tvFreeIssue.setText(getString(R.string.product_detail_free_issue_format,
                            product.buyQtyForFreeIssue, product.freeIssueQty));
                } else {
                    binding.tvFreeIssue.setText(R.string.product_detail_free_issue_none);
                }
            });
        }).start();
    }

    private void openEdit() {
        Intent intent = new Intent(this, AddEditProductActivity.class);
        intent.putExtra(ProductsActivity.EXTRA_PRODUCT_ID, productId);
        startActivity(intent);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }
}
