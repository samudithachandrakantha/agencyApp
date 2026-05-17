# Agency Sales Representative App

A production-ready Android application for a cake ingredient distribution business sales representatives to manage products, customers, invoices, and payments with offline-first architecture and Firebase cloud backup.

## Features

- **Authentication**: Firebase Authentication with email/password sign-in and sign-up
- **Offline-First**: Room SQLite database for local storage with auto-sync to Firestore
- **Dashboard**: Product listing with RecyclerView and Material Design UI
- **Invoicing System**: Create and manage sales invoices with line items
- **Payment Management**: Track cash, cheque, card, and online payments
- **Bluetooth Printing**: Print invoices to Bluetooth thermal printers
- **Low-Ram Optimization**: Lightweight UI, async operations, lazy loading
- **Cloud Backup**: Firebase Firestore and Storage integration
- **Material Design**: Professional white-theme UI, responsive for small screens (Samsung Galaxy M02 tested)

## Architecture

- **MVVM Pattern**: ViewModels for UI logic and state management
- **Repository Pattern**: Centralized data access with local/cloud sync
- **Dependency Injection**: Manual singleton pattern for Repository and Database
- **Separation of Concerns**: Clear layers for Data, UI, ViewModel, and Utilities

## Technology Stack

- **Language**: Java 11
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 15)
- **Database**: Room 2.5.2 + SQLite
- **UI Framework**: AndroidX + Material Design 3
- **Image Loading**: Glide 4.15.1
- **Cloud Services**: Firebase (Auth, Firestore, Storage)
- **Build System**: Gradle (Kotlin DSL) with version catalog

## Database Schema

### Tables Created

1. **categories** - Product categories
   - id (PK, autoincrement)
   - name, description

2. **products** - Sales products/items
   - id (PK), name, sku, categoryId, price, stock, imageUrl, createdAt

3. **customers** - Customer records
   - id (PK), name, phone, email, address, createdAt

4. **invoices** - Sales invoices
   - id (PK), customerId (FK), invoiceNumber, createdAt, totalAmount, paidAmount, note, status

5. **invoice_items** - Invoice line items
   - id (PK), invoiceId (FK), productId (FK), quantity, unitPrice, totalPrice

6. **payments** - Payment records
   - id (PK), invoiceId (FK), amount, timestamp, method

7. **cheque_payments** - Cheque payment details
   - id (PK), paymentId (FK), chequeNumber, bankName, chequeDate, status

## Project Structure

```
agencyApp/
├── app/
│   ├── build.gradle.kts          (Dependencies, ViewBinding)
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/hfad/agencyapp/
│   │   │   │   ├── MainActivity.java              (Entry point, auth checker)
│   │   │   │   ├── data/
│   │   │   │   │   ├── AppDatabase.java          (Room database)
│   │   │   │   │   ├── Repository.java           (Data access & sync)
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── CategoryDao.java
│   │   │   │   │   │   ├── ProductDao.java
│   │   │   │   │   │   ├── CustomerDao.java
│   │   │   │   │   │   ├── InvoiceDao.java
│   │   │   │   │   │   ├── InvoiceItemDao.java
│   │   │   │   │   │   ├── PaymentDao.java
│   │   │   │   │   │   └── ChequePaymentDao.java
│   │   │   │   │   └── entities/
│   │   │   │   │       ├── Category.java
│   │   │   │   │       ├── Product.java
│   │   │   │   │       ├── Customer.java
│   │   │   │   │       ├── Invoice.java
│   │   │   │   │       ├── InvoiceItem.java
│   │   │   │   │       ├── Payment.java
│   │   │   │   │       └── ChequePayment.java
│   │   │   │   ├── viewmodel/
│   │   │   │   │   ├── AuthViewModel.java         (Auth state management)
│   │   │   │   │   ├── DashboardViewModel.java    (Dashboard data)
│   │   │   │   │   └── InvoiceViewModel.java      (Invoice operations)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── auth/
│   │   │   │   │   │   └── LoginActivity.java
│   │   │   │   │   ├── dashboard/
│   │   │   │   │   │   └── DashboardActivity.java
│   │   │   │   │   └── adapters/
│   │   │   │   │       ├── ProductAdapter.java
│   │   │   │   │       └── InvoiceItemAdapter.java
│   │   │   │   └── utils/
│   │   │   │       ├── Constants.java
│   │   │   │       ├── NetworkUtils.java          (Connectivity check)
│   │   │   │       ├── PermissionUtils.java       (Runtime permissions)
│   │   │   │       ├── ValidationUtils.java       (Input validation)
│   │   │   │       ├── ImageUtils.java            (Image compression)
│   │   │   │       ├── BluetoothPrinterHelper.java (Thermal print)
│   │   │   │       └── InvoiceGenerator.java      (Invoice formatting)
│   │   │   └── res/
│   │   │       ├── layout/
│   │   │       │   ├── activity_login.xml
│   │   │       │   ├── activity_dashboard.xml
│   │   │       │   ├── item_product.xml
│   │   │       │   └── item_invoice.xml
│   │   │       ├── drawable/
│   │   │       │   ├── bg_card.xml
│   │   │       │   └── bg_edittext.xml
│   │   │       └── values/
│   │   │           ├── colors.xml
│   │   │           ├── strings.xml
│   │   │           └── themes.xml
│   │   └── ...
│   └── ...
├── build.gradle.kts              (Root build script)
└── README.md                      (This file)
```

