package com.hfad.agencyapp.data;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.hfad.agencyapp.data.entities.Category;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.data.entities.Customer;
import com.hfad.agencyapp.data.entities.Invoice;
import com.hfad.agencyapp.data.entities.InvoiceItem;
import com.hfad.agencyapp.data.entities.Payment;
import com.hfad.agencyapp.data.entities.ChequePayment;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Repository {
    private static final String TAG = "Repository";
    private static Repository INSTANCE;

    private final AppDatabase db;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor;
    // Toggle to enable/disable remote Firestore sync (useful for local development/emulator)
    private static final boolean REMOTE_SYNC_ENABLED = false;

    private Repository(Context context) {
        db = AppDatabase.getInstance(context);
        firestore = FirebaseFirestore.getInstance();
        executor = Executors.newSingleThreadExecutor();
    }

    public static Repository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (Repository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Repository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // Category operations
    public void insertCategory(Category category) {
        executor.execute(() -> {
            long id = db.categoryDao().insert(category);
            category.id = id;
            syncToFirestore("categories", String.valueOf(id), category);
        });
    }

    public LiveData<List<Category>> getAllCategories() {
        return db.categoryDao().getAll();
    }

    // Product operations
    public void insertProduct(Product product) {
        executor.execute(() -> {
            long id = db.productDao().insert(product);
            product.id = id;
            syncToFirestore("products", String.valueOf(id), product);
        });
    }

    public LiveData<List<Product>> getAllProducts() {
        return db.productDao().getAll();
    }

    // Customer operations
    public void insertCustomer(Customer customer) {
        executor.execute(() -> {
            long id = db.customerDao().insert(customer);
            customer.id = id;
            syncToFirestore("customers", String.valueOf(id), customer);
        });
    }

    public LiveData<List<Customer>> getAllCustomers() {
        return db.customerDao().getAll();
    }

    // Invoice operations
    public void insertInvoice(Invoice invoice) {
        executor.execute(() -> {
            long id = db.invoiceDao().insert(invoice);
            invoice.id = id;
            syncToFirestore("invoices", String.valueOf(id), invoice);
        });
    }

    public void updateInvoice(Invoice invoice) {
        executor.execute(() -> {
            db.invoiceDao().update(invoice);
            syncToFirestore("invoices", String.valueOf(invoice.id), invoice);
        });
    }

    public void deleteInvoiceCascade(long invoiceId) {
        executor.execute(() -> {
            // Restore stock levels from invoice items before deleting
            List<InvoiceItem> items = db.invoiceItemDao().getByInvoiceIdOnce(invoiceId);
            if (items != null) {
                for (InvoiceItem item : items) {
                    com.hfad.agencyapp.data.entities.Product p = db.productDao().getById(item.productId);
                    if (p != null) {
                        p.stock = p.stock + item.quantity + item.freeIssueUnits;
                        db.productDao().update(p);
                    }
                }
            }

            List<Payment> payments = db.paymentDao().getByInvoiceIdOnce(invoiceId);
            for (Payment payment : payments) {
                db.chequePaymentDao().deleteByPaymentId(payment.id);
            }
            db.paymentDao().deleteByInvoiceId(invoiceId);
            db.invoiceItemDao().deleteByInvoiceId(invoiceId);
            db.invoiceDao().deleteById(invoiceId);
            syncToFirestore("invoices", String.valueOf(invoiceId), null);
        });
    }

    public void deleteInvoiceChildren(long invoiceId) {
        executor.execute(() -> {
            // Restore stock levels from invoice items before deleting children
            List<InvoiceItem> items = db.invoiceItemDao().getByInvoiceIdOnce(invoiceId);
            if (items != null) {
                for (InvoiceItem item : items) {
                    com.hfad.agencyapp.data.entities.Product p = db.productDao().getById(item.productId);
                    if (p != null) {
                        p.stock = p.stock + item.quantity + item.freeIssueUnits;
                        db.productDao().update(p);
                    }
                }
            }

            List<Payment> payments = db.paymentDao().getByInvoiceIdOnce(invoiceId);
            for (Payment payment : payments) {
                db.chequePaymentDao().deleteByPaymentId(payment.id);
            }
            db.paymentDao().deleteByInvoiceId(invoiceId);
            db.invoiceItemDao().deleteByInvoiceId(invoiceId);
        });
    }

    public LiveData<List<Invoice>> getAllInvoices() {
        return db.invoiceDao().getAll();
    }

    public LiveData<Double> getTodaySales(long startOfDay, long endOfDay) {
        return db.invoiceDao().getTodaySales(startOfDay, endOfDay);
    }

    public LiveData<Integer> getTodayInvoiceCount(long startOfDay, long endOfDay) {
        return db.invoiceDao().getTodayInvoiceCount(startOfDay, endOfDay);
    }

    public LiveData<Invoice> getInvoiceById(long id) {
        return db.invoiceDao().getById(id);
    }

    // Invoice Item operations
    public void insertInvoiceItem(InvoiceItem item) {
        executor.execute(() -> {
            long id = db.invoiceItemDao().insert(item);
            item.id = id;
            // Decrease stock for the product
            com.hfad.agencyapp.data.entities.Product p = db.productDao().getById(item.productId);
            if (p != null) {
                int newStock = p.stock - item.quantity - item.freeIssueUnits;
                p.stock = Math.max(0, newStock);
                db.productDao().update(p);
            }
            syncToFirestore("invoiceItems", String.valueOf(id), item);
        });
    }

    public LiveData<List<InvoiceItem>> getInvoiceItems(long invoiceId) {
        return db.invoiceItemDao().getByInvoiceId(invoiceId);
    }

    public List<InvoiceItem> getInvoiceItemsOnce(long invoiceId) {
        return db.invoiceItemDao().getByInvoiceIdOnce(invoiceId);
    }

    // Payment operations
    public void insertPayment(Payment payment) {
        executor.execute(() -> {
            long id = db.paymentDao().insert(payment);
            payment.id = id;
            syncToFirestore("payments", String.valueOf(id), payment);
        });
    }

    public LiveData<List<Payment>> getPaymentsByInvoice(long invoiceId) {
        return db.paymentDao().getByInvoiceId(invoiceId);
    }

    // Cheque Payment operations
    public void insertChequePayment(ChequePayment chequePayment) {
        executor.execute(() -> {
            long id = db.chequePaymentDao().insert(chequePayment);
            chequePayment.id = id;
            syncToFirestore("chequePayments", String.valueOf(id), chequePayment);
        });
    }

    // Firebase Sync
    private void syncToFirestore(String collection, String documentId, Object data) {
        if (!REMOTE_SYNC_ENABLED) {
            Log.d(TAG, "Remote sync disabled; skipping sync for: " + collection + "/" + documentId);
            return;
        }
        firestore.collection(collection).document(documentId)
                .set(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Synced to Firestore: " + collection + "/" + documentId))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to sync: " + collection + "/" + documentId, e));
    }

    public void shutdown() {
        executor.shutdown();
    }
}

