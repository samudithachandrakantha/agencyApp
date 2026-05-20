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

import java.util.List;

public class InvoiceViewModel extends AndroidViewModel {

    private final Repository repository;
    public final MutableLiveData<Long> currentInvoiceId = new MutableLiveData<>();
    public final LiveData<Invoice> currentInvoice;
    public final LiveData<List<InvoiceItem>> invoiceItems;
    public final LiveData<List<Payment>> payments;

    public InvoiceViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application);
        currentInvoice = new MutableLiveData<>();
        invoiceItems = new MutableLiveData<>();
        payments = new MutableLiveData<>();

        currentInvoiceId.observeForever(id -> {
            if (id != null && id > 0) {
                loadInvoice(id);
            }
        });
    }

    public void setCurrentInvoice(long invoiceId) {
        currentInvoiceId.setValue(invoiceId);
    }

    private void loadInvoice(long invoiceId) {
        ((MutableLiveData<Invoice>) currentInvoice).setValue(null);
        repository.getInvoiceById(invoiceId).observeForever(invoice -> {
            if (invoice != null) {
                ((MutableLiveData<Invoice>) currentInvoice).setValue(invoice);
            }
        });

        ((MutableLiveData<List<InvoiceItem>>) invoiceItems).setValue(null);
        repository.getInvoiceItems(invoiceId).observeForever(items -> {
            if (items != null) {
                ((MutableLiveData<List<InvoiceItem>>) invoiceItems).setValue(items);
            }
        });

        ((MutableLiveData<List<Payment>>) payments).setValue(null);
        repository.getPaymentsByInvoice(invoiceId).observeForever(paymentList -> {
            if (paymentList != null) {
                ((MutableLiveData<List<Payment>>) payments).setValue(paymentList);
            }
        });
    }

    public void addInvoiceItem(long invoiceId, long productId, int quantity, double unitPrice, double totalPrice) {
        InvoiceItem item = new InvoiceItem(invoiceId, productId, quantity, unitPrice, totalPrice);
        repository.insertInvoiceItem(item);
    }

    public void addPayment(long invoiceId, double amount, String method) {
        Payment payment = new Payment(invoiceId, amount, System.currentTimeMillis(), method);
        repository.insertPayment(payment);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}

