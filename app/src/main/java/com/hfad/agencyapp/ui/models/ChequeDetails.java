package com.hfad.agencyapp.ui.models;

import java.util.Date;

/**
 * Data model for Cheque payment details.
 */
public class ChequeDetails {
    private String chequeNumber;
    private String bankName;
    private Date chequeDate;

    public ChequeDetails() {}

    public ChequeDetails(String chequeNumber, String bankName, Date chequeDate) {
        this.chequeNumber = chequeNumber;
        this.bankName = bankName;
        this.chequeDate = chequeDate;
    }

    public String getChequeNumber() {
        return chequeNumber;
    }

    public void setChequeNumber(String chequeNumber) {
        this.chequeNumber = chequeNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public Date getChequeDate() {
        return chequeDate;
    }

    public void setChequeDate(Date chequeDate) {
        this.chequeDate = chequeDate;
    }

    public boolean isValid() {
        return chequeNumber != null && !chequeNumber.trim().isEmpty()
                && bankName != null && !bankName.trim().isEmpty()
                && chequeDate != null;
    }
}