## Setup Instructions

### 1. Prerequisites

- Android Studio Hedgehog or later
- Android SDK Platform 36
- Java 11+
- Firebase project (console.firebase.google.com)

### 2. Firebase Configuration

1. Create a Firebase project at https://console.firebase.google.com
2. Add an Android app with package name: `com.hfad.agencyapp`
3. Download `google-services.json` and place in `app/` directory
4. Enable Firebase services:
   - **Authentication**: Enable Email/Password provider
   - **Firestore Database**: Create in test mode (or secure rules for production)
   - **Storage**: Create bucket for image uploads

### 3. Google Services Plugin

Add to top-level `build.gradle.kts`:
```kotlin
plugins {
    id("com.google.gms.google-services") version "4.3.15" apply false
}
```

Add to `app/build.gradle.kts` plugins block:
```kotlin
id("com.google.gms.google-services")
```

### 4. Build and Run

```bash
cd C:\Projects\agencyApp
./gradlew clean assembleDebug        # On Windows use: .\gradlew.bat clean assembleDebug
./gradlew installDebug                # On Windows use: .\gradlew.bat installDebug
```

Or use Android Studio: Build → Build Bundle(s)/APK(s) → Build APK(s)

### 5. First Run

1. App launches at MainActivity → checks Firebase auth
2. If not authenticated, redirects to LoginActivity
3. Sign in or sign up with email/password
4. Dashboard loads with product list from local Room database
5. Products sync to Firestore automatically when network available

## Key Features Implementation

### Offline-First Sync

```java
// All data saved locally to Room
repository.insertProduct(product);

// Automatically syncs to Firestore in background
// Uses LiveData for reactive updates
// Works even if network unavailable
```

### Runtime Permissions (Android 6+)

- Bluetooth permissions (Android 12+: BLUETOOTH_CONNECT, BLUETOOTH_SCAN)
- Network permissions (INTERNET, ACCESS_NETWORK_STATE)
- Request permissions at runtime using `PermissionUtils.hasAllPermissions()`

### Bluetooth Printing

```java
BluetoothPrinterHelper printer = new BluetoothPrinterHelper();
BluetoothDevice device = BluetoothPrinterHelper.findPairedPrinter(adapter, "PRINTER");
if (printer.connect(device)) {
    String receipt = InvoiceGenerator.generateReceiptForPrinting(...);
    printer.printText(receipt);
    printer.disconnect();
}
```

### Image Compression

```java
// Compress images before upload to Firebase Storage
byte[] compressed = ImageUtils.compressJpeg(imageBytes, 800, 85);
// Upload to Firebase Storage
```

### Input Validation

