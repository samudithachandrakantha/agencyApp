# Deployment Guide

Deploy the Agency Sales App to production devices and Google Play Store.

## Pre-Deployment Checklist

### Code Quality
- [ ] All code reviewed for security vulnerabilities
- [ ] No hardcoded credentials (API keys, passwords)
- [ ] No debug logs left in production code
- [ ] ProGuard/R8 minification enabled
- [ ] All unit/integration tests passing
- [ ] Firebase security rules set to production (not test mode)

### App Configuration
- [ ] Version code incremented (versionCode in build.gradle.kts)
- [ ] Version name updated (e.g., "1.0.0")
- [ ] Minimum SDK set to 24 (Android 7.0)
- [ ] Target SDK set to 36 (Android 15)
- [ ] App icon and splash screen finalized
- [ ] All strings localized (if needed)

### Testing
- [ ] Tested on Samsung Galaxy M02 (low-RAM device)
- [ ] Tested on Android 10, 11, 12, 13, 14, 15 devices/emulators
- [ ] Verified offline functionality (kill network, test local DB)
- [ ] Tested Firebase auth (sign up, sign in, sign out)
- [ ] Verified Bluetooth printer connection (if applicable)
- [ ] Checked battery usage (no excessive drains)
- [ ] Verified app size (<50MB ideal for low-end devices)

### Firebase Configuration
- [ ] Firestore security rules updated from test to production
- [ ] Storage security rules configured
- [ ] Firebase billing account set up (for production)
- [ ] Monitoring/logging configured (optional: Crashlytics)

### Store Preparation
- [ ] Google Play Developer account created ($25 one-time fee)
- [ ] App privacy policy written (required)
- [ ] Screenshots prepared (5+ per language)
- [ ] Feature graphic prepared (1024×500px)
- [ ] App description written
- [ ] Changelog prepared

## Step 1: Configure build.gradle.kts for Release

Edit `app/build.gradle.kts`:

```kotlin
android {
    // ...
    
    buildTypes {
        release {
            isMinifyEnabled = true  // Enable code shrinking
            isDebuggable = false
            isShrinkResources = true  // Remove unused resources
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

## Step 2: Update ProGuard Rules

Edit `app/proguard-rules.pro`:

```
# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Room database classes
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }

# Keep view binding classes
-keep class com.hfad.agencyapp.databinding.** { *; }

# Keep custom entity classes
-keep class com.hfad.agencyapp.data.entities.** { *; }

# Don't obfuscate models (needed for serialization)
-keepclassmembers class com.hfad.agencyapp.data.entities.** {
    public <methods>;
    public <fields>;
}

# Keep Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# General rules
-dontwarn java.lang.invoke.**
-dontwarn com.google.common.**
```

## Step 3: Update Version Information

Edit `app/build.gradle.kts`:

```kotlin
defaultConfig {
    // ...
    versionCode = 1          // Increment on each release (1, 2, 3, ...)
    versionName = "1.0.0"    // Semantic versioning (MAJOR.MINOR.PATCH)
}
```

Pattern:
- **Patch** (1.0.1): Bug fixes
- **Minor** (1.1.0): New features
- **Major** (2.0.0): Breaking changes

## Step 4: Build Release APK/Bundle

### Build APK (side-load on devices)

```bash
cd C:\Projects\agencyApp
.\gradlew.bat assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### Build App Bundle (for Play Store)

```bash
.\gradlew.bat bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

## Step 5: Sign the Build

Generate a signing key (one-time):

```bash
# Generate key using keytool
keytool -genkey -v -keystore agency_sales.keystore ^
  -keyalg RSA -keysize 2048 -validity 10000 ^
  -alias agency_sales_key

