package com.hfad.agencyapp.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "invoices")
public class Invoice {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long customerId;
    public String invoiceNumber;
    public long createdAt;
    public double totalAmount;
    public double paidAmount;
    public String note;
    public String status; // PENDING, COMPLETED, CANCELLED

    public Invoice(long customerId, String invoiceNumber, long createdAt, double totalAmount, double paidAmount, String note, String status) {
        this.customerId = customerId;
        this.invoiceNumber = invoiceNumber;
        this.createdAt = createdAt;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.note = note;
        this.status = status;
    }
}

