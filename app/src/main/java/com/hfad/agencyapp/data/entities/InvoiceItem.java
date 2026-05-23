package com.hfad.agencyapp.data.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
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
    public int freeIssueBuyQty;
    public int freeIssueBonusQty;
    public int freeIssueUnits;

    @Ignore
    public InvoiceItem(long invoiceId, long productId, int quantity, double unitPrice, double totalPrice) {
        this(invoiceId, productId, quantity, unitPrice, totalPrice, 0, 0, 0);
    }

    public InvoiceItem(long invoiceId, long productId, int quantity, double unitPrice, double totalPrice,
                       int freeIssueBuyQty, int freeIssueBonusQty, int freeIssueUnits) {
        this.invoiceId = invoiceId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.freeIssueBuyQty = Math.max(0, freeIssueBuyQty);
        this.freeIssueBonusQty = Math.max(0, freeIssueBonusQty);
        this.freeIssueUnits = Math.max(0, freeIssueUnits);
    }
}

