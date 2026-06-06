package com.hfad.agencyapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.hfad.agencyapp.data.Repository;
import com.hfad.agencyapp.data.entities.Invoice;
import com.hfad.agencyapp.data.entities.InvoiceItem;
import com.hfad.agencyapp.data.entities.Payment;
import com.hfad.agencyapp.ui.models.ChequeDetails;
import com.hfad.agencyapp.ui.models.Customer;
import com.hfad.agencyapp.ui.models.PaymentType;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

/**
 * ViewModel for Create Invoice screen.
 * Manages invoice items, totals, customer selection, and payment type.
 */
public class CreateInvoiceViewModel extends AndroidViewModel {

    private final Repository repository;
    
    // ...existing LiveData fields...
    private final MutableLiveData<List<com.hfad.agencyapp.ui.models.InvoiceItem>> itemsLiveData = new MutableLiveData<>(new ArrayList<>());
    
    // LiveData for financial summaries
    private final MutableLiveData<Double> subtotalLiveData = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> discountLiveData = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> totalLiveData = new MutableLiveData<>(0.0);
    
    // LiveData for customer selection
    private final MutableLiveData<Customer> selectedCustomerLiveData = new MutableLiveData<>();
    
    // LiveData for payment type
    private final MutableLiveData<PaymentType> paymentTypeLiveData = new MutableLiveData<>(PaymentType.CASH);
    
    // LiveData for cheque details (only used when payment type is CHEQUE)
    private final MutableLiveData<ChequeDetails> chequeDetailsLiveData = new MutableLiveData<>(new ChequeDetails());

    public CreateInvoiceViewModel(@NonNull Application application) {
        super(application);
        this.repository = Repository.getInstance(application);
    }

    // ===== Getters for LiveData =====
    
    public LiveData<List<com.hfad.agencyapp.ui.models.InvoiceItem>> getItems() {
        return itemsLiveData;
    }

    public LiveData<Double> getSubtotal() {
        return subtotalLiveData;
    }

    public LiveData<Double> getDiscount() {
        return discountLiveData;
    }

    public LiveData<Double> getTotal() {
        return totalLiveData;
    }

    public LiveData<Customer> getSelectedCustomer() {
        return selectedCustomerLiveData;
    }

    public LiveData<PaymentType> getPaymentType() {
        return paymentTypeLiveData;
    }

    public LiveData<ChequeDetails> getChequeDetails() {
        return chequeDetailsLiveData;
    }

    public void setItems(List<com.hfad.agencyapp.ui.models.InvoiceItem> items) {
        itemsLiveData.setValue(items != null ? new ArrayList<>(items) : new ArrayList<>());
        calculateTotals();
    }

    // ===== Item Management Methods =====

    /**
     * Add a new item to the invoice.
     */
    public void addItem(String productId, String productName, double unitPrice) {
        addItem(productId, productName, unitPrice, 0.0, 0, 0);
    }

    /**
     * Add a new item to the invoice with discount percentage.
     */
    public void addItem(String productId, String productName, double unitPrice, double discountPercent) {
        addItem(productId, productName, unitPrice, discountPercent, 0, 0);
    }

