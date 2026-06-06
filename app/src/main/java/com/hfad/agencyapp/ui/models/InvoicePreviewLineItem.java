package com.hfad.agencyapp.ui.models;

public class InvoicePreviewLineItem {
    public final String productName;
    public final String productCode;
    public final int quantity;
    public final double unitPrice;
    public final double lineTotal;
    public final double discountAmount;
    public final String freeIssueText;

    public InvoicePreviewLineItem(String productName, String productCode, int quantity, double unitPrice, double lineTotal) {
        this(productName, productCode, quantity, unitPrice, lineTotal, 0.0, "");
    }

    public InvoicePreviewLineItem(String productName, String productCode, int quantity, double unitPrice, double lineTotal, String freeIssueText) {
        this(productName, productCode, quantity, unitPrice, lineTotal, 0.0, freeIssueText);
    }

    public InvoicePreviewLineItem(String productName, String productCode, int quantity, double unitPrice, double lineTotal, double discountAmount, String freeIssueText) {
        this.productName = productName;
        this.productCode = productCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.discountAmount = discountAmount;
        this.freeIssueText = freeIssueText;
    }
}