# Quick Start Guide

Get the Agency Sales App running in 5 minutes.

## Prerequisites Checklist

- [ ] Android Studio installed (Hedgehog or later)
- [ ] Android SDK 36 installed
- [ ] Java 11+ installed
- [ ] Firebase account created
- [ ] Project cloned from Git on branch `samuditha`

## 1. Configure Firebase (2 min)

1. Go to https://console.firebase.google.com
2. Create new project → name: `AgencySalesApp`
3. Add Android app → package: `com.hfad.agencyapp`
4. Download `google-services.json`
5. Copy to `app/` directory (same folder as `build.gradle.kts`)

*See `FIREBASE_SETUP.md` for detailed steps.*

## 2. Update Top-level build.gradle.kts (1 min)

Open `build.gradle.kts` (project root) and update plugins:

```kotlin
plugins {
    id("com.google.gms.google-services") version "4.3.15" apply false
    alias(libs.plugins.android.application) apply false
}
```

Save file. Sync Gradle.

## 3. Add Google Services Plugin (1 min)

Open `app/build.gradle.kts` and add plugin to the plugins block:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")  // Add this line
}
```

Save. Sync Gradle.

## 4. Build and Install (1 min)

```bash
cd C:\Projects\agencyApp

# Clean and build
.\gradlew.bat clean build

# Install on device/emulator
.\gradlew.bat installDebug
```

Or in Android Studio: Run → Run 'app'

## 5. Test the App (Optional)

1. **Launch**: App opens → redirects to LoginActivity
2. **Sign Up**: Enter email `test@cake.com`, password `Test@123`
3. **Dashboard**: See product list (empty initially)
4. **Firebase**: Check console.firebase.google.com → Authentication (should see your user)

## Troubleshooting Quick Fixes

| Problem | Fix |
|---------|-----|
| Build fails: "cannot find symbol Product" | Run `./gradlew clean build` |
| `google-services.json` not found | Copy file to `app/` directory, restart IDE |
| Firebase sync fails | Check internet, verify `google-services.json` correct |
| App crashes on startup | Check logcat: `.\gradlew.bat logcat \| grep "Exception"` |

## Next Steps

- **Add Sample Data**: See `SAMPLE_DATA_INIT.md`
- **Configure Bluetooth**: See `BLUETOOTH_SETUP.md`
- **Deploy to Play Store**: See `DEPLOYMENT_GUIDE.md`
- **Full Documentation**: See `README_SETUP.md`

---

**Done!** Your app is ready. Proceed to Dashboard after signing in.

