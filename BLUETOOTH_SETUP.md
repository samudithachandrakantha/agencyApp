# Bluetooth Thermal Printer Setup Guide

Configure and test Bluetooth printing for invoice receipts.

## Supported Printers

- **ESC/POS Thermal Printers**: Xprinter, Zebra, Sunmi, Winpos
- **Protocol**: RFCOMM (SPP - Serial Port Profile)
- **Connection**: Paired Bluetooth devices only
- **Format**: Plain text or basic ASCII formatting

## Hardware Setup

1. **Purchase a Bluetooth thermal printer** (e.g., Xprinter XP-365B)
2. **Charge/power on** the printer
3. **Note the printer name**: Usually printed on device (e.g., "XPRINTER")
4. **Pair with Android device**:
   - Settings → Bluetooth → Scan for devices
   - Select printer from list
   - Enter PIN (default often `0000` or `1234`)
   - Confirm pairing

## Software Configuration

### Step 1: Check Permissions

Permissions already added to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" /> <!-- Android 12+ -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" /> <!-- Android 12+ -->
```

### Step 2: Request Runtime Permissions (Android 6+)

```java
// In LoginActivity or DashboardActivity
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    String[] perms = PermissionUtils.getBluetoothPermissions();
    if (!PermissionUtils.hasAllPermissions(this, perms)) {
        ActivityCompat.requestPermissions(this, perms, 1001);
    }
}
```

### Step 3: Handle Permission Response

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
            Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show();
        }
    }
}
```

## Implementation Example

### Print Invoice Receipt

```java
// In DashboardActivity or InvoiceActivity
public void printInvoice(Invoice invoice, Customer customer, List<InvoiceItem> items) {
    // Check Bluetooth permissions
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    // Get Bluetooth adapter
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if (adapter == null) {
        Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
        return;
    }

    // Find paired printer
    BluetoothDevice printer = BluetoothPrinterHelper.findPrinterByName(adapter, "XPRINTER");
    if (printer == null) {
        Toast.makeText(this, "Printer not found. Pair in Bluetooth settings.", Toast.LENGTH_SHORT).show();
        return;
    }

    // Connect and print
    BluetoothPrinterHelper helper = new BluetoothPrinterHelper();
    if (helper.connect(printer)) {
        String receipt = InvoiceGenerator.generateReceiptForPrinting(
            invoice, customer, items, products);
        
        if (helper.printText(receipt)) {
            Toast.makeText(this, "Print successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Print failed", Toast.LENGTH_SHORT).show();
        }
        helper.disconnect();
    } else {
        Toast.makeText(this, "Cannot connect to printer", Toast.LENGTH_SHORT).show();
    }
}
```

### Add Print Button to Layout

Edit `activity_dashboard.xml`:

```xml
<Button
    android:id="@+id/btn_print"
    android:layout_width="wrap_content"
    android:layout_height="40dp"
    android:text="Print"
    android:layout_marginStart="4dp" />
```

In `DashboardActivity.java`:

```java
binding.btnPrint.setOnClickListener(v -> {
    // Get current invoice and print
    printInvoice(currentInvoice, currentCustomer, currentItems);
});
```

## Receipt Format

The `InvoiceGenerator.generateReceiptForPrinting()` produces:

```
  ===== CAKE INGREDIENTS SALES =====
       INVOICE RECEIPT

Invoice: INV-2026-001
Date: 17/05/2026 14:30
Customer: ABC Bakery

Items:
Flour - Premium      10 x 450.00
                        Total: 4500.00
Sugar - White        5 x 60.00
                        Total: 300.00

Total Amount: 4800.00
Paid Amount: 2500.00
Balance: 2300.00

Thank You!
```

## Customization

### Adjust Receipt Text

Edit `InvoiceGenerator.java`:

```java
public static String generateReceiptForPrinting(...) {
    StringBuilder sb = new StringBuilder();
    
    // Header (customize width for 58mm or 80mm paper)
    sb.append("==== YOUR COMPANY NAME ====\n");  // Adjust for printer width
    sb.append("       RECEIPT\n");
    
    // ... rest of formatting
}
```

### ESC/POS Commands (Advanced)

For advanced formatting (bold, underline, barcode):

```java
byte[] bold = {27, 69, 1};        // ESC E 1 (bold on)
byte[] normalFont = {27, 69, 0};  // ESC E 0 (bold off)
byte[] largeFont = {29, 33, 17};  // GS ! 17 (larger size)

os.write(bold);
os.write("Invoice #000001\n".getBytes());
os.write(normalFont);
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Connection fails | Verify printer is paired in Settings; check Bluetooth is ON |
| No text prints | Confirm printer is ready (green light); check paper supply |
| Garbled text | Printer baud rate mismatch; reconnect and try again |
| App crashes on print | Check runtime permissions are granted; verify logcat |
| Network interference | Move away from WiFi router/microwave; avoid congestion |

## Testing

### Manual Test

1. Pair printer via Settings → Bluetooth
2. In app, click "Print" button
3. Check receipt prints on thermal printer
4. Verify formatting (alignment, spacing)

### Automated Test (Optional)

```java
// Mock test
@Test
public void testPrinterConnection() {
    BluetoothPrinterHelper printer = new BluetoothPrinterHelper();
    assertFalse(printer.isConnected());
    // Mock BluetoothDevice and test connection logic
}
```

## Production Notes

- **Reliability**: Keep Bluetooth range <10m; avoid obstacles
- **Battery**: Printer battery lasts ~4-8 hours; charge daily
- **Maintenance**: Clean printer head monthly with alcohol swabs
- **Paper**: Use 58mm thermal paper (80mm also supported)
- **Cost**: Thermal paper ~Rs. 500/roll (1000 receipts)

---

**Done!** Your app can now print invoices. Test with paired printer.

