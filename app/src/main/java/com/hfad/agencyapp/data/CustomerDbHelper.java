package com.hfad.agencyapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hfad.agencyapp.ui.models.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Simple SQLiteOpenHelper for customers table. Uses TEXT id (UUID) to keep compatibility.
 */
public class CustomerDbHelper extends SQLiteOpenHelper {
    // Keep this separate from Room's agency_app.db to avoid schema conflicts.
    private static final String DATABASE_NAME = "customer_ui.db";
    // bumped to 4 to add address/payment methods while preserving legacy city data
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_CUSTOMERS = "customers";
    public static final String COL_ID = "id";
    public static final String COL_BUSINESS = "business_name";
    public static final String COL_CONTACT = "contact_person";
    public static final String COL_CITY = "city"; // legacy column retained for upgrade compatibility
    public static final String COL_ADDRESS = "address";
    // Optional fields
    public static final String COL_PHONE = "phone";
    public static final String COL_BR_NUMBER = "br_number";
    public static final String COL_ID_NUMBER = "id_number";
    public static final String COL_PAYMENT_METHODS = "payment_methods";

    public CustomerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create customers table with legacy city plus canonical address/payment columns.
        String sql = "CREATE TABLE " + TABLE_CUSTOMERS + " ("
                + COL_ID + " TEXT PRIMARY KEY,"
                + COL_BUSINESS + " TEXT,"
                + COL_CONTACT + " TEXT,"
                + COL_CITY + " TEXT,"
                + COL_ADDRESS + " TEXT,"
                + COL_PHONE + " TEXT,"
                + COL_BR_NUMBER + " TEXT,"
                + COL_ID_NUMBER + " TEXT,"
                + COL_PAYMENT_METHODS + " TEXT"
                + ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migrate existing DB without dropping data.
        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_CUSTOMERS + " ADD COLUMN " + COL_ADDRESS + " TEXT");
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE " + TABLE_CUSTOMERS + " ADD COLUMN " + COL_PHONE + " TEXT");
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE " + TABLE_CUSTOMERS + " ADD COLUMN " + COL_BR_NUMBER + " TEXT");
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE " + TABLE_CUSTOMERS + " ADD COLUMN " + COL_ID_NUMBER + " TEXT");
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE " + TABLE_CUSTOMERS + " ADD COLUMN " + COL_PAYMENT_METHODS + " TEXT");
            } catch (Exception ignored) {}
            try {
                db.execSQL("UPDATE " + TABLE_CUSTOMERS + " SET " + COL_ADDRESS + " = " + COL_CITY + " WHERE (" + COL_ADDRESS + " IS NULL OR " + COL_ADDRESS + " = '') AND " + COL_CITY + " IS NOT NULL");
            } catch (Exception ignored) {}
        }
    }

    // Insert or update
    public String insertCustomer(Customer c) {
        SQLiteDatabase db = getWritableDatabase();
        if (c.getId() == null || c.getId().isEmpty()) {
            c.setId(UUID.randomUUID().toString());
        }
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, c.getId());
        cv.put(COL_BUSINESS, c.getBusinessName());
        cv.put(COL_CONTACT, c.getContactPerson());
        cv.put(COL_CITY, c.getAddress());
        cv.put(COL_ADDRESS, c.getAddress());
        // optional fields may be null
        cv.put(COL_PHONE, c.getPhone());
        cv.put(COL_BR_NUMBER, c.getBrNumber());
        cv.put(COL_ID_NUMBER, c.getIdNumber());
        cv.put(COL_PAYMENT_METHODS, c.getPaymentMethods());

        long res = db.insertWithOnConflict(TABLE_CUSTOMERS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        return res == -1 ? null : c.getId();
    }

    public boolean deleteCustomer(String id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_CUSTOMERS, COL_ID + " = ?", new String[]{id});
        return rows > 0;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_CUSTOMERS, null, null, null, null, null, COL_BUSINESS + " ASC")) {
            while (cursor.moveToNext()) {
                list.add(cursorToCustomer(cursor));
            }
        }
        return list;
    }

    public Customer getCustomerById(String id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_CUSTOMERS, null, COL_ID + " = ?", new String[]{id}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursorToCustomer(cursor);
            }
            return null;
        }
    }

    public List<Customer> searchCustomers(String query) {
        List<Customer> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String q = "%" + query + "%";
        // include optional fields in search (phone, br number, id number)
        String selection = COL_BUSINESS + " LIKE ? OR " + COL_CONTACT + " LIKE ? OR " + COL_ADDRESS + " LIKE ? OR " + COL_CITY + " LIKE ?"
                + " OR " + COL_PHONE + " LIKE ? OR " + COL_BR_NUMBER + " LIKE ? OR " + COL_ID_NUMBER + " LIKE ?";
        String[] args = new String[]{q, q, q, q, q, q, q};
        try (Cursor cursor = db.query(TABLE_CUSTOMERS, null, selection, args, null, null, COL_BUSINESS + " ASC")) {
            while (cursor.moveToNext()) {
                list.add(cursorToCustomer(cursor));
            }
        }
        return list;
    }

    private Customer cursorToCustomer(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID));
        String business = cursor.getString(cursor.getColumnIndexOrThrow(COL_BUSINESS));
        String contact = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT));
        String address = null;
        int idx = cursor.getColumnIndex(COL_ADDRESS);
        if (idx != -1) address = cursor.getString(idx);
        if (address == null || address.isEmpty()) {
            idx = cursor.getColumnIndex(COL_CITY);
            if (idx != -1) address = cursor.getString(idx);
        }
        // optional fields may be null if not set
        String phone = null;
        String br = null;
        String idNum = null;
        idx = cursor.getColumnIndex(COL_PHONE);
        if (idx != -1) phone = cursor.getString(idx);
        idx = cursor.getColumnIndex(COL_BR_NUMBER);
        if (idx != -1) br = cursor.getString(idx);
        idx = cursor.getColumnIndex(COL_ID_NUMBER);
        if (idx != -1) idNum = cursor.getString(idx);
        String paymentMethods = null;
        idx = cursor.getColumnIndex(COL_PAYMENT_METHODS);
        if (idx != -1) paymentMethods = cursor.getString(idx);

        return new Customer(id, business, contact, address, phone, br, idNum, paymentMethods);
    }
}




