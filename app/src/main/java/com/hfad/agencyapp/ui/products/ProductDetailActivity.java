package com.hfad.agencyapp.ui.products;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.hfad.agencyapp.R;
import com.hfad.agencyapp.data.ProductRepository;
import com.hfad.agencyapp.data.entities.Category;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.data.entities.StockMovement;
import com.hfad.agencyapp.databinding.ActivityProductDetailBinding;
import com.hfad.agencyapp.ui.adapters.StockMovementAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ActivityProductDetailBinding binding;
    private ProductRepository repository;
    private StockMovementAdapter adapter;
    private long productId = -1;
    private Product product;
    private final Map<Long, String> categoryNames = new HashMap<>();

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

        adapter = new StockMovementAdapter();
        binding.rvMovements.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMovements.setAdapter(adapter);

        loadCategories();

        loadProduct();
        binding.btnEditProduct.setOnClickListener(v -> openEdit());
        binding.btnAdjustStock.setOnClickListener(v -> showAdjustStockDialog());
    }

    private void loadCategories() {
        new Thread(() -> {
            try {
                List<Category> categories = repository.getAllCategoriesAsync().get();
                categoryNames.clear();
                if (categories != null) {
                    for (Category category : categories) {
                        categoryNames.put(category.id, category.name);
                    }
                }
                runOnUiThread(this::renderProduct);
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void renderProduct() {
        if (product != null) {
            binding.tvCategory.setText(getString(R.string.product_detail_category,
                    safe(categoryNames.get(product.categoryId))));
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
            List<StockMovement> movementList;
            try {
                movementList = repository.getRecentMovementsAsync(productId).get();
            } catch (Exception e) {
                movementList = new java.util.ArrayList<>();
            }
            final List<StockMovement> movements = movementList;
            runOnUiThread(() -> {
                binding.collapsingToolbar.setTitle(product.name);
                if (product.imagePath != null && !product.imagePath.isEmpty()) {
                    binding.ivProductImage.setImageURI(Uri.parse("file://" + product.imagePath));
                }
                binding.tvCurrentStock.setText(String.valueOf(product.stock));
                binding.tvTotalSold.setText("0");
                binding.tvRevenue.setText(String.format(Locale.getDefault(), "Rs. %.2f", 0.0));
                binding.tvSku.setText(getString(R.string.product_detail_sku, safe(product.sku)));
                binding.tvCategory.setText(getString(R.string.product_detail_category, safe(categoryNames.get(product.categoryId))));
                binding.tvBrand.setText(getString(R.string.product_detail_brand, safe(product.brand)));
                binding.tvUnit.setText(getString(R.string.product_detail_unit, safe(product.unit)));
                binding.tvPrice.setText(getString(R.string.product_detail_price, product.sellingPrice));
                binding.tvMrp.setText(getString(R.string.product_detail_mrp, product.mrp));
                binding.tvTaxRate.setText(getString(R.string.product_detail_tax, product.taxRate));
                binding.tvDescription.setText(getString(R.string.product_detail_description, safe(product.description)));
                adapter.submitList(movements);
            });
        }).start();
    }

    private void openEdit() {
        Intent intent = new Intent(this, AddEditProductActivity.class);
        intent.putExtra(ProductsActivity.EXTRA_PRODUCT_ID, productId);
        startActivity(intent);
    }

    private void showAdjustStockDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_adjust_stock, null);
        com.google.android.material.textfield.TextInputLayout tilQty = dialogView.findViewById(R.id.tilQuantity);
        com.google.android.material.textfield.MaterialAutoCompleteTextView etReason = dialogView.findViewById(R.id.etReason);
        com.google.android.material.textfield.TextInputEditText etQty = dialogView.findViewById(R.id.etQuantity);
        com.google.android.material.textfield.TextInputEditText etNotes = dialogView.findViewById(R.id.etNotes);
        android.widget.TextView tvCurrent = dialogView.findViewById(R.id.tvCurrentStock);
        com.google.android.material.button.MaterialButtonToggleGroup group = dialogView.findViewById(R.id.toggleStockType);
        String[] reasons = new String[]{"Purchase", "Sale", "Damage", "Return", "Correction"};
        etReason.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reasons));
        etReason.setText(reasons[0], false);
        tvCurrent.setText(getString(R.string.product_detail_current_stock, product.stock));
        group.check(R.id.btnAddStock);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Adjust Stock")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String qtyStr = etQty.getText() == null ? "" : etQty.getText().toString().trim();
            if (qtyStr.isEmpty()) { tilQty.setError("Quantity required"); return; }
            int qty = Integer.parseInt(qtyStr);
            boolean add = group.getCheckedButtonId() == R.id.btnAddStock;
            if (!add && qty > product.stock) {
                tilQty.setError("Cannot remove more than current stock");
                return;
            }
            tilQty.setError(null);
            new Thread(() -> {
                boolean ok;
                try {
                    ok = repository.adjustStockAsync(productId, add, qty,
                            etReason.getText() == null ? "" : etReason.getText().toString().trim(),
                            etNotes.getText() == null ? "" : etNotes.getText().toString().trim()).get();
                } catch (Exception e) {
                    ok = false;
                }
                boolean finalOk = ok;
                runOnUiThread(() -> {
                    Snackbar.make(binding.getRoot(), finalOk ? "Stock updated" : "Stock update failed", Snackbar.LENGTH_SHORT).show();
                    if (finalOk) loadProduct();
                });
            }).start();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }
}






