# Common Usage & Code Snippets

Quick reference for common operations in the Agency Sales App.

## Adding a Product Programmatically

### From an Activity or ViewModel

```java
// In DashboardViewModel or any activity
private final DashboardViewModel viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

viewModel.insertProduct(
    "Flour - Premium",      // name
    "FLR-001",              // sku
    1,                      // categoryId
    450.00,                 // price
    100,                    // stock
    ""                      // imageUrl (empty or Firebase Storage URL)
);
```

### Directly via Repository

```java
Repository repo = Repository.getInstance(context);
Product product = new Product("Sugar - White", "SUG-001", 2, 60.00, 200, "");
repo.insertProduct(product);
```

## Creating an Invoice

```java
// Create invoice
viewModel.insertInvoice(
    customerId,             // e.g., 1
    "INV-2026-001",        // invoice number
    5000.00,               // total amount
    "Partial payment"      // note
);

// Add invoice items
viewModel.addInvoiceItem(
    invoiceId,             // from created invoice
    productId,             // e.g., 1 (Product id)
    10,                    // quantity
    500.00,                // unit price
    5000.00                // total price (qty x unit price)
);

// Record payment
viewModel.addPayment(
    invoiceId,
    2500.00,               // payment amount
    "CASH"                 // method: CASH, CHEQUE, CARD, ONLINE
);
```

## Handling Runtime Permissions

### Check Bluetooth Permissions

```java
String[] perms = PermissionUtils.getBluetoothPermissions();
if (PermissionUtils.hasAllPermissions(this, perms)) {
    // All permissions granted, proceed with Bluetooth
} else {
    // Request permissions
    ActivityCompat.requestPermissions(this, perms, 1001);
}
```

### Handle Permission Response

```java
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    
    if (requestCode == 1001) {
        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        
        if (allGranted) {
            // All permissions granted
            startBluetoothPrinting();
        }
    }
}
```

## Validation Examples

### Validate User Input

```java
String email = binding.etEmail.getText().toString().trim();
String password = binding.etPassword.getText().toString();
String phone = binding.etPhone.getText().toString();
String amount = binding.etAmount.getText().toString();

// Validate
if (!ValidationUtils.isValidEmail(email)) {
    binding.etEmail.setError("Invalid email format");
    return;
}

if (!ValidationUtils.isValidPassword(password)) {
    binding.etPassword.setError("Password must be at least 6 characters");
    return;
}

if (!ValidationUtils.isValidPhone(phone)) {
    binding.etPhone.setError("Phone must be at least 10 digits");
    return;
}

try {
    double amt = Double.parseDouble(amount);
    if (!ValidationUtils.isValidAmount(amt)) {
        binding.etAmount.setError("Amount must be greater than 0");
        return;
    }
} catch (NumberFormatException e) {
    binding.etAmount.setError("Invalid amount");
    return;
}

// All valid, proceed
```

## Network Checking

### Check Internet Availability Before Sync

```java
if (NetworkUtils.isOnline(getContext())) {
    // Network available, sync to Firebase
    repository.syncToFirestore();
} else {
    // Network unavailable, work with local data
    Toast.makeText(this, "Offline mode - changes will sync when online", Toast.LENGTH_SHORT).show();
}
```

## Image Compression

### Compress Image Before Upload

```java
// Load image file
byte[] imageBytes = Files.readAllBytes(imagePath);

// Compress
byte[] compressed = ImageUtils.compressJpeg(
    imageBytes,
    Constants.IMAGE_MAX_WIDTH,  // 800px
    Constants.IMAGE_QUALITY     // 85%
);

// Upload to Firebase Storage
FirebaseStorage.getInstance()
    .getReference("products/" + productId + ".jpg")
    .putBytes(compressed)
    .addOnSuccessListener(task -> {
        // Get download URL
        task.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();
            // Save to database with this URL
        });
    });
```

## Bluetooth Printing

### Print Invoice Receipt

```java
private void printInvoice() {
    // Check permissions
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    // Get Bluetooth adapter
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if (adapter == null) {
        Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
        return;
    }

    // Find printer
    BluetoothDevice printer = BluetoothPrinterHelper.findPrinterByName(adapter, "XPRINTER");
    if (printer == null) {
        Toast.makeText(this, "Printer not found", Toast.LENGTH_SHORT).show();
        return;
    }

    // Connect and print
    BluetoothPrinterHelper helper = new BluetoothPrinterHelper();
    if (helper.connect(printer)) {
        String receipt = InvoiceGenerator.generateReceiptForPrinting(
            invoice, customer, invoiceItems, products);
        
        if (helper.printText(receipt)) {
            Toast.makeText(this, "Printed successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Print failed", Toast.LENGTH_SHORT).show();
        }
        helper.disconnect();
    }
}
```

## Invoice Generation

### Generate Invoice Text

