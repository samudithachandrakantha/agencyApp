package com.hfad.agencyapp.ui.models;

public class RecentInvoiceUiModel {

    public final String customerName;
    public final String invoiceId;
    public final String totalAmount;
    public final String paymentStatus; // Paid, Credit, Cheque

    public RecentInvoiceUiModel(String customerName, String invoiceId, String totalAmount, String paymentStatus) {
        this.customerName = customerName;
        this.invoiceId = invoiceId;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
    }
}

