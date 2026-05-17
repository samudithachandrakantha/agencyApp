package com.hfad.agencyapp.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "payments")
public class Payment {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long invoiceId;
    public double amount;
    public long timestamp;
    public String method; // CASH, CHEQUE, CARD, ONLINE

    public Payment(long invoiceId, double amount, long timestamp, String method) {
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.timestamp = timestamp;
        this.method = method;
    }
}

