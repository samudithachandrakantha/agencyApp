package com.hfad.agencyapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hfad.agencyapp.data.Repository;
import com.hfad.agencyapp.data.entities.Category;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.data.entities.Invoice;
import com.hfad.agencyapp.data.entities.InvoiceItem;
import com.hfad.agencyapp.data.entities.Customer;

import java.util.List;
import java.util.Calendar;
import java.util.TimeZone;

public class DashboardViewModel extends AndroidViewModel {

    private final Repository repository;
    public final LiveData<List<Product>> products;
    public final LiveData<List<Category>> categories;
    public final LiveData<List<Invoice>> invoices;
    public final LiveData<List<Customer>> customers;
    public final LiveData<Double> todaySales;
    public final LiveData<Integer> todayInvoiceCount;
    public final MutableLiveData<Integer> loadingCount = new MutableLiveData<>(0);

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        products = repository.getAllProducts();
        categories = repository.getAllCategories();
        invoices = repository.getAllInvoices();
        customers = repository.getAllCustomers();
        long[] dayBounds = getTodayBounds();
        todaySales = repository.getTodaySales(dayBounds[0], dayBounds[1]);
        todayInvoiceCount = repository.getTodayInvoiceCount(dayBounds[0], dayBounds[1]);
        loadData();
    }

    public LiveData<Invoice> getInvoiceByIdLive(long id) {
        return repository.getInvoiceById(id);
    }

    public LiveData<List<InvoiceItem>> getInvoiceItemsLive(long invoiceId) {
        return repository.getInvoiceItems(invoiceId);
    }

    public void loadData() {
        // Data auto-loads via LiveData from Repository
    }

    private long[] getTodayBounds() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = calendar.getTimeInMillis();
        return new long[]{startOfDay, endOfDay};
    }

    public void insertProduct(String name, String sku, long categoryId, double price, int stock) {
        Product product = Product.createNew(name, "", 0, price, 0, 0, 0, categoryId, sku);
        product.stock = stock;
        repository.insertProduct(product);
    }

    public void insertCustomer(String name, String phone, String email, String address) {
        Customer customer = new Customer(name, phone, email, address);
        repository.insertCustomer(customer);
    }

    public void insertInvoice(long customerId, String invoiceNumber, double totalAmount, String note) {
        Invoice invoice = new Invoice(customerId, invoiceNumber, System.currentTimeMillis(), totalAmount, 0, note, "PENDING");
        repository.insertInvoice(invoice);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}

