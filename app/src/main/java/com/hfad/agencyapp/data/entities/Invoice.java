package com.hfad.agencyapp.data.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "invoices")
public class Invoice {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long customerId;
    public String customerName;  // Store customer name/business name for display
    public String invoiceNumber;
    public long createdAt;
    public double totalAmount;
    public double paidAmount;
    public String note;
    public String status; // PENDING, COMPLETED, CANCELLED
    public String paymentMethod;  // CASH, CHEQUE, CREDIT, CARD, ONLINE
    public long chequeDate; // Cheque date stored as timestamp (in millis). 0 if not a cheque

    @Ignore
    public Invoice(long customerId, String customerName, String invoiceNumber, long createdAt, double totalAmount, double paidAmount, String note, String status, String paymentMethod) {
        this(customerId, customerName, invoiceNumber, createdAt, totalAmount, paidAmount, note, status, paymentMethod, 0);
    }

    public Invoice(long customerId, String customerName, String invoiceNumber, long createdAt, double totalAmount, double paidAmount, String note, String status, String paymentMethod, long chequeDate) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.invoiceNumber = invoiceNumber;
        this.createdAt = createdAt;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.note = note;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.chequeDate = chequeDate;
    }

    // Backwards compatibility constructor
    @Ignore
    public Invoice(long customerId, String invoiceNumber, long createdAt, double totalAmount, double paidAmount, String note, String status) {
        this(customerId, "", invoiceNumber, createdAt, totalAmount, paidAmount, note, status, "CASH");
    }
}

