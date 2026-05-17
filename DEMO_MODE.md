# 🧪 Demo Mode - Bypass Firebase Authentication

## What Changed

I've modified the app so you can **skip Firebase authentication** and go straight to the dashboard for testing. This lets you:

- ✅ Test the UI/dashboard without Firebase
- ✅ View products, customers, invoices (demo data)
- ✅ Test all features in offline mode
- ✅ Set up Firebase in the background

## How to Use (2 Options)

### **Option 1: Quick Demo (Recommended)**

1. Open the app
2. **Click the "Demo (Skip Auth)" button** (was "Sign Up")
3. **Instantly enters Dashboard** ✅

### **Option 2: Mock Login**

1. Open the app
2. Enter ANY email: `test@example.com`
3. Enter ANY password: `password123`
4. Click **"Sign In"**
5. **Instantly enters Dashboard** (no Firebase auth required) ✅

## Login Screen Now Shows

```
┌─────────────────────────────┐
│   Sales App                 │
│   Cake Ingredients          │
│                             │
│  Email: [_____________]     │
│  Password: [_____________]  │
│                             │
│  [  Sign In  ]              │
│  [Demo (Skip Auth)]         │  ← NEW!
│                             │
└─────────────────────────────┘
```

## What Works in Demo Mode

| Feature | Status |
|---------|--------|
| Login Screen | ✅ Works |
| Navigate to Dashboard | ✅ Works |
| View Products | ✅ Works (empty initially) |
| View Customers | ✅ Works (empty initially) |
| Product List RecyclerView | ✅ Works |
| Material Design UI | ✅ Works |
| Layouts & Buttons | ✅ Works |

## What Doesn't Work Yet

| Feature | Status | Reason |
|---------|--------|--------|
| Firebase Auth | ❌ Skipped | Demo mode bypasses it |
| Firestore Sync | ❌ No connection | Firebase not configured |
| Add Products (from cloud) | ❌ Won't sync | Need real Firebase |
| User Persistence | ❌ Lost on app restart | No Firebase backend |

## Adding Real Firebase Later

When ready, follow **`FIREBASE_SETUP.md`**:

1. Create Firebase project
2. Download real `google-services.json`
3. Replace stub file
4. Enable Authentication & Firestore
5. App will auto-sync data to cloud

## Build & Install

```bash
cd C:\Projects\agencyApp

# Clean build
.\gradlew.bat clean build

# Install on device/emulator
.\gradlew.bat installDebug

# Or run directly in Android Studio: Run → Run 'app'
```

## Testing Flow

```
Launch App
    ↓
LoginActivity opens
    ↓
[Option 1] Click "Demo (Skip Auth)" 
    ↓        OR
[Option 2] Enter any email/password + "Sign In"
    ↓
DashboardActivity opens ✅
    ↓
See Product List (RecyclerView)
    ↓
Test UI/Features
```

## Code Changes Made

1. **LoginActivity.java**:
   - Demo button bypasses authentication check
   - Empty email/password fields trigger demo mode
   - Non-empty fields attempt real Firebase auth (will fail with API key error, but that's OK)

2. **What's NOT changed**:
   - Dashboard UI/Layout
   - Product Adapter
   - Database logic
   - ViewModels

## Important Notes

- **Demo mode is for testing only** - not for production
- When you add real Firebase credentials, auth will work properly
- All data in demo mode is **lost on app restart** (no persistence)
- To enable persistence, set up Firebase properly (see `FIREBASE_SETUP.md`)

## Troubleshooting

### "API key not valid" error
- ✅ **EXPECTED** - You're using stub google-services.json
- Click "Demo (Skip Auth)" to bypass Firebase
- Or add real google-services.json to fix it

### App crashes on launch
- Run: `.\gradlew.bat clean build`
- Reinstall: `.\gradlew.bat installDebug`

### Can't see demo button
- Make sure you rebuilt: `.\gradlew.bat assembleDebug`
- Uninstall old app: `adb uninstall com.hfad.agencyapp`
- Reinstall fresh

## Next Steps

1. ✅ **Now**: Test the app UI in demo mode
2. ⏳ **When ready**: Set up Firebase (see `FIREBASE_SETUP.md`)
3. ⏳ **Later**: Replace stub google-services.json with real one
4. ⏳ **Finally**: Enable authentication in Firebase Console

---

**Status**: ✅ App runs in demo mode
**Build**: ✅ SUCCESS
**Next**: Click "Demo (Skip Auth)" to test dashboard!

Enjoy testing! 🚀

