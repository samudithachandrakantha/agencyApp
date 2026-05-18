# Create Invoice Screen - Implementation Guide

## Overview
Complete implementation of the "Create Invoice" screen for the Android Sales Representative App using **MVVM Architecture** with **ViewBinding** and **Material Design 3**.

## Files Created

### XML Layouts
1. **`activity_create_invoice.xml`** (407 lines)
   - Main activity layout with CoordinatorLayout
   - Top app bar with back navigation
   - NestedScrollView for scrollable content
   - Customer selection card
   - Items card with RecyclerView
   - Payment type toggle group
   - Cheque details section (conditional visibility)
   - Bottom action bar with save button

2. **`item_row_invoice.xml`** (129 lines)
   - RecyclerView item layout for invoice items
   - Product name display
   - Quantity stepper (minus/plus buttons)
   - Unit price and line total calculation
   - Delete button for removing items

### Drawable Resources
1. **`bg_dashed_border.xml`** - Dashed border for cheque image upload area
2. **`bg_button_selected.xml`** - Selected button state styling
3. **`bg_button_unselected.xml`** - Unselected button state styling

### Java Classes

#### Models
1. **`PaymentType.java`** - Enum for payment types (CASH, CREDIT, CHEQUE)
2. **`InvoiceItem.java`** - Data model for invoice line items
3. **`ChequeDetails.java`** - Data model for cheque payment information
4. **`Customer.java`** - Data model for customer information

#### ViewModel
1. **`CreateInvoiceViewModel.java`** (271 lines)
   - Extends `AndroidViewModel`
   - Manages invoice items, totals, customer, and payment type
   - LiveData for reactive UI updates
   - Methods: addItem(), removeItem(), updateQuantity(), setPaymentType(), setChequeNumber/BankName/Date/ImagePath(), validateInvoice()

#### Adapter
1. **`InvoiceItemsAdapter.java`** (124 lines)
   - RecyclerView adapter with ViewBinding
   - Uses DiffUtil for efficient updates
   - OnItemActionListener interface for callback handling
   - Supports quantity increment/decrement and item deletion

#### Activity
1. **`CreateInvoiceActivity.java`** (452 lines)
   - Main activity with ViewBinding
   - Observes ViewModel LiveData
   - Handles:
     - Camera permissions and photo capture
     - Image compression and storage to internal directory (`/files/cheques/`)
     - Customer picker dialog
     - Product picker dialog
     - Date picker for cheque dates
     - Form validation with error display
     - Invoice preview and save workflow

### Resources

#### Strings (`strings.xml`)
- All UI text strings extracted to resources (32 strings)
- Full localization support ready

#### Dimensions (`dimens.xml`)
- Added `space_xxl` (80dp) for bottom padding

#### Colors (`colors.xml`)
- Added `error_red` color for validation errors

#### Manifest (`AndroidManifest.xml`)
- Added CreateInvoiceActivity declaration
- Added camera and storage permissions

## Key Features

### 1. Customer Selection
- Clickable row with chevron icon
- Opens dialog with mock customer list
- Validates customer is selected before save

### 2. Items Management
- Add items via product picker dialog
- Quantity stepper with +/- buttons
- Real-time line total calculation
- Delete items with confirmation
- Summary section showing Subtotal, Discount, Total

### 3. Payment Type Selection
- MaterialButtonToggleGroup with 3 options: Cash, Credit, Cheque
- Single selection required
- Cheque details section visibility tied to selection

### 4. Cheque Details (when Cheque selected)
- Cheque Number field
- Bank Name field
- Cheque Date with DatePicker (read-only)
- Cheque Image upload area with dashed border
- Camera capture with auto-compression
- Image scaling (max 1024px)
- JPEG compression (80% quality)
- Saved to internal storage: `/files/cheques/cheque_[timestamp].jpg`

### 5. Validation
- Customer required
- At least one item required
- If Cheque: number, bank, date, and image all required
- Validation errors shown via:
  - TextInputLayout.setError() for field-level errors
  - Snackbar for general errors
  - Error TextViews with error messages

### 6. Bottom Action Bar
- Fixed "Preview & Save" button
- Full-width dark blue button
- 48dp height for touch-friendly UX

## Architecture

### MVVM Pattern
```
CreateInvoiceActivity (View)
        ↓
CreateInvoiceViewModel (ViewModel)
        ↓
LiveData<T> (Observable State)
```

### Data Flow
1. User interacts with UI (CreateInvoiceActivity)
2. Activity calls ViewModel methods
3. ViewModel updates LiveData
4. Activity observes LiveData changes
5. UI automatically updates via observers

### ViewBinding
All layouts use ViewBinding for type-safe view access:
```java
binding = ActivityCreateInvoiceBinding.inflate(getLayoutInflater());
setContentView(binding.getRoot());
```

## Performance Optimizations

1. **DiffUtil in Adapter** - Efficient RecyclerView updates
2. **Image Compression** - Max 1024px with 80% JPEG quality
3. **NestedScrollView** - Smooth scrolling without main thread blocking
4. **Lazy Loading** - Product list loaded on demand in picker dialog
5. **Internal Storage** - Images stored locally, only paths saved to database

## Runtime Permissions

- **CAMERA** - For cheque photo capture
- **WRITE_EXTERNAL_STORAGE** - For older Android versions
- **READ_EXTERNAL_STORAGE** - For older Android versions

Permissions are requested at runtime with user-friendly UI feedback.

## Material Design 3 Components Used

1. **AppBarLayout** + **Toolbar** - Top app bar
2. **MaterialCardView** - Cards with elevation
3. **MaterialButton** - Standard and toggle buttons
4. **MaterialButtonToggleGroup** - Payment type selection
5. **TextInputLayout** + **TextInputEditText** - Form fields
6. **MaterialDatePicker** - Date selection
7. **Snackbar** - User feedback

## Testing Checklist

- [ ] Add customer via dialog
- [ ] Add multiple items with different quantities
- [ ] Modify quantities with +/- buttons
- [ ] Delete items from list
- [ ] Verify totals update correctly
- [ ] Select each payment type
- [ ] Capture cheque image with camera
- [ ] Verify image compression and storage
- [ ] Test all form validations
- [ ] Verify errors display correctly
- [ ] Test save with various states
- [ ] Back navigation works correctly

## Future Enhancements

1. **Implement actual customer picker** - Connect to database/Firebase
2. **Add product search** - Filter large product lists
3. **Implement preview screen** - Before final submission
4. **Add discount calculator** - Special pricing logic
5. **Payment terms** - Credit limits and terms
6. **Invoice templates** - Customize layout
7. **Recurring invoices** - Copy previous invoice
8. **Offline support** - Queue unsaved invoices
9. **Analytics** - Track invoice creation patterns
10. **Barcode scanning** - Quick product addition

## Known Limitations

- Mock customer/product lists - Replace with real data
- Image upload only from camera - Add gallery option
- No invoice preview screen yet - Plan for future
- No network sync yet - Add Firebase integration
- Cheque image stored locally only - Add backup strategy

## Dependencies

- AndroidX libraries (ViewBinding, LifeCycle)
- Material Design 3 (com.google.android.material)
- RecyclerView with DiffUtil
- Camera Intent (built-in)

## Color Scheme

- **Primary**: Navy Blue (#0B2A5B)
- **Background**: Light Slate (#F1F5F9)
- **Surface**: White (#FFFFFF)
- **Accent Green**: #22A06B
- **Accent Blue**: #2F6FED
- **Error**: Holo Red Light

All colors defined in `colors.xml` for consistency.

