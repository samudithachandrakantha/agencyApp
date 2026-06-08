package com.hfad.agencyapp.utils;

import android.content.Context;

import com.hfad.agencyapp.R;
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

    public static String generateInvoiceText(Context context, Invoice invoice, Customer customer, List<InvoiceItem> items, List<Product> products) {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("========================================\n");
        sb.append("        ").append(context.getString(R.string.invoice_generator_brand)).append("\n");
        sb.append("        ").append(context.getString(R.string.invoice_generator_receipt)).append("\n");
        sb.append("========================================\n\n");

        // Invoice Details
        sb.append(context.getString(R.string.invoice_generator_invoice_number, invoice.invoiceNumber)).append("\n");
        sb.append(context.getString(R.string.invoice_generator_date, dateFormat.format(new Date(invoice.createdAt)))).append("\n\n");

        // Customer Details
        sb.append(context.getString(R.string.invoice_generator_customer)).append("\n");
        if (customer != null) {
            sb.append(customer.name).append("\n");
            sb.append(context.getString(R.string.invoice_generator_phone, customer.phone)).append("\n");
            sb.append(context.getString(R.string.invoice_generator_address, customer.address)).append("\n");
        }
        sb.append("\n");

        // Items
        sb.append("----------------------------------------\n");
        sb.append(context.getString(R.string.invoice_generator_item_details)).append("\n");
        sb.append("----------------------------------------\n");

        double subtotal = 0.0;
        double total = 0.0;

        for (InvoiceItem item : items) {
            Product product = findProductById(products, item.productId);
            String productName = product != null ? product.name : "Unknown";
            double lineSubtotal = item.quantity * item.unitPrice;
            double lineTotal = item.totalPrice > 0 ? item.totalPrice : lineSubtotal;
            double lineDiscount = Math.max(0.0, lineSubtotal - lineTotal);
            subtotal += lineSubtotal;
            total += lineTotal;
            sb.append(String.format("%s\n", productName));
            sb.append(String.format(context.getString(R.string.invoice_generator_qty_line), item.quantity, item.unitPrice, lineSubtotal)).append("\n");
            sb.append(String.format(context.getString(R.string.invoice_generator_discount_line), lineDiscount)).append("\n");
            sb.append(String.format(context.getString(R.string.invoice_generator_line_total), lineTotal)).append("\n");
            if (item.freeIssueUnits > 0) {
                sb.append(String.format(context.getString(R.string.invoice_generator_free_issue),
                        item.freeIssueUnits,
                        item.freeIssueBuyQty,
                        item.freeIssueBonusQty)).append("\n");
            }
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format(context.getString(R.string.invoice_generator_subtotal), subtotal)).append("\n");
        sb.append(String.format(context.getString(R.string.invoice_generator_total_discount), Math.max(0.0, subtotal - total))).append("\n");
        sb.append(String.format(context.getString(R.string.invoice_generator_total), total)).append("\n");
        sb.append(String.format(context.getString(R.string.invoice_generator_paid), invoice.paidAmount)).append("\n");
        sb.append(String.format(context.getString(R.string.invoice_generator_outstanding), total - invoice.paidAmount)).append("\n");
        sb.append("\n");

        if (!TextUtils.isEmpty(invoice.note)) {
            sb.append(String.format(context.getString(R.string.invoice_generator_note), invoice.note)).append("\n");
        }

        sb.append("========================================\n");
        sb.append(context.getString(R.string.invoice_generator_thank_you)).append("\n");
        sb.append("========================================\n");

        return sb.toString();
    }

    public static String generateReceiptForPrinting(Context context, Invoice invoice, Customer customer, List<InvoiceItem> items, List<Product> products) {
        StringBuilder sb = new StringBuilder();

        sb.append("  ").append(context.getString(R.string.invoice_generator_print_receipt_brand)).append("\n");
        sb.append("       ").append(context.getString(R.string.invoice_generator_print_receipt_title)).append("\n");
        sb.append("\n");

        sb.append(context.getString(R.string.invoice_generator_invoice, invoice.invoiceNumber)).append("\n");
        sb.append(context.getString(R.string.invoice_generator_date, dateFormat.format(new Date(invoice.createdAt)))).append("\n");
        sb.append(context.getString(R.string.invoice_generator_customer)).append(" ").append(customer != null ? customer.name : context.getString(R.string.invoice_generator_customer_walk_in)).append("\n");
        sb.append("\n");

        sb.append(context.getString(R.string.invoice_generator_items)).append("\n");
        double subtotal = 0.0;
        double total = 0.0;
        for (InvoiceItem item : items) {
            Product product = findProductById(products, item.productId);
            String productName = product != null ? product.name : context.getString(R.string.invoice_generator_unknown);
            double lineSubtotal = item.quantity * item.unitPrice;
            double lineTotal = item.totalPrice > 0 ? item.totalPrice : lineSubtotal;
            double lineDiscount = Math.max(0.0, lineSubtotal - lineTotal);
            subtotal += lineSubtotal;
            total += lineTotal;
            sb.append(String.format(context.getString(R.string.invoice_generator_print_row), productName, item.quantity, item.unitPrice)).append("\n");
            sb.append(String.format(context.getString(R.string.invoice_generator_print_subtotal), lineSubtotal)).append("\n");
            sb.append(String.format(context.getString(R.string.invoice_generator_print_discount), lineDiscount)).append("\n");
            sb.append(String.format(context.getString(R.string.invoice_generator_print_total), lineTotal)).append("\n");
            if (item.freeIssueUnits > 0) {
                sb.append(String.format(context.getString(R.string.invoice_generator_print_free),
                        item.freeIssueUnits,
                        item.freeIssueBuyQty,
                        item.freeIssueBonusQty)).append("\n");
            }
        }

        sb.append("\n");
        sb.append(String.format(context.getString(R.string.invoice_generator_subtotal), subtotal)).append("\n");
        sb.append(String.format(context.getString(R.string.invoice_generator_total_discount), Math.max(0.0, subtotal - total))).append("\n");
        sb.append(String.format(context.getString(R.string.invoice_generator_total_amount), total)).append("\n");
        sb.append(String.format(context.getString(R.string.invoice_generator_paid_amount), invoice.paidAmount)).append("\n");
        sb.append(String.format(context.getString(R.string.invoice_generator_balance), total - invoice.paidAmount)).append("\n");

        sb.append("\n");
        sb.append(context.getString(R.string.invoice_generator_thank_you_short)).append("\n");

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

