package com.hfad.agencyapp.ui.products;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.hfad.agencyapp.R;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.databinding.ActivityAddEditProductBinding;
import com.hfad.agencyapp.viewmodel.ProductViewModel;

import java.util.Locale;
import java.util.UUID;

public class AddEditProductActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = ProductsActivity.EXTRA_PRODUCT_ID;

    private ActivityAddEditProductBinding binding;
    private ProductViewModel viewModel;
    private long productId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(productId > 0 ? getString(R.string.edit_product) : getString(R.string.add_product));
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSaveProduct.setOnClickListener(v -> saveProduct());
        loadProductIfNeeded();
    }

    private void loadProductIfNeeded() {
        if (productId <= 0) return;
        new Thread(() -> {
            Product product = viewModel.getById(productId);
            if (product == null) return;
            runOnUiThread(() -> {
                binding.etProductName.setText(product.name);
                binding.etBrand.setText(product.brand);
                binding.etCostPrice.setText(product.costPrice > 0 ? String.valueOf(product.costPrice) : "");
                binding.etSellingPrice.setText(product.sellingPrice > 0 ? String.valueOf(product.sellingPrice) : "");
                binding.etDiscount.setText(product.discountPercent > 0 ? String.valueOf(product.discountPercent) : "");
                binding.etBuyQtyForFree.setText(product.buyQtyForFreeIssue > 0 ? String.valueOf(product.buyQtyForFreeIssue) : "");
                binding.etFreeIssueQty.setText(product.freeIssueQty > 0 ? String.valueOf(product.freeIssueQty) : "");
                binding.etStock.setText(String.valueOf(product.stock));
            });
        }).start();
    }

    private void saveProduct() {
        String name = text(binding.etProductName);
        String brand = text(binding.etBrand);
        String costStr = text(binding.etCostPrice);
        String sellingStr = text(binding.etSellingPrice);
        String discountStr = text(binding.etDiscount);
        String buyForFreeStr = text(binding.etBuyQtyForFree);
        String freeQtyStr = text(binding.etFreeIssueQty);
        String stockStr = text(binding.etStock);

        clearErrors();
        boolean valid = true;
        if (name.isEmpty()) {
            binding.tilProductName.setError(getString(R.string.error_product_name_required));
            valid = false;
        }
        if (costStr.isEmpty()) {
            binding.tilCostPrice.setError(getString(R.string.error_cost_price_required));
            valid = false;
        }
        if (sellingStr.isEmpty()) {
            binding.tilSellingPrice.setError(getString(R.string.error_selling_price_required));
            valid = false;
        }
        if (stockStr.isEmpty()) {
            binding.tilStock.setError(getString(R.string.error_stock_level_required));
            valid = false;
        }
        if (!valid) return;

        double cost = parseDouble(costStr);
        double selling = parseDouble(sellingStr);
        if (cost < 0) {
            binding.tilCostPrice.setError(getString(R.string.error_invalid_amount));
            return;
        }
        if (selling < 0) {
            binding.tilSellingPrice.setError(getString(R.string.error_invalid_amount));
            return;
        }

        double discount = discountStr.isEmpty() ? 0 : parseDouble(discountStr);
        if (discount < 0 || discount > 100) {
            binding.tilDiscount.setError(getString(R.string.error_discount_range));
            return;
        }

        int buyForFree = buyForFreeStr.isEmpty() ? 0 : parseInt(buyForFreeStr);
        int freeQty = freeQtyStr.isEmpty() ? 0 : parseInt(freeQtyStr);
        if (buyForFree < 0 || freeQty < 0) {
            binding.tilBuyQtyForFree.setError(getString(R.string.error_invalid_quantity));
            return;
        }
        if (buyForFree > 0 && freeQty <= 0) {
            binding.tilFreeIssueQty.setError(getString(R.string.error_free_issue_qty_required));
            return;
        }
        if (freeQty > 0 && buyForFree <= 0) {
            binding.tilBuyQtyForFree.setError(getString(R.string.error_buy_qty_for_free_required));
            return;
        }

        int stock = parseInt(stockStr);
        if (stock < 0) {
            binding.tilStock.setError(getString(R.string.error_invalid_quantity));
            return;
        }

        new Thread(() -> {
            long categoryId = viewModel.resolveDefaultCategoryId();
            String sku = generateSku(name);
            Product product;
            if (productId > 0) {
                Product existing = viewModel.getById(productId);
                if (existing == null) {
                    runOnUiThread(() -> Toast.makeText(this, R.string.product_not_found, Toast.LENGTH_SHORT).show());
                    return;
                }
                product = existing;
                product.name = name;
                product.brand = brand;
                product.costPrice = cost;
                product.sellingPrice = selling;
                product.discountPercent = discount;
                product.buyQtyForFreeIssue = buyForFree;
                product.freeIssueQty = freeQty;
                product.stock = stock;
                if (product.sku == null || product.sku.isEmpty()) {
                    product.sku = sku;
                }
            } else {
                product = Product.createNew(name, brand, cost, selling, buyForFree, freeQty, discount, categoryId, sku);
                product.stock = stock;
            }

            boolean ok = viewModel.saveProduct(product);
            runOnUiThread(() -> {
                if (ok) {
                    Snackbar.make(binding.getRoot(), R.string.product_saved, Snackbar.LENGTH_SHORT).show();
                    finish();
                } else {
                    Snackbar.make(binding.getRoot(), R.string.product_save_failed, Snackbar.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void clearErrors() {
        binding.tilProductName.setError(null);
        binding.tilCostPrice.setError(null);
        binding.tilSellingPrice.setError(null);
        binding.tilDiscount.setError(null);
        binding.tilBuyQtyForFree.setError(null);
        binding.tilFreeIssueQty.setError(null);
        binding.tilStock.setError(null);
    }

    private String generateSku(String name) {
        String clean = name.trim().toUpperCase(Locale.getDefault()).replaceAll("[^A-Z0-9]+", "");
        if (clean.length() > 6) clean = clean.substring(0, 6);
        return clean + "-" + System.currentTimeMillis() % 10000;
    }

    private String text(@Nullable android.widget.TextView et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }
}
