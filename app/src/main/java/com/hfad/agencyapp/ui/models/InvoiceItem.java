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

    public InvoiceItem(String productId, String productName, int quantity, double unitPrice) {
        this(productId, productName, quantity, unitPrice, 0.0);
    }

    public InvoiceItem(String productId, String productName, int quantity, double unitPrice, double discountPercent) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountPercent = Math.max(0, discountPercent);
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
}