    public void addItem(String productId, String productName, double unitPrice, double discountPercent,
                        int freeIssueBuyQty, int freeIssueBonusQty) {
        List<com.hfad.agencyapp.ui.models.InvoiceItem> current = itemsLiveData.getValue();
        List<com.hfad.agencyapp.ui.models.InvoiceItem> next = current != null ? new ArrayList<>(current) : new ArrayList<>();

        // If product already exists in invoice, increase quantity instead of duplicating row.
        int existingIndex = -1;
        for (int i = 0; i < next.size(); i++) {
            com.hfad.agencyapp.ui.models.InvoiceItem row = next.get(i);
            if (row.getProductId() != null && row.getProductId().equals(productId)) {
                existingIndex = i;
                break;
            }
        }

        if (existingIndex >= 0) {
            com.hfad.agencyapp.ui.models.InvoiceItem existing = next.get(existingIndex);
            next.set(existingIndex, new com.hfad.agencyapp.ui.models.InvoiceItem(
                    existing.getProductId(),
                    existing.getProductName(),
                    existing.getQuantity() + 1,
                    existing.getUnitPrice(),
                    existing.getDiscountPercent(),
                    existing.getFreeIssueBuyQty(),
                    existing.getFreeIssueBonusQty()
            ));
        } else {
                next.add(new com.hfad.agencyapp.ui.models.InvoiceItem(
                    productId,
                    productName,
                    1,
                    unitPrice,
                    discountPercent,
                    freeIssueBuyQty,
                    freeIssueBonusQty
                ));
        }

        itemsLiveData.setValue(next);
        calculateTotals();
    }

    /**
     * Remove an item from the invoice by index.
     */
    public void removeItem(int position) {
        List<com.hfad.agencyapp.ui.models.InvoiceItem> current = itemsLiveData.getValue();
        if (current != null && position >= 0 && position < current.size()) {
            List<com.hfad.agencyapp.ui.models.InvoiceItem> next = new ArrayList<>(current);
            next.remove(position);
            itemsLiveData.setValue(next);
            calculateTotals();
        }
    }

    /**
     * Update the quantity of an item.
     */
    public void updateQuantity(int position, int newQuantity) {
        List<com.hfad.agencyapp.ui.models.InvoiceItem> current = itemsLiveData.getValue();
        if (current != null && position >= 0 && position < current.size()) {
            List<com.hfad.agencyapp.ui.models.InvoiceItem> next = new ArrayList<>(current);
            com.hfad.agencyapp.ui.models.InvoiceItem old = next.get(position);
            next.set(position, new com.hfad.agencyapp.ui.models.InvoiceItem(
                    old.getProductId(),
                    old.getProductName(),
                    newQuantity,
                    old.getUnitPrice(),
                    old.getDiscountPercent(),
                    old.getFreeIssueBuyQty(),
                    old.getFreeIssueBonusQty()
            ));
            itemsLiveData.setValue(next);
            calculateTotals();
        }
    }

    /**
     * Update the discount percentage of an item.
     */
    public void updateDiscount(int position, double newDiscountPercent) {
        List<com.hfad.agencyapp.ui.models.InvoiceItem> current = itemsLiveData.getValue();
        if (current != null && position >= 0 && position < current.size()) {
            List<com.hfad.agencyapp.ui.models.InvoiceItem> next = new ArrayList<>(current);
            com.hfad.agencyapp.ui.models.InvoiceItem old = next.get(position);
            next.set(position, new com.hfad.agencyapp.ui.models.InvoiceItem(
                    old.getProductId(),
                    old.getProductName(),
                    old.getQuantity(),
                    old.getUnitPrice(),
                    newDiscountPercent,
                    old.getFreeIssueBuyQty(),
                    old.getFreeIssueBonusQty()
            ));
            itemsLiveData.setValue(next);
            calculateTotals();
        }
    }

    /**
     * Calculate subtotal, discount, and total.
     */
    private void calculateTotals() {
        List<com.hfad.agencyapp.ui.models.InvoiceItem> items = itemsLiveData.getValue();
        double subtotal = 0.0;
        double totalDiscount = 0.0;
        
        if (items != null) {
            for (com.hfad.agencyapp.ui.models.InvoiceItem item : items) {
                subtotal += item.getQuantity() * item.getUnitPrice();
                totalDiscount += item.getDiscount();
            }
        }
        
        double total = subtotal - totalDiscount;
        
        subtotalLiveData.setValue(subtotal);
        discountLiveData.setValue(totalDiscount);
        totalLiveData.setValue(Math.max(0, total));
    }

    // ===== Customer Management =====

    /**
     * Set the selected customer.
     */
    public void setSelectedCustomer(Customer customer) {
        selectedCustomerLiveData.setValue(customer);
    }

