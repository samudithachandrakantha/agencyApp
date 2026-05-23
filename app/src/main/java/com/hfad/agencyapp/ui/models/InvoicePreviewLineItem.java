package com.hfad.agencyapp.ui.models;

public class InvoicePreviewLineItem {
    public final String productName;
    public final String productCode;
    public final int quantity;
    public final double unitPrice;
    public final double lineTotal;
    public final String freeIssueText;

    public InvoicePreviewLineItem(String productName, String productCode, int quantity, double unitPrice, double lineTotal) {
        this(productName, productCode, quantity, unitPrice, lineTotal, "");
    }

    public InvoicePreviewLineItem(String productName, String productCode, int quantity, double unitPrice, double lineTotal, String freeIssueText) {
        this.productName = productName;
        this.productCode = productCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.freeIssueText = freeIssueText;
    }
}