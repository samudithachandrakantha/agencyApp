package com.hfad.agencyapp.ui.models;

/**
 * Data model for individual invoice items.
 */
public class InvoiceItem {
    private String productId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double discountPercent;
    private int freeIssueBuyQty;
    private int freeIssueBonusQty;

    public InvoiceItem(String productId, String productName, int quantity, double unitPrice) {
        this(productId, productName, quantity, unitPrice, 0.0, 0, 0);
    }

    public InvoiceItem(String productId, String productName, int quantity, double unitPrice, double discountPercent) {
        this(productId, productName, quantity, unitPrice, discountPercent, 0, 0);
    }

    public InvoiceItem(String productId, String productName, int quantity, double unitPrice,
                      double discountPercent, int freeIssueBuyQty, int freeIssueBonusQty) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountPercent = Math.max(0, discountPercent);
        this.freeIssueBuyQty = Math.max(0, freeIssueBuyQty);
        this.freeIssueBonusQty = Math.max(0, freeIssueBonusQty);
    }

    // Getters & Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = Math.max(0, discountPercent);
    }

    public double getDiscount() {
        double subtotal = quantity * unitPrice;
        return (subtotal * discountPercent) / 100.0;
    }

    public double getLineTotal() {
        double subtotal = quantity * unitPrice;
        double discount = (subtotal * discountPercent) / 100.0;
        return Math.max(0, subtotal - discount);
    }

    public int getFreeIssueBuyQty() {
        return freeIssueBuyQty;
    }

    public void setFreeIssueBuyQty(int freeIssueBuyQty) {
        this.freeIssueBuyQty = Math.max(0, freeIssueBuyQty);
    }

    public int getFreeIssueBonusQty() {
        return freeIssueBonusQty;
    }

    public void setFreeIssueBonusQty(int freeIssueBonusQty) {
        this.freeIssueBonusQty = Math.max(0, freeIssueBonusQty);
    }

    public int getFreeIssueUnits() {
        if (freeIssueBuyQty <= 0 || freeIssueBonusQty <= 0 || quantity <= 0) {
            return 0;
        }
        return (quantity / freeIssueBuyQty) * freeIssueBonusQty;
    }

    public String getFreeIssueSummary() {
        int freeUnits = getFreeIssueUnits();
        if (freeUnits <= 0) {
            return "";
        }
        return "Free issue: +" + freeUnits + " (Buy " + freeIssueBuyQty + " get " + freeIssueBonusQty + " free)";
    }
}

