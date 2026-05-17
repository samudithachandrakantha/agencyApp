package com.hfad.agencyapp.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cheque_payments")
public class ChequePayment {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long paymentId;
    public String chequeNumber;
    public String bankName;
    public long chequeDate;
    public String status; // PENDING, CLEARED, BOUNCED

    public ChequePayment(long paymentId, String chequeNumber, String bankName, long chequeDate, String status) {
        this.paymentId = paymentId;
        this.chequeNumber = chequeNumber;
        this.bankName = bankName;
        this.chequeDate = chequeDate;
        this.status = status;
    }
}