```java
// Generate for display
String invoiceText = InvoiceGenerator.generateInvoiceText(
    invoice,        // Invoice object
    customer,       // Customer object
    items,          // List<InvoiceItem>
    products        // List<Product> for lookup
);

// Display in TextView
binding.tvInvoice.setText(invoiceText);

// Generate for printing (58mm thermal paper)
String receipt = InvoiceGenerator.generateReceiptForPrinting(
    invoice, customer, items, products);
```

## LiveData Observation

### Observe Product List Changes

```java
dashboardViewModel.products.observe(this, products -> {
    if (products != null && !products.isEmpty()) {
        // Update adapter or UI
        adapter.submitList(products);
        binding.tvEmpty.setVisibility(View.GONE);
    } else {
        // No products
        binding.tvEmpty.setVisibility(View.VISIBLE);
        binding.tvEmpty.setText("No products found");
    }
});
```

### Observe Authentication State

```java
authViewModel.userLive.observe(this, user -> {
    if (user != null) {
        // User logged in
        Toast.makeText(this, "Welcome, " + user.getEmail(), Toast.LENGTH_SHORT).show();
    } else {
        // User logged out
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
});

authViewModel.errorMessage.observe(this, error -> {
    if (error != null && !error.isEmpty()) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }
});

authViewModel.loading.observe(this, isLoading -> {
    binding.progress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
});
```

## Database Queries

### Query Products by Category

```java
// Via DAO directly (from DB thread)
List<Product> categoryProducts = db.productDao().getByCategoryId(categoryId);

// Via Repository (with LiveData)
Repository repo = Repository.getInstance(context);
repo.getAllProducts().observe(this, allProducts -> {
    List<Product> filtered = allProducts.stream()
        .filter(p -> p.categoryId == categoryId)
        .collect(Collectors.toList());
    adapter.submitList(filtered);
});
```

### Query Customer by Name

```java
// Search functionality
String searchTerm = binding.etSearch.getText().toString();
List<Customer> results = db.customerDao().searchByName(searchTerm);
```

## Error Handling Best Practices

```java
// Wrap database operations
try {
    long id = repository.insertProduct(product);
    Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
} catch (Exception e) {
    Log.e("ProductError", "Failed to add product", e);
    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    // Show error dialog or retry option
}

// Wrap network operations
firestore.collection("products").get()
    .addOnSuccessListener(snapshot -> {
        // Handle success
    })
    .addOnFailureListener(e -> {
        Log.e("FirestoreError", "Query failed", e);
        Toast.makeText(this, "Failed to sync data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    });
```

## Data Synchronization

### Manual Sync Trigger

```java
public void manualSync() {
    if (!NetworkUtils.isOnline(context)) {
        Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
        return;
    }

    binding.progress.setVisibility(View.VISIBLE);

    // Trigger sync from Repository
    new Thread(() -> {
        try {
            // Sync all data
            List<Product> products = db.productDao().getAll();
            List<Customer> customers = db.customerDao().getAll();
            List<Invoice> invoices = db.invoiceDao().getAll();

            for (Product p : products) {
                firestore.collection("products").document(String.valueOf(p.id)).set(p);
            }
            for (Customer c : customers) {
                firestore.collection("customers").document(String.valueOf(c.id)).set(c);
            }
            for (Invoice i : invoices) {
                firestore.collection("invoices").document(String.valueOf(i.id)).set(i);
            }

            runOnUiThread(() -> {
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(context, "Sync complete", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            Log.e("SyncError", "Sync failed", e);
            runOnUiThread(() -> {
                binding.progress.setVisibility(View.GONE);
                Toast.makeText(context, "Sync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }).start();
}
```

## SnackBar Messages (Better UX)

```java
// Import
import com.google.android.material.snackbar.Snackbar;

// Show message
Snackbar.make(binding.getRoot(), "Invoice saved successfully", Snackbar.LENGTH_SHORT)
    .setAction("Undo", v -> {
        // Undo action
    })
    .setActionTextColor(Color.YELLOW)
    .show();

// Show error (longer duration)
Snackbar.make(binding.getRoot(), "Failed to save invoice", Snackbar.LENGTH_LONG)
    .setBackgroundTint(Color.RED)
    .show();
```

## Constants Usage

```java
// Instead of hardcoding values, use Constants:
String method = Constants.PAYMENT_METHOD_CASH;
String status = Constants.INVOICE_STATUS_PENDING;
int imageQuality = Constants.IMAGE_QUALITY;
int imageMaxWidth = Constants.IMAGE_MAX_WIDTH;

// For request codes
int permissionCode = Constants.PERMISSION_REQUEST_CODE;
int cameraCode = Constants.CAMERA_REQUEST_CODE;
```

---

**Note**: These are common patterns. Adapt to your specific use cases and always handle errors appropriately.

For more detailed information, refer to the main documentation files.

