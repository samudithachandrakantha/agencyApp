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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.hfad.agencyapp.R;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.databinding.ActivityProductsBinding;
import com.hfad.agencyapp.ui.customers.CustomersActivity;
import com.hfad.agencyapp.ui.dashboard.DashboardActivity;
import com.hfad.agencyapp.ui.adapters.ProductAdapter;
import com.hfad.agencyapp.ui.insights.InsightsActivity;
import com.hfad.agencyapp.ui.invoice.InvoicesActivity;
import com.hfad.agencyapp.viewmodel.ProductViewModel;

import java.util.HashMap;
import java.util.Map;

public class ProductsActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";

    private ActivityProductsBinding binding;
    private ProductViewModel viewModel;
    private ProductAdapter adapter;
    private final Map<Integer, String> chipTagMap = new HashMap<>();

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
        setupStockFilters();

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
            adapter.submitList(list);
            binding.emptyState.getRoot().setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });
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

    private void setupStockFilters() {
        binding.chipGroupFilters.removeAllViews();
        chipTagMap.clear();
        addFilterChip(getString(R.string.filter_all), "ALL", true);
        addFilterChip(getString(R.string.filter_low_stock), "LOW", false);
        addFilterChip(getString(R.string.filter_out_of_stock), "OUT", false);

        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds == null || checkedIds.isEmpty()) {
                viewModel.setStockFilter(ProductViewModel.StockFilter.ALL);
                return;
            }
            String tag = chipTagMap.get(checkedIds.get(0));
            if ("LOW".equals(tag)) {
                viewModel.setStockFilter(ProductViewModel.StockFilter.LOW_STOCK);
            } else if ("OUT".equals(tag)) {
                viewModel.setStockFilter(ProductViewModel.StockFilter.OUT_OF_STOCK);
            } else {
                viewModel.setStockFilter(ProductViewModel.StockFilter.ALL);
            }
            updateChipStyles(checkedIds.get(0));
        });
    }


    private void updateChipStyles(int selectedChipId) {
        for (int i = 0; i < binding.chipGroupFilters.getChildCount(); i++) {
            View child = binding.chipGroupFilters.getChildAt(i);
            if (!(child instanceof Chip)) continue;
            Chip chip = (Chip) child;
            boolean selected = chip.getId() == selectedChipId;
            if (selected) {
                chip.setTextColor(getColor(R.color.white));
                chip.setChipBackgroundColorResource(R.color.navy_900);
            } else {
                chip.setTextColor(getColor(R.color.text_primary));
                chip.setChipBackgroundColorResource(R.color.white);
            }
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
        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if ("Edit".equals(title)) {
                openAddEditProduct(product.id);
                return true;
            } else if ("Delete".equals(title)) {
                confirmDelete(product);
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
            showSearchBar();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSearchBar() {
        binding.tilSearch.setVisibility(View.VISIBLE);
        binding.etSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