    // ===== Payment Type Management =====

    /**
     * Set the payment type.
     */
    public void setPaymentType(PaymentType paymentType) {
        paymentTypeLiveData.setValue(paymentType);
    }

    // ===== Cheque Details Management =====

    /**
     * Update cheque number.
     */
    public void setChequeNumber(String chequeNumber) {
        ChequeDetails details = chequeDetailsLiveData.getValue();
        if (details != null) {
            details.setChequeNumber(chequeNumber);
            chequeDetailsLiveData.setValue(details);
        }
    }

    /**
     * Update bank name.
     */
    public void setBankName(String bankName) {
        ChequeDetails details = chequeDetailsLiveData.getValue();
        if (details != null) {
            details.setBankName(bankName);
            chequeDetailsLiveData.setValue(details);
        }
    }

    /**
     * Update cheque date.
     */
    public void setChequeDate(java.util.Date chequeDate) {
        ChequeDetails details = chequeDetailsLiveData.getValue();
        if (details != null) {
            details.setChequeDate(chequeDate);
            chequeDetailsLiveData.setValue(details);
        }
    }

    // ===== Validation & Submission =====

    /**
     * Validate invoice before saving.
     * Returns empty string if valid, or error message if not.
     */
    public String validateInvoice() {
        // Check customer selection
        if (selectedCustomerLiveData.getValue() == null) {
            return "Please select a customer";
        }

        // Check at least one item
        List<com.hfad.agencyapp.ui.models.InvoiceItem> items = itemsLiveData.getValue();
        if (items == null || items.isEmpty()) {
            return "At least one item is required";
        }

        // Check cheque details if payment type is CHEQUE
        if (paymentTypeLiveData.getValue() == PaymentType.CHEQUE) {
            ChequeDetails details = chequeDetailsLiveData.getValue();
            if (details == null || !details.isValid()) {
                return "Cheque number, bank name, and date are required";
            }
        }

        return ""; // Valid
    }

    /**
     * Save invoice to database with all items and payment information.
     */
    public void saveInvoice() {
        saveInvoice(null, null);
    }

    public void saveInvoice(Long existingInvoiceId) {
        saveInvoice(existingInvoiceId, null);
    }

