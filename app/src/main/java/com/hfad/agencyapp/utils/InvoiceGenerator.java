package com.hfad.agencyapp.utils;

import com.hfad.agencyapp.data.entities.Customer;
import com.hfad.agencyapp.data.entities.Invoice;
import com.hfad.agencyapp.data.entities.InvoiceItem;
import com.hfad.agencyapp.data.entities.Product;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InvoiceGenerator {

    private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public static String generateInvoiceText(Invoice invoice, Customer customer, List<InvoiceItem> items, List<Product> products) {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("========================================\n");
        sb.append("        CAKE INGREDIENTS SALES\n");
        sb.append("        Invoice Receipt\n");
        sb.append("========================================\n\n");

        // Invoice Details
        sb.append("Invoice #: ").append(invoice.invoiceNumber).append("\n");
        sb.append("Date: ").append(dateFormat.format(new Date(invoice.createdAt))).append("\n\n");

        // Customer Details
        sb.append("Customer:\n");
        if (customer != null) {
            sb.append(customer.name).append("\n");
            sb.append("Phone: ").append(customer.phone).append("\n");
            sb.append("Address: ").append(customer.address).append("\n");
        }
        sb.append("\n");

        // Items
        sb.append("----------------------------------------\n");
        sb.append("Item Details:\n");
        sb.append("----------------------------------------\n");

        for (InvoiceItem item : items) {
            Product product = findProductById(products, item.productId);
            String productName = product != null ? product.name : "Unknown";
            sb.append(String.format("%s\n", productName));
            sb.append(String.format("Qty: %d x %.2f = %.2f\n", item.quantity, item.unitPrice, item.totalPrice));
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format("Subtotal: %.2f\n", invoice.totalAmount));
        sb.append(String.format("Paid: %.2f\n", invoice.paidAmount));
        sb.append(String.format("Outstanding: %.2f\n", invoice.totalAmount - invoice.paidAmount));
        sb.append("\n");

        if (!TextUtils.isEmpty(invoice.note)) {
            sb.append("Note: ").append(invoice.note).append("\n");
        }

        sb.append("========================================\n");
        sb.append("Thank you for your business!\n");
        sb.append("========================================\n");

        return sb.toString();
    }

    public static String generateReceiptForPrinting(Invoice invoice, Customer customer, List<InvoiceItem> items, List<Product> products) {
        StringBuilder sb = new StringBuilder();

        sb.append("  ===== CAKE INGREDIENTS SALES =====\n");
        sb.append("       INVOICE RECEIPT\n");
        sb.append("\n");

        sb.append("Invoice: ").append(invoice.invoiceNumber).append("\n");
        sb.append("Date: ").append(dateFormat.format(new Date(invoice.createdAt))).append("\n");
        sb.append("Customer: ").append(customer != null ? customer.name : "Walk-in").append("\n");
        sb.append("\n");

        sb.append("Items:\n");
        for (InvoiceItem item : items) {
            Product product = findProductById(products, item.productId);
            String productName = product != null ? product.name : "Unknown";
            sb.append(String.format("%-20s %d x %.2f\n", productName, item.quantity, item.unitPrice));
            sb.append(String.format("                        Total: %.2f\n", item.totalPrice));
        }

        sb.append("\n");
        sb.append(String.format("Total Amount: %.2f\n", invoice.totalAmount));
        sb.append(String.format("Paid Amount: %.2f\n", invoice.paidAmount));
        sb.append(String.format("Balance: %.2f\n", invoice.totalAmount - invoice.paidAmount));

        sb.append("\n");
        sb.append("Thank You!\n");

        return sb.toString();
    }

    private static Product findProductById(List<Product> products, long productId) {
        for (Product p : products) {
            if (p.id == productId) return p;
        }
        return null;
    }

    // TextUtils import helper (avoiding Android Text utils dependency in this standalone util)
    public static class TextUtils {
        public static boolean isEmpty(CharSequence str) {
            return str == null || str.length() == 0;
        }
    }
}

