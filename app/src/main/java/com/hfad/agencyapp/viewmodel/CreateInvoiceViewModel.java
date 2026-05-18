package com.hfad.agencyapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.hfad.agencyapp.data.Repository;
import com.hfad.agencyapp.ui.models.ChequeDetails;
import com.hfad.agencyapp.ui.models.Customer;
import com.hfad.agencyapp.ui.models.InvoiceItem;
import com.hfad.agencyapp.ui.models.PaymentType;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for Create Invoice screen.
 * Manages invoice items, totals, customer selection, and payment type.
 */
public class CreateInvoiceViewModel extends AndroidViewModel {

    // ...existing LiveData fields...
    private final MutableLiveData<List<InvoiceItem>> itemsLiveData = new MutableLiveData<>(new ArrayList<>());
    
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
    
    // LiveData for cheque image path
    private final MutableLiveData<String> chequeImagePathLiveData = new MutableLiveData<>();

    public CreateInvoiceViewModel(@NonNull Application application) {
        super(application);
    }

    // ===== Getters for LiveData =====
    
    public LiveData<List<InvoiceItem>> getItems() {
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

    public LiveData<String> getChequeImagePath() {
        return chequeImagePathLiveData;
    }

    // ===== Item Management Methods =====

    /**
     * Add a new item to the invoice.
     */
    public void addItem(String productId, String productName, double unitPrice) {
        List<InvoiceItem> items = itemsLiveData.getValue();
        if (items != null) {
            items.add(new InvoiceItem(productId, productName, 1, unitPrice));
            itemsLiveData.setValue(new ArrayList<>(items));
            calculateTotals();
        }
    }

    /**
     * Remove an item from the invoice by index.
     */
    public void removeItem(int position) {
        List<InvoiceItem> items = itemsLiveData.getValue();
        if (items != null && position >= 0 && position < items.size()) {
            items.remove(position);
            itemsLiveData.setValue(new ArrayList<>(items));
            calculateTotals();
        }
    }

    /**
     * Update the quantity of an item.
     */
    public void updateQuantity(int position, int newQuantity) {
        List<InvoiceItem> items = itemsLiveData.getValue();
        if (items != null && position >= 0 && position < items.size()) {
            items.get(position).setQuantity(newQuantity);
            itemsLiveData.setValue(new ArrayList<>(items));
            calculateTotals();
        }
    }

    /**
     * Calculate subtotal, discount, and total.
     */
    private void calculateTotals() {
        List<InvoiceItem> items = itemsLiveData.getValue();
        double subtotal = 0.0;
        
        if (items != null) {
            for (InvoiceItem item : items) {
                subtotal += item.getLineTotal();
            }
        }
        
        double discount = discountLiveData.getValue() != null ? discountLiveData.getValue() : 0.0;
        double total = subtotal - discount;
        
        subtotalLiveData.setValue(subtotal);
        totalLiveData.setValue(Math.max(0, total)); // Ensure total is not negative
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

    /**
     * Set cheque image path.
     */
    public void setChequeImagePath(String imagePath) {
        chequeImagePathLiveData.setValue(imagePath);
        ChequeDetails details = chequeDetailsLiveData.getValue();
        if (details != null) {
            details.setChequeImagePath(imagePath);
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
        List<InvoiceItem> items = itemsLiveData.getValue();
        if (items == null || items.isEmpty()) {
            return "At least one item is required";
        }

        // Check cheque details if payment type is CHEQUE
        if (paymentTypeLiveData.getValue() == PaymentType.CHEQUE) {
            ChequeDetails details = chequeDetailsLiveData.getValue();
            if (details == null || !details.isValid()) {
                return "All cheque details (number, bank, date, image) are required";
            }
        }

        return ""; // Valid
    }

    /**
     * Save invoice (placeholder for repository call).
     */
    public void saveInvoice() {
        String validation = validateInvoice();
        if (!validation.isEmpty()) {
            // Return error - handled in Activity
            return;
        }
        // Call repository to save invoice in production
        // repository.saveInvoice(...)
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
        chequeImagePathLiveData.setValue(null);
    }
}




