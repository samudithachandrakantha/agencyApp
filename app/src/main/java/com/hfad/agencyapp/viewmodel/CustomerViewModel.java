package com.hfad.agencyapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hfad.agencyapp.data.Repository;
import com.hfad.agencyapp.data.CustomerRepository;
import com.hfad.agencyapp.data.entities.Invoice;
import com.hfad.agencyapp.ui.models.Customer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * ViewModel for managing customers list and CRUD operations.
 */
public class CustomerViewModel extends AndroidViewModel {
    private final CustomerRepository repository;
    private final Repository invoiceRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<Customer>> customers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private String currentQuery = "";

    public CustomerViewModel(@NonNull Application application) {
        super(application);
        repository = new CustomerRepository(application);
        invoiceRepository = Repository.getInstance(application);
        loadAll();
    }

    public LiveData<List<Customer>> getCustomers() { return customers; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void loadAll() {
        loading.postValue(true);
        executor.execute(() -> {
            try {
                Future<List<Customer>> f = repository.getAllCustomersAsync();
                List<Customer> list = f.get();
                customers.postValue(applyOutstandingAmounts(list));
            } catch (ExecutionException | InterruptedException e) {
                error.postValue(e.getMessage());
            } finally {
                loading.postValue(false);
            }
        });
    }

    public void filter(String q) {
        currentQuery = q == null ? "" : q.trim();
        if (currentQuery.isEmpty()) {
            loadAll();
            return;
        }
        loading.postValue(true);
        executor.execute(() -> {
            try {
                Future<List<Customer>> f = repository.searchCustomersAsync(currentQuery);
                List<Customer> list = f.get();
                customers.postValue(applyOutstandingAmounts(list));
            } catch (ExecutionException | InterruptedException e) {
                error.postValue(e.getMessage());
            } finally {
                loading.postValue(false);
            }
        });
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

    @Override
    protected void onCleared() {
        executor.shutdownNow();
        super.onCleared();
    }

    private List<Customer> applyOutstandingAmounts(List<Customer> source) {
        List<Customer> result = new ArrayList<>();
        if (source == null || source.isEmpty()) {
            return result;
        }

        Map<String, Double> outstandingByKey = buildOutstandingIndex();
        for (Customer customer : source) {
            if (customer == null) {
                continue;
            }
            Customer copy = copyCustomer(customer);
            copy.setOutstandingAmount(resolveOutstandingAmount(copy, outstandingByKey));
            result.add(copy);
        }
        return result;
    }

    private Map<String, Double> buildOutstandingIndex() {
        Map<String, Double> totals = new HashMap<>();
        List<Invoice> invoices = invoiceRepository.getAllInvoicesOnce();
        if (invoices == null) {
            return totals;
        }

        for (Invoice invoice : invoices) {
            if (invoice == null) {
                continue;
            }
            double outstanding = calculateOutstanding(invoice);
            if (outstanding <= 0.0) {
                continue;
            }

            String nameKey = normalizeCustomerKey(invoice.customerName);
            if (!nameKey.isEmpty()) {
                Double current = totals.get(nameKey);
                totals.put(nameKey, (current != null ? current : 0.0) + outstanding);
            }
        }
        return totals;
    }

    private double resolveOutstandingAmount(Customer customer, Map<String, Double> outstandingByKey) {
        double total = 0.0;

        String businessKey = normalizeCustomerKey(customer.getBusinessName());
        if (!businessKey.isEmpty()) {
            Double value = outstandingByKey.get(businessKey);
            if (value != null) {
                total += value;
            }
        }

        String contactKey = normalizeCustomerKey(customer.getContactPerson());
        if (!contactKey.isEmpty() && !contactKey.equals(businessKey)) {
            Double value = outstandingByKey.get(contactKey);
            if (value != null) {
                total += value;
            }
        }

        return total;
    }

    private double calculateOutstanding(Invoice invoice) {
        double effectivePaidAmount = invoice.paidAmount;
        if ("CASH".equalsIgnoreCase(invoice.paymentMethod)) {
            effectivePaidAmount = invoice.totalAmount;
        }
        return Math.max(0.0, invoice.totalAmount - effectivePaidAmount);
    }

    private Customer copyCustomer(Customer original) {
        return new Customer(
                original.getId(),
                original.getBusinessName(),
                original.getContactPerson(),
                original.getAddress(),
                original.getPhone(),
                original.getBrNumber(),
                original.getIdNumber(),
                original.getPaymentMethods(),
                original.isBlocked()
        );
    }

    private String normalizeCustomerKey(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }
}

