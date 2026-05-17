# Sample Data Initialization Guide

Initialize the app with sample products and customers for testing.

## Overview

This guide shows how to add sample data to the local Room database and Firebase from code or manually.

## Method 1: Programmatic Initialization (Recommended)

Create a helper class to insert sample data on first launch:

### DataInitializer.java

```java
package com.hfad.agencyapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.hfad.agencyapp.data.Repository;
import com.hfad.agencyapp.data.entities.Category;
import com.hfad.agencyapp.data.entities.Customer;
import com.hfad.agencyapp.data.entities.Product;

public class DataInitializer {

    private static final String PREF_NAME = "app_prefs";
    private static final String KEY_INITIALIZED = "data_initialized";

    public static void initializeIfNeeded(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
            initializeData(context);
            prefs.edit().putBoolean(KEY_INITIALIZED, true).apply();
        }
    }

    private static void initializeData(Context context) {
        Repository repo = Repository.getInstance(context);

        // Add Categories
        Category cat1 = new Category("Flour", "All types of flour");
        Category cat2 = new Category("Sugar & Sweeteners", "Sugar products");
        Category cat3 = new Category("Baking Powder", "Leavening agents");

        repo.insertCategory(cat1);
        repo.insertCategory(cat2);
        repo.insertCategory(cat3);

        // Add Products
        Product p1 = new Product("Flour - Premium", "FLR-001", 1, 450.00, 100, "");
        Product p2 = new Product("Flour - Standard", "FLR-002", 1, 350.00, 150, "");
        Product p3 = new Product("Sugar - White", "SUG-001", 2, 60.00, 200, "");
        Product p4 = new Product("Baking Powder - 100g", "BP-001", 3, 40.00, 300, "");
        Product p5 = new Product("Vanilla Extract", "VAN-001", 2, 300.00, 50, "");

        repo.insertProduct(p1);
        repo.insertProduct(p2);
        repo.insertProduct(p3);
        repo.insertProduct(p4);
        repo.insertProduct(p5);

        // Add Customers
        Customer c1 = new Customer("ABC Bakery", "03001234567", "contact@abcbakery.com", "Karachi");
        Customer c2 = new Customer("XYZ Confectionery", "03105555555", "info@xyzconf.com", "Lahore");
        Customer c3 = new Customer("Sweet Treats", "03219876543", "sales@sweettreats.com", "Islamabad");

        repo.insertCustomer(c1);
        repo.insertCustomer(c2);
        repo.insertCustomer(c3);
    }
}
```

### Usage in MainActivity or DashboardActivity

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    // Initialize sample data on first run
    DataInitializer.initializeIfNeeded(this);
    
    // ... rest of onCreate
}
```

## Method 2: Firestore Console (Manual)

If you prefer to add data directly via Firebase Console:

1. Go to https://console.firebase.google.com → Your Project → Firestore
2. Click "Create Collection" → name: `products`
3. Add first document with ID `1`:

```json
{
  "id": 1,
  "name": "Flour - Premium",
  "sku": "FLR-001",
  "categoryId": 1,
  "price": 450.00,
  "stock": 100,
  "imageUrl": "",
  "createdAt": 1705600000
}
```

4. Repeat for other collections: `categories`, `customers`, etc.

## Sample Data Sets

### Categories

| ID | Name | Description |
|----|------|-------------|
| 1 | Flour | All types of flour |
| 2 | Sugar & Sweeteners | Sugar products |
| 3 | Baking Powder | Leavening agents |
| 4 | Oils & Fats | Butter, oil, ghee |
| 5 | Dairy | Milk, cream, eggs |

### Products

| SKU | Name | Category | Price | Stock |
|-----|------|----------|-------|-------|
| FLR-001 | Flour - Premium | 1 | 450.00 | 100 |
| FLR-002 | Flour - Standard | 1 | 350.00 | 150 |
| SUG-001 | Sugar - White | 2 | 60.00 | 200 |
| BP-001 | Baking Powder - 100g | 3 | 40.00 | 300 |
| VAN-001 | Vanilla Extract | 2 | 300.00 | 50 |
| OIL-001 | Coconut Oil - 1L | 4 | 800.00 | 40 |
| GHEE-001 | Pure Ghee - 1L | 4 | 1200.00 | 30 |

### Customers

| Name | Phone | Email | Address |
|------|-------|-------|---------|
| ABC Bakery | 03001234567 | contact@abcbakery.com | Karachi |
| XYZ Confectionery | 03105555555 | info@xyzconf.com | Lahore |
| Sweet Treats | 03219876543 | sales@sweettreats.com | Islamabad |

## Testing Data

### Sample Invoice Creation

After adding sample data, create a test invoice:

1. Sign in to Dashboard
2. Click "New Invoice" (feature to be implemented)
3. Select customer: "ABC Bakery"
4. Add items:
   - 10 × Flour - Premium = Rs. 4,500
   - 5 × Sugar - White = Rs. 300
5. Total: Rs. 4,800
6. Pay: Rs. 2,500 (CASH)
7. Outstanding: Rs. 2,300

## Reset Data

To clear all data and start fresh:

1. **Local Database**:
   ```java
   // In Repository or DAO
   db.clearAllTables();  // Clears all Room tables
   ```

2. **Firebase Firestore**:
   - Go to Firestore Console
   - For each collection, select all documents
   - Delete

3. **iOS-style Reset**:
   - Uninstall app from device
   - Clear app cache in Settings
   - Reinstall

## Cost & Performance Notes

- Sample data (~50 products, 10 customers, 100 invoices) = ~500 Firestore operations
- First run uses ~2MB local storage in Room DB
- Firebase free tier covers this easily
- No API charges for test data

---

**Done!** Dashboard now shows products. Create your first invoice.

