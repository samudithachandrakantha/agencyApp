package com.hfad.agencyapp.data;

import android.content.Context;

import com.hfad.agencyapp.ui.models.Customer;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Repository that abstracts DB operations and runs them off the main thread.
 */
public class CustomerRepository {
    private final CustomerDbHelper dbHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CustomerRepository(Context context) {
        dbHelper = new CustomerDbHelper(context.getApplicationContext());
    }

    public Future<List<Customer>> getAllCustomersAsync() {
        return executor.submit(new Callable<List<Customer>>() {
            @Override
            public List<Customer> call() {
                return dbHelper.getAllCustomers();
            }
        });
    }

    public Future<List<Customer>> searchCustomersAsync(final String q) {
        return executor.submit(() -> dbHelper.searchCustomers(q));
    }

    public Future<String> insertOrUpdateAsync(final Customer c) {
        return executor.submit(() -> dbHelper.insertCustomer(c));
    }

    public Future<Boolean> deleteCustomerAsync(final String id) {
        return executor.submit(() -> dbHelper.deleteCustomer(id));
    }

    public Future<Customer> getByIdAsync(final String id) {
        return executor.submit(() -> dbHelper.getCustomerById(id));
    }
}

