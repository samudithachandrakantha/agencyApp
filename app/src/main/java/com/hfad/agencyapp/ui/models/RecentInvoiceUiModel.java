package com.hfad.agencyapp.ui.models;

public class RecentInvoiceUiModel {

    public final String customerName;
    public final String invoiceId;
    public final long invoiceDbId;
    public final String totalAmount;
    public final String paymentStatus; // Paid, Credit, Cheque
    public final String dueAmount; // Due payment amount
    public final boolean isPending; // Indicates if invoice is pending
    public final String chequeDate; // Cheque date for cheque payments (e.g., "27 May 2026")

    public RecentInvoiceUiModel(String customerName, String invoiceId, String totalAmount, String paymentStatus) {
        this(customerName, invoiceId, -1, totalAmount, paymentStatus, "0.00", false, "");
    }

    public RecentInvoiceUiModel(String customerName, String invoiceId, long invoiceDbId, String totalAmount, String paymentStatus) {
        this(customerName, invoiceId, invoiceDbId, totalAmount, paymentStatus, "0.00", false, "");
    }

    public RecentInvoiceUiModel(String customerName, String invoiceId, long invoiceDbId, String totalAmount, String paymentStatus, String dueAmount, boolean isPending) {
        this(customerName, invoiceId, invoiceDbId, totalAmount, paymentStatus, dueAmount, isPending, "");
    }

    public RecentInvoiceUiModel(String customerName, String invoiceId, long invoiceDbId, String totalAmount, String paymentStatus, String dueAmount, boolean isPending, String chequeDate) {
        this.customerName = customerName;
        this.invoiceId = invoiceId;
        this.invoiceDbId = invoiceDbId;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.dueAmount = dueAmount;
        this.isPending = isPending;
        this.chequeDate = chequeDate;
    }
}

