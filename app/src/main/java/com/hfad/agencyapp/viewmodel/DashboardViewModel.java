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
import com.hfad.agencyapp.data.entities.Customer;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final Repository repository;
    public final LiveData<List<Product>> products;
    public final LiveData<List<Category>> categories;
    public final LiveData<List<Invoice>> invoices;
    public final LiveData<List<Customer>> customers;
    public final MutableLiveData<Integer> loadingCount = new MutableLiveData<>(0);

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        products = repository.getAllProducts();
        categories = repository.getAllCategories();
        invoices = repository.getAllInvoices();
        customers = repository.getAllCustomers();
        loadData();
    }

    public void loadData() {
        // Data auto-loads via LiveData from Repository
    }

    public void insertProduct(String name, String sku, long categoryId, double price, int stock, String imageUrl) {
        Product product = new Product(name, sku, categoryId, price, stock, imageUrl);
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
        repository.shutdown();
    }
}

