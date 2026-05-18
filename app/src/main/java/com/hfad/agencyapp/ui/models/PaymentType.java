package com.hfad.agencyapp.ui.models;

/**
 * Enum representing payment types for invoices.
 */
public enum PaymentType {
    CASH("Cash"),
    CREDIT("Credit"),
    CHEQUE("Cheque");

    private final String displayName;

    PaymentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

