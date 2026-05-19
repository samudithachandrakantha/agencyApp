package com.hfad.agencyapp.ui.products;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.hfad.agencyapp.R;
import com.hfad.agencyapp.data.entities.Category;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.databinding.ActivityAddEditProductBinding;
import com.hfad.agencyapp.viewmodel.ProductViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddEditProductActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = ProductsActivity.EXTRA_PRODUCT_ID;
    private static final int REQ_CAMERA = 301;
    private static final int REQ_CAMERA_PERMISSION = 302;
    private static final int REQ_GALLERY = 303;
    private static final int MAX_IMAGE_SIZE = 1024;
    private static final int JPEG_QUALITY = 80;

    private ActivityAddEditProductBinding binding;
    private ProductViewModel viewModel;
    private long productId = -1;
    private final List<Category> categories = new ArrayList<>();
    private String imagePath;

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
            getSupportActionBar().setTitle(productId > 0 ? "Edit Product" : "Add Product");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupDropdowns();
        setupWatchers();
        setupImagePicker();
        setupSave();
        loadProductIfNeeded();
    }

    private void setupDropdowns() {
        String[] units = new String[]{"kg", "g", "L", "ml", "pcs", "box", "dozen"};
        binding.etUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, units));
        binding.etUnit.setText("pcs", false);

        viewModel.getCategories().observe(this, cats -> {
            categories.clear();
            if (cats != null) categories.addAll(cats);
            List<String> names = new ArrayList<>();
            for (Category category : categories) names.add(category.name);
            binding.etCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names));
            if (productId > 0) {
                fillCategoryFromExisting();
            }
        });
    }

    private void setupWatchers() {
        TextWatcher marginWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateProfitMargin(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.etSellingPrice.addTextChangedListener(marginWatcher);
        binding.etCostPrice.addTextChangedListener(marginWatcher);

        binding.ivProductImage.setOnClickListener(v -> showImageSourceDialog());
    }

    private void setupImagePicker() {
        // no-op; handled via dialog
    }

    private void setupSave() {
        binding.btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void loadProductIfNeeded() {
        if (productId <= 0) return;
        new Thread(() -> {
            Product product = viewModel.getById(productId);
            if (product == null) return;
            runOnUiThread(() -> {
                binding.etProductName.setText(product.name);
                binding.etSku.setText(product.sku);
                binding.etBrand.setText(product.brand);
                binding.etDescription.setText(product.description);
                binding.etSellingPrice.setText(product.sellingPrice > 0 ? String.valueOf(product.sellingPrice) : "");
                binding.etMrp.setText(product.mrp > 0 ? String.valueOf(product.mrp) : "");
                binding.etCostPrice.setText(product.costPrice > 0 ? String.valueOf(product.costPrice) : "");
                binding.etTaxRate.setText(product.taxRate > 0 ? String.valueOf(product.taxRate) : "");
                binding.etStock.setText(String.valueOf(product.stock));
                binding.etLowStock.setText(String.valueOf(product.lowStockThreshold > 0 ? product.lowStockThreshold : 20));
                binding.etLocation.setText(product.location);
                binding.etUnit.setText(product.unit, false);
                imagePath = product.imagePath;
                if (imagePath != null && !imagePath.isEmpty()) {
                    binding.ivProductImage.setImageURI(Uri.parse("file://" + imagePath));
                }
                fillCategoryFromExisting();
                updateProfitMargin();
            });
        }).start();
    }

    private void fillCategoryFromExisting() {
        if (productId <= 0 || categories.isEmpty()) return;
        Product p = viewModel.getById(productId);
        if (p == null) return;
        for (Category category : categories) {
            if (category.id == p.categoryId) {
                binding.etCategory.setText(category.name, false);
                break;
            }
        }
    }

    private void showImageSourceDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Image")
                .setItems(new CharSequence[]{"Camera", "Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQ_CAMERA);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQ_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @Nullable String[] permissions, @Nullable int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA_PERMISSION) {
            if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;

        try {
            if (requestCode == REQ_CAMERA) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    imagePath = saveBitmap(bitmap);
                    if (imagePath != null) binding.ivProductImage.setImageURI(Uri.parse("file://" + imagePath));
                }
            } else if (requestCode == REQ_GALLERY) {
                Uri uri = data.getData();
                if (uri != null) {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imagePath = saveBitmap(bitmap);
                    if (imagePath != null) binding.ivProductImage.setImageURI(Uri.parse("file://" + imagePath));
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveBitmap(Bitmap bitmap) throws IOException {
        Bitmap scaled = scaleDown(bitmap);
        File dir = new File(getFilesDir(), "products");
        if (!dir.exists() && !dir.mkdirs()) return null;
        File file = new File(dir, "product_" + UUID.randomUUID() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
            fos.flush();
        }
        return file.getAbsolutePath();
    }

    private Bitmap scaleDown(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float ratio = (float) width / (float) height;
        if (width > height) {
            width = MAX_IMAGE_SIZE;
            height = Math.round(width / ratio);
        } else {
            height = MAX_IMAGE_SIZE;
            width = Math.round(height * ratio);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private void updateProfitMargin() {
        double selling = parseDouble(binding.etSellingPrice.getText() != null ? binding.etSellingPrice.getText().toString() : "");
        double cost = parseDouble(binding.etCostPrice.getText() != null ? binding.etCostPrice.getText().toString() : "");
        if (selling > 0 && cost > 0) {
            double profit = selling - cost;
            double margin = (profit / selling) * 100.0;
            binding.tvProfitMargin.setText(String.format(Locale.getDefault(), "Profit Margin: %.1f%%", margin));
        } else {
            binding.tvProfitMargin.setText("Profit Margin: -");
        }
    }

    private void saveProduct() {
        String name = text(binding.etProductName);
        String sku = text(binding.etSku);
        String categoryName = text(binding.etCategory);
        String brand = text(binding.etBrand);
        String description = text(binding.etDescription);
        String sellingStr = text(binding.etSellingPrice);
        String mrpStr = text(binding.etMrp);
        String costStr = text(binding.etCostPrice);
        String taxStr = text(binding.etTaxRate);
        String stockStr = text(binding.etStock);
        String unit = text(binding.etUnit);
        String lowStockStr = text(binding.etLowStock);
        String location = text(binding.etLocation);

        boolean valid = true;
        if (name.isEmpty()) { binding.tilProductName.setError("Product name required"); valid = false; } else binding.tilProductName.setError(null);
        if (categoryName.isEmpty()) { binding.tilCategory.setError("Category required"); valid = false; } else binding.tilCategory.setError(null);
        if (sellingStr.isEmpty()) { binding.tilSellingPrice.setError("Selling price required"); valid = false; } else binding.tilSellingPrice.setError(null);
        if (stockStr.isEmpty()) { binding.tilStock.setError("Stock required"); valid = false; } else binding.tilStock.setError(null);
        if (unit.isEmpty()) { binding.tilUnit.setError("Unit required"); valid = false; } else binding.tilUnit.setError(null);
        if (!valid) return;

        double selling = parseDouble(sellingStr);
        double mrp = mrpStr.isEmpty() ? selling : parseDouble(mrpStr);
        double cost = parseDouble(costStr);
        double tax = parseDouble(taxStr);
        int stock = parseInt(stockStr);
        int lowStock = lowStockStr.isEmpty() ? 20 : parseInt(lowStockStr);

        if (mrp > 0 && selling > mrp) {
            binding.tilSellingPrice.setError("Selling price should be less than or equal to MRP");
            return;
        }

        long categoryId = getCategoryId(categoryName);
        if (categoryId <= 0) {
            binding.tilCategory.setError("Select a valid category");
            return;
        }

        Product product = new Product(name, sku.isEmpty() ? generateSku(name) : sku, categoryId, brand, description,
                selling, mrp, cost, tax, stock, unit, lowStock, location, imagePath == null ? "" : imagePath);
        if (productId > 0) {
            product.id = productId;
            Product existing = viewModel.getById(productId);
            if (existing != null) product.createdAt = existing.createdAt;
        }

        new Thread(() -> {
            boolean ok = viewModel.saveProduct(product);
            runOnUiThread(() -> {
                if (ok) {
                    Snackbar.make(binding.getRoot(), "Product saved", Snackbar.LENGTH_SHORT).show();
                    finish();
                } else {
                    Snackbar.make(binding.getRoot(), "Save failed", Snackbar.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private long getCategoryId(String categoryName) {
        for (Category category : categories) {
            if (category.name != null && category.name.equalsIgnoreCase(categoryName)) return category.id;
        }
        return -1;
    }

    private String generateSku(String name) {
        String clean = name.trim().toUpperCase(Locale.getDefault()).replaceAll("[^A-Z0-9]+", "");
        if (clean.length() > 6) clean = clean.substring(0, 6);
        return clean + "-" + System.currentTimeMillis() % 10000;
    }

    private String text(android.widget.TextView et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
}


