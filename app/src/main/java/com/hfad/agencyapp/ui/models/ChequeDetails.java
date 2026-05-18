package com.hfad.agencyapp.ui.models;

import java.util.Date;

/**
 * Data model for Cheque payment details.
 */
public class ChequeDetails {
    private String chequeNumber;
    private String bankName;
    private Date chequeDate;
    private String chequeImagePath; // File path to stored cheque image

    public ChequeDetails() {}

    public ChequeDetails(String chequeNumber, String bankName, Date chequeDate, String chequeImagePath) {
        this.chequeNumber = chequeNumber;
        this.bankName = bankName;
        this.chequeDate = chequeDate;
        this.chequeImagePath = chequeImagePath;
    }

    // Getters & Setters
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

    public String getChequeImagePath() {
        return chequeImagePath;
    }

    public void setChequeImagePath(String chequeImagePath) {
        this.chequeImagePath = chequeImagePath;
    }

    /**
     * Validate cheque details for completeness.
     */
    public boolean isValid() {
        return chequeNumber != null && !chequeNumber.trim().isEmpty() &&
               bankName != null && !bankName.trim().isEmpty() &&
               chequeDate != null &&
               chequeImagePath != null && !chequeImagePath.trim().isEmpty();
    }
}

