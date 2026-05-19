package com.hfad.agencyapp.ui.invoice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.hfad.agencyapp.databinding.ActivityCreateInvoiceBinding;
import com.hfad.agencyapp.ui.adapters.InvoiceItemsAdapter;
import com.hfad.agencyapp.ui.models.Customer;
import com.hfad.agencyapp.ui.models.PaymentType;
import com.hfad.agencyapp.viewmodel.CreateInvoiceViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for creating invoices.
 * Handles UI interactions, camera capture for cheque images, and validation.
 */
public class CreateInvoiceActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int MAX_IMAGE_SIZE = 1024;
    private static final int COMPRESSION_QUALITY = 80;

    private ActivityCreateInvoiceBinding binding;
    private CreateInvoiceViewModel viewModel;
    private InvoiceItemsAdapter adapter;
    private DecimalFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize ViewBinding
        binding = ActivityCreateInvoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(CreateInvoiceViewModel.class);
        
        // Setup formatters
        // Use a numeric pattern for DecimalFormat and prefix currency symbol when formatting
        currencyFormat = new DecimalFormat("#,##0.00");
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        // Setup UI components
        setupToolbar();
        setupRecyclerView();
        setupObservers();
        setupClickListeners();
        setupToggleGroup();
        setupTextInputListeners();
    }

    /**
     * Setup toolbar with back navigation.
     */
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(null);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Setup RecyclerView adapter for invoice items.
     */
    private void setupRecyclerView() {
        adapter = new InvoiceItemsAdapter(new InvoiceItemsAdapter.OnItemActionListener() {
            @Override
            public void onQuantityChange(int position, int newQuantity) {
                viewModel.updateQuantity(position, newQuantity);
            }

            @Override
            public void onRemove(int position) {
                viewModel.removeItem(position);
            }
        });

        binding.rvInvoiceItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvInvoiceItems.setAdapter(adapter);
    }

    /**
     * Setup LiveData observers.
     */
    private void setupObservers() {
        // Observe items list
        viewModel.getItems().observe(this, items -> {
            adapter.submitList(items);
            binding.tvNoItems.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Observe subtotal
        viewModel.getSubtotal().observe(this, subtotal -> 
            binding.tvSubtotal.setText(getString(com.hfad.agencyapp.R.string.amount_format, currencyFormat.format(subtotal)))
        );

        // Observe discount
        viewModel.getDiscount().observe(this, discount -> 
            binding.tvDiscount.setText(getString(com.hfad.agencyapp.R.string.amount_format, currencyFormat.format(discount)))
        );

        // Observe total
        viewModel.getTotal().observe(this, total -> 
            binding.tvTotal.setText(getString(com.hfad.agencyapp.R.string.amount_format, currencyFormat.format(total)))
        );

        // Observe selected customer
        viewModel.getSelectedCustomer().observe(this, customer -> {
            if (customer != null) {
                // Customer model now uses businessName/contactPerson
                binding.tvCustomerName.setText(customer.getBusinessName());
                binding.tvCustomerError.setVisibility(View.GONE);
                applyAllowedPaymentMethods(customer);
            } else {
                applyAllowedPaymentMethods(null);
            }
        });

        // Observe payment type
        viewModel.getPaymentType().observe(this, paymentType -> {
            boolean isCheque = paymentType == PaymentType.CHEQUE;
            binding.containerChequeDetails.setVisibility(isCheque ? View.VISIBLE : View.GONE);
        });

        // Observe cheque image path
        viewModel.getChequeImagePath().observe(this, imagePath -> {
            if (imagePath != null && !imagePath.isEmpty()) {
                binding.ivChequeThumbnail.setImageURI(Uri.parse("file://" + imagePath));
                binding.ivChequeThumbnail.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Setup click listeners for buttons and interactive elements.
     */
    private void setupClickListeners() {
        // Customer selection
        binding.customerRow.setOnClickListener(v -> openCustomerPicker());

        // Add item button
        binding.btnAddItem.setOnClickListener(v -> openProductPicker());

        // Cheque date picker
        binding.etChequeDate.setOnClickListener(v -> openDatePicker());

        // Cheque image upload
        binding.frameChecqueImage.setOnClickListener(v -> openCameraForCheque());

        // Save invoice button
        binding.btnSaveInvoice.setOnClickListener(v -> saveInvoice());
    }

    /**
     * Setup MaterialButtonToggleGroup for payment types.
     */
    private void setupToggleGroup() {
        binding.togglePaymentType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                PaymentType type;
                if (checkedId == binding.btnCash.getId()) {
                    type = PaymentType.CASH;
                } else if (checkedId == binding.btnCredit.getId()) {
                    type = PaymentType.CREDIT;
                } else {
                    type = PaymentType.CHEQUE;
                }
                viewModel.setPaymentType(type);
            }
        });
        applyAllowedPaymentMethods(viewModel.getSelectedCustomer().getValue());
    }

    /**
     * Setup text input listeners for cheque details.
     */
    private void setupTextInputListeners() {
        binding.etChequeNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setChequeNumber(s.toString());
                binding.tilChequeNumber.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etBankName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setBankName(s.toString());
                binding.tilBankName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Open customer picker dialog/activity.
     * TODO: Implement customer picker dialog or launch customer selection activity.
     */
    private void openCustomerPicker() {
        // Load customers from local DB repository and show in dialog
        com.hfad.agencyapp.data.CustomerRepository repo = new com.hfad.agencyapp.data.CustomerRepository(this);
        try {
            List<Customer> list = repo.getAllCustomersAsync().get();
            if (list == null || list.isEmpty()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("No customers")
                        .setMessage("No customers found. Please add a customer first.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }
            CharSequence[] names = new CharSequence[list.size()];
            for (int i = 0; i < list.size(); i++) names[i] = list.get(i).getBusinessName();

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Select Customer")
                    .setItems(names, (dialog, which) -> {
                        Customer selected = list.get(which);
                        viewModel.setSelectedCustomer(selected);
                    })
                    .show();
        } catch (java.util.concurrent.ExecutionException | InterruptedException e) {
            Snackbar.make(binding.getRoot(), "Failed to load customers", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void applyAllowedPaymentMethods(Customer customer) {
        Set<PaymentType> allowed = parseAllowedPaymentTypes(customer != null ? customer.getPaymentMethods() : null);

        binding.btnCash.setVisibility(allowed.contains(PaymentType.CASH) ? View.VISIBLE : View.GONE);
        binding.btnCredit.setVisibility(allowed.contains(PaymentType.CREDIT) ? View.VISIBLE : View.GONE);
        binding.btnCheque.setVisibility(allowed.contains(PaymentType.CHEQUE) ? View.VISIBLE : View.GONE);

        PaymentType current = viewModel.getPaymentType().getValue();
        if (current == null || !allowed.contains(current)) {
            PaymentType fallback = allowed.contains(PaymentType.CASH)
                    ? PaymentType.CASH
                    : (allowed.contains(PaymentType.CREDIT) ? PaymentType.CREDIT : PaymentType.CHEQUE);
            viewModel.setPaymentType(fallback);
            checkPaymentButton(fallback);
        } else {
            checkPaymentButton(current);
        }
    }

    private void checkPaymentButton(PaymentType type) {
        if (type == PaymentType.CASH) {
            binding.togglePaymentType.check(binding.btnCash.getId());
        } else if (type == PaymentType.CREDIT) {
            binding.togglePaymentType.check(binding.btnCredit.getId());
        } else if (type == PaymentType.CHEQUE) {
            binding.togglePaymentType.check(binding.btnCheque.getId());
        }
    }

    private Set<PaymentType> parseAllowedPaymentTypes(String paymentMethods) {
        Set<PaymentType> allowed = new HashSet<>();
        if (paymentMethods == null || paymentMethods.trim().isEmpty()) {
            allowed.addAll(Arrays.asList(PaymentType.CASH, PaymentType.CREDIT, PaymentType.CHEQUE));
            return allowed;
        }
        for (String token : paymentMethods.split(",")) {
            if (token == null || token.trim().isEmpty()) continue;
            try {
                allowed.add(PaymentType.valueOf(token.trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (allowed.isEmpty()) {
            allowed.addAll(Arrays.asList(PaymentType.CASH, PaymentType.CREDIT, PaymentType.CHEQUE));
        }
        return allowed;
    }

    /**
     * Open product picker dialog/activity.
     * TODO: Implement product picker with lazy loading.
     */
    private void openProductPicker() {
        // Placeholder: Show a dialog with mock products
        String[] products = {
            "Flour (1kg)",
            "Sugar (1kg)",
            "Butter (500g)",
            "Eggs (dozen)",
            "Baking Powder (100g)"
        };
        double[] prices = {150.0, 200.0, 350.0, 180.0, 45.0};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Product")
                .setItems(products, (dialog, which) -> {
                    viewModel.addItem(
                            "prod_" + which,
                            products[which],
                            prices[which]
                    );
                    Snackbar.make(binding.getRoot(), products[which] + " added", Snackbar.LENGTH_SHORT).show();
                })
                .show();
    }

    /**
     * Open date picker for cheque date selection.
     */
    private void openDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .build();
        
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Date selectedDate = new Date(selection);
            viewModel.setChequeDate(selectedDate);
            binding.etChequeDate.setText(dateFormat.format(selectedDate));
            binding.tilChequeDate.setError(null);
        });

        datePicker.show(getSupportFragmentManager(), "date_picker");
    }

    /**
     * Open camera to capture cheque image.
     */
    private void openCameraForCheque() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    /**
     * Launch camera intent.
     */
    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Save invoice with validation.
     */
    private void saveInvoice() {
        String error = viewModel.validateInvoice();
        
        if (!error.isEmpty()) {
            // Show error messages
            if (error.contains("customer")) {
                binding.tvCustomerError.setVisibility(View.VISIBLE);
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            } else if (error.contains("item")) {
                binding.tvItemsError.setVisibility(View.VISIBLE);
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            } else if (error.contains("cheque")) {
                binding.tvChequeError.setVisibility(View.VISIBLE);
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        } else {
            // Clear error messages
            binding.tvCustomerError.setVisibility(View.GONE);
            binding.tvItemsError.setVisibility(View.GONE);
            binding.tvChequeError.setVisibility(View.GONE);

            // Save invoice
            viewModel.saveInvoice();
            Snackbar.make(binding.getRoot(), "Invoice saved successfully", Snackbar.LENGTH_SHORT).show();
            
            // Navigate back or show preview
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                if (imageBitmap != null) {
                    // Compress and save image
                    String imagePath = compressAndSaveImage(imageBitmap);
                    if (imagePath != null) {
                        viewModel.setChequeImagePath(imagePath);
                        binding.tvChequeError.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    /**
     * Compress image and save to internal storage.
     * @param bitmap Original bitmap from camera
     * @return File path to compressed image, or null if failed
     */
    @Nullable
    private String compressAndSaveImage(Bitmap bitmap) {
        try {
            // Create cheques directory
            File chequeDir = new File(getFilesDir(), "cheques");
            if (!chequeDir.exists()) {
                if (!chequeDir.mkdir()) {
                    Toast.makeText(this, "Failed to create cheques directory", Toast.LENGTH_SHORT).show();
                    return null;
                }
            }

            // Scale down image if necessary
            Bitmap scaledBitmap = scaleDownBitmap(bitmap);

            // Save compressed image
            String filename = "cheque_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(chequeDir, filename);
            
            FileOutputStream fos = new FileOutputStream(imageFile);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, fos);
            fos.close();

            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * Scale down bitmap to a maximum size while maintaining aspect ratio.
     */
    private Bitmap scaleDownBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float aspectRatio = (float) width / height;

        if (width > height) {
            width = MAX_IMAGE_SIZE;
            height = (int) (width / aspectRatio);
        } else {
            height = MAX_IMAGE_SIZE;
            width = (int) (height * aspectRatio);
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
}











