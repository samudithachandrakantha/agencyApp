package com.hfad.agencyapp.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "invoice_items")
public class InvoiceItem {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long invoiceId;
    public long productId;
    public int quantity;
    public double unitPrice;
    public double totalPrice;

    public InvoiceItem(long invoiceId, long productId, int quantity, double unitPrice, double totalPrice) {
        this.invoiceId = invoiceId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }
}