    public void saveInvoice(Long existingInvoiceId, String existingInvoiceNumber) {
        String validation = validateInvoice();
        if (!validation.isEmpty()) {
            // Return error - handled in Activity
            return;
        }

        // Get invoice data from current state
        Customer customer = selectedCustomerLiveData.getValue();
        List<com.hfad.agencyapp.ui.models.InvoiceItem> uiItems = itemsLiveData.getValue();
        Double total = totalLiveData.getValue();
        PaymentType paymentType = paymentTypeLiveData.getValue();
        ChequeDetails chequeDetails = chequeDetailsLiveData.getValue();

        if (customer == null || uiItems == null || uiItems.isEmpty() || total == null) {
            return;
        }

        long timestamp = System.currentTimeMillis();
        String invoiceNumber = existingInvoiceNumber != null && !existingInvoiceNumber.isEmpty()
            ? existingInvoiceNumber
            : "INV-" + (timestamp / 1000);

        // Try to parse customer ID as long, otherwise use 0
        long customerId = 0;
        try {
            customerId = Long.parseLong(customer.getId());
        } catch (NumberFormatException | NullPointerException e) {
            // If customer ID is a UUID string, leave customerId as 0
            // The customerName will be used for display instead
        }

        // Get customer name/business name
        String customerName = customer.getBusinessName() != null ? customer.getBusinessName() : customer.getContactPerson();

        String paymentMethodName = paymentType != null ? paymentType.name() : "CASH";
        boolean isCashPayment = "CASH".equals(paymentMethodName);
        
        // Get cheque date if it's a cheque payment
        long chequeMillis = 0;
        if (chequeDetails != null && chequeDetails.getChequeDate() != null) {
            chequeMillis = chequeDetails.getChequeDate().getTime();
        }

        // Create Invoice entity
        Invoice invoice = new Invoice(
                customerId,
                customerName,
                invoiceNumber,
                timestamp,
                total,
                isCashPayment ? total : 0,  // cash is paid immediately
                "",  // note
                isCashPayment ? "COMPLETED" : "PENDING",  // status
                paymentMethodName,
                chequeMillis
        );

        if (existingInvoiceId != null && existingInvoiceId > 0) {
            invoice.id = existingInvoiceId;
            repository.updateInvoice(invoice);
            repository.deleteInvoiceChildren(existingInvoiceId);
        } else {
            repository.insertInvoice(invoice);
        }

        // Capture values for background thread (must be effectively final)
        final String _paymentMethodName = paymentMethodName;
        final long _chequeMillis = chequeMillis;
        final String _invoiceNumber = invoiceNumber;

        new Thread(() -> {
            try {
                Thread.sleep(existingInvoiceId != null && existingInvoiceId > 0 ? 300 : 500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            for (com.hfad.agencyapp.ui.models.InvoiceItem uiItem : uiItems) {
                double totalPrice = uiItem.getQuantity() * uiItem.getUnitPrice() - uiItem.getDiscount();
                int freeIssueUnits = uiItem.getFreeIssueUnits();
                InvoiceItem dbItem = new InvoiceItem(
                        invoice.id,
                        Long.parseLong(uiItem.getProductId()),
                        uiItem.getQuantity(),
                        uiItem.getUnitPrice(),
                    totalPrice,
                    uiItem.getFreeIssueBuyQty(),
                    uiItem.getFreeIssueBonusQty(),
                    freeIssueUnits
                );
                repository.insertInvoiceItem(dbItem);
            }

            Payment payment = new Payment(
                    invoice.id,
                    total,
                    timestamp,
                    paymentType != null ? paymentType.name() : "CASH"
            );
            repository.insertPayment(payment);
            // Schedule cheque notification if applicable
            if ("CHEQUE".equalsIgnoreCase(_paymentMethodName) && _chequeMillis > 0) {
                try {
                    scheduleChequeNotification(getApplication(), _chequeMillis, _invoiceNumber);
                } catch (Exception ignored) { }
            }
        }).start();

        // Clear invoice for next entry
        clearInvoice();
    }

    /**
     * Clear all invoice data (for new invoice).
     */
    public void clearInvoice() {
        itemsLiveData.setValue(new ArrayList<>());
        subtotalLiveData.setValue(0.0);
        discountLiveData.setValue(0.0);
        totalLiveData.setValue(0.0);
        selectedCustomerLiveData.setValue(null);
        paymentTypeLiveData.setValue(PaymentType.CASH);
        chequeDetailsLiveData.setValue(new ChequeDetails());
    }

    private void scheduleChequeNotification(Context context, long chequeMillis, String invoiceNumber) {
        // Compute desired notification date/time
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(chequeMillis);

        int dow = cal.get(Calendar.DAY_OF_WEEK);
        int addDays = 1;
        // If cheque date is Friday or Saturday, use +3 days (per user's rules)
        if (dow == Calendar.FRIDAY || dow == Calendar.SATURDAY) {
            addDays = 3;
        }

        cal.add(Calendar.DATE, addDays);
        // Set time to 14:30
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long triggerAt = cal.getTimeInMillis();
        long now = System.currentTimeMillis();
        long delay = Math.max(1000L, triggerAt - now);

        // Persist a friendly text for profile UI
        SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        String friendly = "Cheque reminder scheduled on " + fmt.format(cal.getTime());
        SharedPreferences prefs = context.getSharedPreferences("cheque_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("next_cheque_notification_text", friendly).apply();

        // Enqueue WorkManager one-time request
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(com.hfad.agencyapp.workers.ChequeNotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(context).enqueue(req);
    }
}




