# Firebase Setup Guide

This guide provides step-by-step instructions to configure Firebase for the Agency Sales App.

## Step 1: Create Firebase Project

1. Go to https://console.firebase.google.com
2. Click "Create a project"
3. Enter project name: `AgencySalesApp` (or your preference)
4. Accept terms and create

## Step 2: Add Android App to Firebase

1. In Firebase Console, click "Add app" → Select "Android"
2. Enter package name: `com.hfad.agencyapp`
3. Enter app nickname: `Agency Sales App`
4. Download `google-services.json`
5. Place `google-services.json` in `app/` directory (same level as `build.gradle.kts`)

## Step 3: Update Gradle Build Files

### Top-level `build.gradle.kts`

```kotlin
plugins {
    id("com.google.gms.google-services") version "4.3.15" apply false
    alias(libs.plugins.android.application) apply false
}
```

### App-level `app/build.gradle.kts`

Already configured in the project. Verify:

```kotlin
plugins {
    id("com.android.application")
    id("com.google.gms.google-services")  // Add this line
}

dependencies {
    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:32.2.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
}
```

## Step 4: Enable Firebase Services

### Authentication

1. In Firebase Console, go to **Authentication**
2. Click "Get started"
3. Under "Sign-in providers", click **Email/Password**
4. Toggle "Enable" → Save
5. (Optional) Enable Anonymous sign-in for testing

### Firestore Database

1. Go to **Firestore Database**
2. Click "Create database"
3. Choose region closest to users (e.g., `us-central1`)
4. Select **Start in test mode** (for development; change to production rules later)
5. Create

### Cloud Storage

1. Go to **Storage**
2. Click "Get started"
3. Select region (same as Firestore)
4. Accept default rules
5. Create

## Step 5: Firestore Security Rules (Production)

Replace default rules with:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Authenticated users can read/write their own data
    match /{document=**} {
      allow read, write: if request.auth.uid != null;
    }
  }
}
```

Go to **Firestore Console** → **Rules** tab → Replace content → Publish.

## Step 6: Storage Security Rules (Production)

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

Go to **Storage** → **Rules** tab → Replace → Publish.

## Step 7: Sync Dependencies

In Android Studio:
1. File → Sync Now
2. Or from terminal: `./gradlew clean build` (Windows: `.\gradlew.bat clean build`)

## Step 8: Test Firebase Connection

Run the app:
```bash
./gradlew installDebug  # Windows: .\gradlew.bat installDebug
```

1. Sign up with test email (e.g., `test@example.com`)
2. Check Firebase Console → Authentication → Users (should see new user)
3. Create a sample invoice
4. Check Firestore → Collections (should see `products`, `invoices`, etc.)

## Firestore Collections Structure

The app creates these collections automatically:

### products
```json
{
  "id": 1,
  "name": "Flour - Premium",
  "sku": "FLR-001",
  "categoryId": 1,
  "price": 500.00,
  "stock": 100,
  "imageUrl": "gs://bucket/images/flour.jpg",
  "createdAt": 1705600000
}
```

### customers
```json
{
  "id": 1,
  "name": "ABC Bakery",
  "phone": "03001234567",
  "email": "contact@abcbakery.com",
  "address": "Karachi, Pakistan",
  "createdAt": 1705600000
}
```

### invoices
```json
{
  "id": 1,
  "customerId": 1,
  "invoiceNumber": "INV-2026-001",
  "createdAt": 1705600000,
  "totalAmount": 5000.00,
  "paidAmount": 2500.00,
  "note": "Partial payment",
  "status": "PENDING"
}
```

### invoiceItems
```json
{
  "id": 1,
  "invoiceId": 1,
  "productId": 1,
  "quantity": 10,
  "unitPrice": 500.00,
  "totalPrice": 5000.00
}
```

### payments
```json
{
  "id": 1,
  "invoiceId": 1,
  "amount": 2500.00,
  "timestamp": 1705600000,
  "method": "CASH"
}
```

### chequePayments
```json
{
  "id": 1,
  "paymentId": 1,
  "chequeNumber": "123456",
  "bankName": "HBL",
  "chequeDate": 1705600000,
  "status": "PENDING"
}
```

## Troubleshooting Firebase

| Issue | Solution |
|-------|----------|
| `google-services.json` not found | Verify file in `app/` directory; restart IDE |
| Auth fails "Invalid API key" | Check `google-services.json` matches Firebase project |
| Firestore writes fail | Check security rules (test mode vs production); see logcat |
| Images not uploading | Verify Firebase Storage rules; check URL scheme `gs://` |
| Offline data not syncing | Check internet connection; verify Repository sync logic in logcat |

## Monitoring & Debugging

### Firebase Console Insights

1. **Authentication**: Check active users, sign-up trends
2. **Firestore**: Monitor document reads/writes and costs
3. **Storage**: View uploaded images and bandwidth usage
4. **Logs**: Firestore Rules Playground to test queries

### Android Logcat

```bash
# View Firebase logs
./gradlew installDebug -i | grep "Firebase\|Firestore\|Auth"

# Or in Android Studio: Logcat → Filter "firebase"
```

### Sample Logcat Output

```
D/Repository: Synced to Firestore: products/1
D/AuthViewModel: User signed in: test@example.com
W/Repository: Failed to sync: customers/1 (Error: Permission denied)
```

## Cost Optimization

**Firebase Pricing (as of 2026)**:
- **Firestore**: First 1GB/month free; ~$0.06 per 100K reads
- **Storage**: First 5GB/month free; ~$0.02 per GB after
- **Authentication**: Free for up to 50K users/month

**Optimization Tips**:
1. Cache reads using LiveData (avoid redundant queries)
2. Batch writes (combine multiple documents in one update)
3. Delete old synced data from Firestore periodically
4. Use indexes for frequent queries

---

**Setup Complete!** Your app is now ready to use Firebase. Test by signing in and creating your first invoice.

