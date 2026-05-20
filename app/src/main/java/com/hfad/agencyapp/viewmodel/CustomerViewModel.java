package com.hfad.agencyapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hfad.agencyapp.data.CustomerRepository;
import com.hfad.agencyapp.ui.models.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * ViewModel for managing customers list and CRUD operations.
 */
public class CustomerViewModel extends AndroidViewModel {
    private final CustomerRepository repository;
    private final MutableLiveData<List<Customer>> customers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private String currentQuery = "";

    public CustomerViewModel(@NonNull Application application) {
        super(application);
        repository = new CustomerRepository(application);
        loadAll();
    }

    public LiveData<List<Customer>> getCustomers() { return customers; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void loadAll() {
        loading.postValue(true);
        Future<List<Customer>> f = repository.getAllCustomersAsync();
        try {
            List<Customer> list = f.get();
            customers.postValue(list);
        } catch (ExecutionException | InterruptedException e) {
            error.postValue(e.getMessage());
        } finally {
            loading.postValue(false);
        }
    }

    public void filter(String q) {
        currentQuery = q == null ? "" : q.trim();
        if (currentQuery.isEmpty()) {
            loadAll();
            return;
        }
        loading.postValue(true);
        Future<List<Customer>> f = repository.searchCustomersAsync(currentQuery);
        try {
            List<Customer> list = f.get();
            customers.postValue(list);
        } catch (ExecutionException | InterruptedException e) {
            error.postValue(e.getMessage());
        } finally {
            loading.postValue(false);
        }
    }

    public boolean saveCustomer(Customer c) {
        try {
            Future<String> f = repository.insertOrUpdateAsync(c);
            String id = f.get();
            if (id != null) {
                loadAll();
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            error.postValue(e.getMessage());
        }
        return false;
    }

    public boolean deleteCustomer(String id) {
        try {
            Future<Boolean> f = repository.deleteCustomerAsync(id);
            boolean ok = f.get();
            if (ok) loadAll();
            return ok;
        } catch (ExecutionException | InterruptedException e) {
            error.postValue(e.getMessage());
            return false;
        }
    }

    public Customer getById(String id) {
        try {
            Future<Customer> f = repository.getByIdAsync(id);
            return f.get();
        } catch (ExecutionException | InterruptedException e) {
            error.postValue(e.getMessage());
            return null;
        }
    }

    public void refresh() {
        if (currentQuery == null || currentQuery.isEmpty()) {
            loadAll();
        } else {
            filter(currentQuery);
        }
    }
}