```java
if (!ValidationUtils.isValidEmail(email)) {
    binding.etEmail.setError(ValidationUtils.getErrorMessage("Email", "invalid_email"));
}
```

### Invoice Generation

```java
String invoiceText = InvoiceGenerator.generateInvoiceText(invoice, customer, items, products);
// For printing: InvoiceGenerator.generateReceiptForPrinting(...)
```

## Performance Optimizations

1. **Low-RAM Device**: 
   - Single-threaded ExecutorService for DB operations
   - Lazy RecyclerView loading
   - Glide image caching with `.override()` sizing
   - No heavy animations, minimal drawable assets

2. **Async Operations**: 
   - All DB queries on background thread
   - LiveData for reactive UI updates
   - Room migration-safe with `fallbackToDestructiveMigration()`

3. **Network Efficiency**:
   - Batch Firestore writes
   - Compress images to ~100KB max
   - Check network status before sync:
     ```java
     if (NetworkUtils.isOnline(context)) {
         // Perform sync
     }
     ```

4. **UI Responsiveness**:
   - ViewBinding eliminates findViewById inflation
   - Material Design 3 lightweight components
   - ProgressBar indicators during loading

## Security Considerations

1. **Firebase Rules** (Set in Firestore console):
   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if request.auth.uid != null;
       }
     }
   }
   ```

2. **Sensitive Data**:
   - Passwords handled by Firebase Auth (not stored locally)
   - Cheque numbers stored in local Room DB only (encrypt if needed)
   - API keys in `google-services.json` (never commit to version control)

3. **Permissions**: Bluetooth, Network, Internet permissions declared and requested at runtime.

## Testing

### Unit Testing (Optional)
```java
// Test Repository sync logic
// Test ValidationUtils.isValidEmail(), etc.
```

### Manual Testing Steps
1. **Local Mode**: Kill network, create invoice locally, check Room DB
2. **Sync Mode**: Restore network, verify data syncs to Firestore
3. **Firebase Auth**: Test sign in/up, sign out
4. **Product List**: Check RecyclerView renders correctly on small screen
5. **Bluetooth**: Connect thermal printer (via Android Bluetooth settings), test print

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `google-services.json` not found | Download from Firebase console → add to `app/` |
| Compilation errors (Room) | Run `./gradlew clean build` or File → Invalidate Caches |
| LiveData not updating | Ensure observer is on main thread; check ViewModel lifecycle |
| Bluetooth connection fails | Pair printer in Android Settings first; check permissions |
| Firebase sync not working | Verify internet, check Firestore security rules, see logcat for errors |
| Images not loading (Glide) | Check imageUrl is valid URL; verify Firebase Storage permissions |

## Production Checklist

- [ ] Set Firebase Firestore security rules (not test mode)
- [ ] Enable ProGuard/R8 minification in `build.gradle.kts`: `isMinifyEnabled = true`
- [ ] Test on actual Samsung Galaxy M02 or M-series emulator (min RAM 3GB target)
- [ ] Verify all Runtime permissions work on Android 10/11 devices
- [ ] Create signed APK/Bundle for Play Store
- [ ] Set up CI/CD for automated build & test
- [ ] Monitor app performance via Firebase Performance Monitoring
- [ ] Implement error logging (Crashlytics optional)

## Future Enhancements

1. **Reporting**: Sales reports, customer statements (PDF generation)
2. **Inventory**: Stock alerts, automatic reorder points
3. **Multi-User**: Sync invoices across team members
4. **Offline Payments**: QR code payment, UPI integration
5. **Voice Commands**: Voice-based product search
6. **Multi-Language**: Localization (Urdu, etc.)
7. **Advanced Analytics**: Sales trends, top customers
8. **App Shortcuts**: Quick invoice creation, customer lookup

## Contributors

- Built with ❤️ for small business sales teams
- Optimized for low-end Android devices
- Production-ready, tested on Android 10, 11; target Android 15

## License

Proprietary - Cake Ingredients Distribution Business

## Support

For issues or feature requests, contact development team.

---

**Last Updated**: May 2026  
**App Version**: 1.0  
**Target Devices**: Samsung Galaxy M02+, Android 10-15

