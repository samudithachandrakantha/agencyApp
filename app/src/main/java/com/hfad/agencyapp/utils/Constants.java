package com.hfad.agencyapp.utils;

public class Constants {
    public static final String APP_NAME = "Agency Sales App";
    public static final String DB_NAME = "agency_app.db";
    public static final String CUSTOMER_DB_NAME = "customer_ui.db";

    // SharedPreferences
    public static final String PREFS_CHEQUE = "cheque_prefs";
    public static final String PREFS_APP_SETTINGS = "app_settings";
    public static final String KEY_CHEQUE_NOTIFICATION_COUNT = "cheque_notification_count";
    public static final String KEY_NEXT_CHEQUE_NOTIFICATION_TEXT = "next_cheque_notification_text";
    public static final String KEY_OUT_OF_STOCK_BEHAVIOR = "out_of_stock_behavior";

    // Out-of-stock behavior preference values
    public static final String OUT_OF_STOCK_AUTO_ALLOW = "AUTO_ALLOW";
    public static final String OUT_OF_STOCK_PROMPT = "PROMPT";
    public static final String OUT_OF_STOCK_BLOCK = "BLOCK";

    // Notifications
    public static final String CHANNEL_CHEQUE_NOTIFICATIONS = "cheque_notifications";

    // Firebase Collections
    public static final String COLLECTION_PRODUCTS = "products";
    public static final String COLLECTION_CUSTOMERS = "customers";
    public static final String COLLECTION_INVOICES = "invoices";
    public static final String COLLECTION_PAYMENTS = "payments";

    // Invoice Status
    public static final String INVOICE_STATUS_PENDING = "PENDING";
    public static final String INVOICE_STATUS_COMPLETED = "COMPLETED";
    public static final String INVOICE_STATUS_CANCELLED = "CANCELLED";

    // Payment Methods
    public static final String PAYMENT_METHOD_CASH = "CASH";
    public static final String PAYMENT_METHOD_CHEQUE = "CHEQUE";
    public static final String PAYMENT_METHOD_CARD = "CARD";
    public static final String PAYMENT_METHOD_ONLINE = "ONLINE";

    // Request codes
    public static final int PERMISSION_REQUEST_CODE = 1001;
}

