package com.hfad.agencyapp.ui.products;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.hfad.agencyapp.R;
import com.hfad.agencyapp.data.entities.Category;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.databinding.ActivityProductsBinding;
import com.hfad.agencyapp.ui.adapters.ProductAdapter;
import com.hfad.agencyapp.viewmodel.ProductViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductsActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";

    private ActivityProductsBinding binding;
    private ProductViewModel viewModel;
    private ProductAdapter adapter;
    private final Map<Integer, String> chipTagMap = new HashMap<>();
    private final Map<Long, String> categoryNames = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Products");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        setupRecyclerView();
        setupObservers();
        setupSearch();
        setupStaticFilters();

        binding.fabAddProduct.setOnClickListener(v -> openAddEditProduct(null));
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onClick(Product product) {
                openDetail(product.id);
            }

            @Override
            public void onLongClick(Product product, View anchor) {
                showContextMenu(product, anchor);
            }

            @Override
            public void onEditClick(Product product) {
                openAddEditProduct(product.id);
            }
        });
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProducts.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getProducts().observe(this, list -> {
            if (list != null) {
                for (Product product : list) {
                    if (categoryNames.containsKey(product.categoryId)) {
                        product.categoryName = categoryNames.get(product.categoryId);
                    }
                }
            }
            adapter.submitList(list);
            binding.emptyState.getRoot().setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getCategories().observe(this, this::buildCategoryChips);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            private Runnable pending;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.etSearch.removeCallbacks(pending);
                pending = () -> viewModel.setSearchQuery(s.toString());
                binding.etSearch.postDelayed(pending, 300);
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupStaticFilters() {
        binding.chipGroupFilters.removeAllViews();
        chipTagMap.clear();
        addFilterChip("All", "ALL", true);
        addFilterChip("Low Stock", "LOW", false);
        addFilterChip("Out of Stock", "OUT", false);

        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds == null || checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            String tag = chipTagMap.get(id);
            if ("LOW".equals(tag)) {
                viewModel.setStockFilter(ProductViewModel.StockFilter.LOW_STOCK);
                viewModel.setCategoryFilter(-1L);
            } else if ("OUT".equals(tag)) {
                viewModel.setStockFilter(ProductViewModel.StockFilter.OUT_OF_STOCK);
                viewModel.setCategoryFilter(-1L);
            } else {
                viewModel.setStockFilter(ProductViewModel.StockFilter.ALL);
                String catTag = chipTagMap.get(id);
                if (catTag != null && catTag.startsWith("CAT:")) {
                    viewModel.setCategoryFilter(Long.parseLong(catTag.substring(4)));
                } else {
                    viewModel.setCategoryFilter(-1L);
                }
            }
        });
    }

    private void buildCategoryChips(List<Category> categories) {
        setupStaticFilters();
        for (Category category : categories) {
            categoryNames.put(category.id, category.name);
            addFilterChip(category.name, "CAT:" + category.id, false);
        }
    }

    private void addFilterChip(String text, String tag, boolean checked) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setChecked(checked);
        chip.setChipBackgroundColorResource(R.color.white);
        chip.setTextColor(getColor(R.color.text_primary));
        chip.setPaddingRelative(16, 8, 16, 8);
        int id = View.generateViewId();
        chip.setId(id);
        chip.setTag(tag);
        chipTagMap.put(id, tag);
        binding.chipGroupFilters.addView(chip);
        if (checked) {
            chip.setTextColor(getColor(R.color.white));
            chip.setChipBackgroundColorResource(R.color.navy_900);
            binding.chipGroupFilters.check(id);
        }
    }

    private void showContextMenu(Product product, View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenu().add("Edit");
        popupMenu.getMenu().add("Delete");
        popupMenu.getMenu().add("Adjust Stock");
        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if ("Edit".equals(title)) {
                openAddEditProduct(product.id);
                return true;
            } else if ("Delete".equals(title)) {
                confirmDelete(product);
                return true;
            } else if ("Adjust Stock".equals(title)) {
                showAdjustStockDialog(product);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void openDetail(long productId) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(EXTRA_PRODUCT_ID, productId);
        startActivity(intent);
    }

    private void openAddEditProduct(@Nullable Long productId) {
        Intent intent = new Intent(this, AddEditProductActivity.class);
        if (productId != null) intent.putExtra(EXTRA_PRODUCT_ID, productId);
        startActivity(intent);
    }

    private void confirmDelete(Product product) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Product")
                .setMessage("Delete " + product.name + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean ok = viewModel.deleteProduct(product.id);
                    Snackbar.make(binding.getRoot(), ok ? "Product deleted" : "Delete failed", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", v -> openAddEditProduct(product.id))
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAdjustStockDialog(Product product) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_adjust_stock, null);
        com.google.android.material.textfield.TextInputLayout tilQty = dialogView.findViewById(R.id.tilQuantity);
        com.google.android.material.textfield.TextInputLayout tilReason = dialogView.findViewById(R.id.tilReason);
        com.google.android.material.textfield.TextInputEditText etQty = dialogView.findViewById(R.id.etQuantity);
        com.google.android.material.textfield.MaterialAutoCompleteTextView etReason = dialogView.findViewById(R.id.etReason);
        com.google.android.material.textfield.TextInputEditText etNotes = dialogView.findViewById(R.id.etNotes);
        android.widget.TextView tvCurrent = dialogView.findViewById(R.id.tvCurrentStock);
        com.google.android.material.button.MaterialButtonToggleGroup group = dialogView.findViewById(R.id.toggleStockType);

        tvCurrent.setText("Current stock: " + product.stock);
        String[] reasons = new String[]{"Purchase", "Sale", "Damage", "Return", "Correction"};
        etReason.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reasons));
        etReason.setText(reasons[0], false);
        group.check(R.id.btnAddStock);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Adjust Stock")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String qtyStr = etQty.getText() != null ? etQty.getText().toString().trim() : "";
                String reason = etReason.getText() != null ? etReason.getText().toString().trim() : "";
                String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
                boolean add = group.getCheckedButtonId() == R.id.btnAddStock;
                if (qtyStr.isEmpty()) {
                    tilQty.setError("Quantity required");
                    return;
                }
                int qty = Integer.parseInt(qtyStr);
                if (qty <= 0) {
                    tilQty.setError("Quantity must be > 0");
                    return;
                }
                if (!add && qty > product.stock) {
                    tilQty.setError("Cannot remove more than current stock");
                    return;
                }
                tilQty.setError(null);
                boolean ok = viewModel.adjustStock(product.id, add, qty, reason, notes);
                Snackbar.make(binding.getRoot(), ok ? "Stock updated" : "Stock update failed", Snackbar.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_products, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.action_search) {
            binding.etSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT);
            return true;
        }
        if (id == R.id.action_filter) {
            binding.chipScroll.setVisibility(binding.chipScroll.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}




