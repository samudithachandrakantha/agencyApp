package com.hfad.agencyapp.ui.models;

public class RecentInvoiceUiModel {

    public final String customerName;
    public final String invoiceId;
    public final long invoiceDbId;
    public final String totalAmount;
    public final String paymentStatus; // Paid, Credit, Cheque

    public RecentInvoiceUiModel(String customerName, String invoiceId, String totalAmount, String paymentStatus) {
        this(customerName, invoiceId, -1, totalAmount, paymentStatus);
    }

    public RecentInvoiceUiModel(String customerName, String invoiceId, long invoiceDbId, String totalAmount, String paymentStatus) {
        this.customerName = customerName;
        this.invoiceId = invoiceId;
        this.invoiceDbId = invoiceDbId;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
    }
}