# Follow prompts for:
# - Password (e.g., SecurePass123!)
# - First and Last Name
# - Organization Unit, Organization
# - City/Locality, State/Province, Country
```

Configure signing in `app/build.gradle.kts`:

```kotlin
signingConfigs {
    release {
        storeFile = file("../agency_sales.keystore")
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "YOUR_PASSWORD"
        keyAlias = "agency_sales_key"
        keyPassword = System.getenv("KEY_PASSWORD") ?: "YOUR_PASSWORD"
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.release
        // ... other config
    }
}
```

Or use Android Studio GUI:
- Build → Generate Signed Bundle / APK
- Select "APK" or "Bundle"
- Follow wizard to sign

**Important**: Store keystore file safely and never commit to Git:

```bash
# Add to .gitignore
echo "*.keystore" >> .gitignore
```

## Step 6: Create Google Play Listing

1. Go to https://play.google.com/console
2. Create app → category: "Business" or "Productivity"
3. Fill in app details:
   - **Title**: "Agency Sales App"
   - **Description**: (max 4000 chars) Copy from README
   - **Privacy Policy URL**: (required; create on privacy policy generator)
   - **Permissions**: Explain Bluetooth, Network, Camera (if added later)

4. Upload screenshots (5+ per language):
   - Login screen
   - Dashboard
   - Invoice list
   - Product list
   - Payment screen

5. Create feature graphic (1024×500px):
   - App name + tagline
   - Design in Canva or GIMP

## Step 7: Release to alpha/beta Track (Optional)

Before public release, test with limited users:

1. Go to Play Console → App Releases
2. Create **Internal Testing** release → upload APK/AAB
3. Invite up to 100 internal testers
4. Collect feedback

Then:
- Go to **Closed Testing** → release to beta users (up to 1000)
- Go to **Production** → public release

## Step 8: Upload to Play Store

1. Play Console → App Releases → Production → Create new release
2. Upload signed `app-release.aab` (or APK)
3. Add release notes:
   ```
   Version 1.0.0 - Initial release
   - Authentication with Firebase
   - Offline product database
   - Invoice management
   - Payment tracking
   - Bluetooth printer support
   - Firestore cloud backup
   ```
4. Review and publish
5. Wait 1-2 hours for app to go live

## Step 9: Verify Deployment

1. **Check Play Store**: Search "Agency Sales App"
2. **Install on test device**: Download from Play Store
3. **Test core flows**:
   - Sign in → Dashboard → Create invoice → Print
4. **Monitor metrics**: Play Console → Acquisition, Crash rate

## Post-Deployment Monitoring

### Firebase Console

- **Authentication**: Monitor sign-ups, active users
- **Firestore**: Track database usage, optimize queries if needed
- **Storage**: Monitor image uploads, clean up old data
- **Crashes**: Set up Crashlytics to catch runtime errors

```bash
# View logs in real-time
adb logcat | grep "AgencyApp\|Exception"
```

### Android Studio Device Monitor

- **Memory usage**: Target <50MB on low-RAM devices
- **Battery drain**: Check background sync isn't excessive
- **Network**: Verify offline mode works

## Update Management

### Versioning Strategy

- **1.0.0** → "1.0.1" (hotfix): Bug fixes only
- **1.0.0** → "1.1.0" (minor): New features, backward compatible
- **1.0.0** → "2.0.0" (major): Breaking changes, database migration needed

### Push Updates via Play Store

Changes are automatic; users get updates within 24-48 hours.

### Graceful Updates

If data schema changes (e.g., new table added to Room):

```java
// In AppDatabase onUpgrade
public static final int DATABASE_VERSION = 2;

@Database(entities = {...}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    // Room handles migrations with .fallbackToDestructiveMigration()
}
```

## Rollback Plan

If critical bugs discovered after release:

1. **Immediate**: Create hotfix branch from release tag
2. **Fix & Build**: Push new version (1.0.1)
3. **Test**: Internal + beta testing (1-2 days)
4. **Release**: Update version code, republish to Play Store
5. **Communicate**: Notify users of critical fix

## Compliance & Legal

- [ ] Privacy Policy covers: data collection, Firebase, Bluetooth
- [ ] Terms of Service (if applicable)
- [ ] GDPR compliance (if EU users): data deletion, consent
- [ ] Payment processing (if in-app purchases added)
- [ ] Accessibility: WCAG 2.1 AA minimum

## Cost Estimates

| Item | Cost |
|------|------|
| Google Play Developer Account | $25 USD (one-time) |
| Firebase Firestore (free tier) | $0 (1GB/month) |
| Firebase Storage (free tier) | $0 (5GB/month) |
| Android signing certificate | $0 (self-signed) |

---

**Deployment Complete!** Your app is live on Google Play Store. Monitor metrics and plan next version features.

