package com.hfad.agencyapp.utils;

public class Constants {
    public static final String APP_NAME = "Agency Sales App";
    public static final String DB_NAME = "agency_app.db";

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

    // Image compression settings
    public static final int IMAGE_MAX_WIDTH = 800;
    public static final int IMAGE_QUALITY = 85;

    // Request codes
    public static final int PERMISSION_REQUEST_CODE = 1001;
    public static final int CAMERA_REQUEST_CODE = 1002;
    public static final int GALLERY_REQUEST_CODE = 1003;
}

