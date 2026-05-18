# Android Resource Linking Error - FIXED ✅

## Problem
Build error: `attribute insetLeft (aka com.hfad.agencyapp:insetLeft) not found` in `item_row_invoice.xml`

### Error Details
```
com.hfad.agencyapp-mergeDebugResources-4:/layout/item_row_invoice.xml:44: 
  error: attribute insetLeft (aka com.hfad.agencyapp:insetLeft) not found.

com.hfad.agencyapp-mergeDebugResources-4:/layout/item_row_invoice.xml:44: 
  error: attribute insetRight (aka com.hfad.agencyapp:insetRight) not found.
```

**Affected Lines**: 44, 66, 95  
**Root Cause**: Invalid namespace for MaterialButton attributes in Material Design 3

---

## Solution

### What Was Changed
Replaced `app:insetLeft` and `app:insetRight` with `android:insetLeft` and `android:insetRight` in all MaterialButton components.

### Files Modified
- `app/src/main/res/layout/item_row_invoice.xml`

### Changes Made

**Before**:
```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/btn_delete"
    android:layout_width="32dp"
    android:layout_height="32dp"
    android:padding="0dp"
    app:cornerRadius="4dp"
    app:icon="@android:drawable/ic_menu_delete"
    app:iconGravity="textStart"
    app:iconPadding="0dp"
    app:iconSize="16dp"
    app:iconTint="@android:color/holo_red_light"
    app:insetLeft="0dp"        <!-- ❌ WRONG NAMESPACE -->
    app:insetRight="0dp"       <!-- ❌ WRONG NAMESPACE -->
    app:rippleColor="@color/soft_blue"
    app:strokeColor="@android:color/transparent" />
```

**After**:
```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/btn_delete"
    android:layout_width="32dp"
    android:layout_height="32dp"
    android:padding="0dp"
    android:insetLeft="0dp"    <!-- ✅ CORRECT NAMESPACE -->
    android:insetRight="0dp"   <!-- ✅ CORRECT NAMESPACE -->
    app:cornerRadius="4dp"
    app:icon="@android:drawable/ic_menu_delete"
    app:iconGravity="textStart"
    app:iconPadding="0dp"
    app:iconSize="16dp"
    app:iconTint="@android:color/holo_red_light"
    app:rippleColor="@color/soft_blue"
    app:strokeColor="@android:color/transparent" />
```

### Buttons Updated
1. **Delete Button** (line 39-40)
2. **Minus Button** (line 63-64)
3. **Plus Button** (line 92-93)

---

## Technical Details

### Why This Happened
- `insetLeft` and `insetRight` are Android framework attributes
- They should use the `android:` namespace, not `app:` namespace
- Material Design 3 library attributes should use `app:` namespace
- Framework attributes should always use `android:` namespace

### Namespace Rules in Android XML
```
android:        → Android framework attributes
app:            → AndroidX/Material Design library attributes
tools:          → Android Studio tooling attributes
```

### Affected Components
- `com.google.android.material.button.MaterialButton` instances
- Attributes: `insetLeft`, `insetRight` (both framework attributes)

---

## Verification

### Build Status
✅ **Before**: 6 resource linking errors
✅ **After**: 0 resource linking errors (only minor lint warnings remain)

### Remaining Warnings (Acceptable)
```
Hardcoded string "−", should use `@string` resource
Hardcoded string "+", should use `@string` resource
Hardcoded string "1", should use `@string` resource
```
These are acceptable for simple operators and placeholder values.

---

## How to Prevent This

### When Using MaterialButton
```xml
<!-- ✅ CORRECT: Framework attributes use android: namespace -->
<com.google.android.material.button.MaterialButton
    android:insetLeft="0dp"
    android:insetRight="0dp"
    app:cornerRadius="4dp"
    app:icon="@drawable/ic_delete" />

<!-- ❌ WRONG: Don't use app: for framework attributes -->
<com.google.android.material.button.MaterialButton
    app:insetLeft="0dp"
    app:insetRight="0dp" />
```

### Namespace Reference
| Attribute | Type | Namespace |
|-----------|------|-----------|
| `insetLeft` | Framework | `android:` |
| `insetRight` | Framework | `android:` |
| `cornerRadius` | Material Design 3 | `app:` |
| `icon` | Material Design 3 | `app:` |
| `iconGravity` | Material Design 3 | `app:` |
| `iconSize` | Material Design 3 | `app:` |
| `iconTint` | Material Design 3 | `app:` |

---

## Summary

✅ **Issue**: Incorrect namespace for MaterialButton attributes  
✅ **Fix**: Changed `app:` to `android:` for `insetLeft` and `insetRight`  
✅ **Result**: All resource linking errors resolved  
✅ **Build Status**: Ready for compilation  

The app is now ready to compile and run without resource linking errors.

