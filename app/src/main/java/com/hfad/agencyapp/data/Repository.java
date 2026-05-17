package com.hfad.agencyapp.data;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
        MutableLiveData<List<Category>> result = new MutableLiveData<>();
        executor.execute(() -> result.postValue(db.categoryDao().getAll()));
        return result;
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
        MutableLiveData<List<Product>> result = new MutableLiveData<>();
        executor.execute(() -> result.postValue(db.productDao().getAll()));
        return result;
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
        MutableLiveData<List<Customer>> result = new MutableLiveData<>();
        executor.execute(() -> result.postValue(db.customerDao().getAll()));
        return result;
    }

    // Invoice operations
    public void insertInvoice(Invoice invoice) {
        executor.execute(() -> {
            long id = db.invoiceDao().insert(invoice);
            invoice.id = id;
            syncToFirestore("invoices", String.valueOf(id), invoice);
        });
    }

    public LiveData<List<Invoice>> getAllInvoices() {
        MutableLiveData<List<Invoice>> result = new MutableLiveData<>();
        executor.execute(() -> result.postValue(db.invoiceDao().getAll()));
        return result;
    }

    public LiveData<Invoice> getInvoiceById(long id) {
        MutableLiveData<Invoice> result = new MutableLiveData<>();
        executor.execute(() -> result.postValue(db.invoiceDao().getById(id)));
        return result;
    }

    // Invoice Item operations
    public void insertInvoiceItem(InvoiceItem item) {
        executor.execute(() -> {
            long id = db.invoiceItemDao().insert(item);
            item.id = id;
            syncToFirestore("invoiceItems", String.valueOf(id), item);
        });
    }

    public LiveData<List<InvoiceItem>> getInvoiceItems(long invoiceId) {
        MutableLiveData<List<InvoiceItem>> result = new MutableLiveData<>();
        executor.execute(() -> result.postValue(db.invoiceItemDao().getByInvoiceId(invoiceId)));
        return result;
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
        MutableLiveData<List<Payment>> result = new MutableLiveData<>();
        executor.execute(() -> result.postValue(db.paymentDao().getByInvoiceId(invoiceId)));
        return result;
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
        firestore.collection(collection).document(documentId)
                .set(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Synced to Firestore: " + collection + "/" + documentId))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to sync: " + collection + "/" + documentId, e));
    }

    public void shutdown() {
        executor.shutdown();
    }
}

