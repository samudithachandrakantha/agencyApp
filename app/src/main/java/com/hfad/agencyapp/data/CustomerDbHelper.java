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
    private static final String DATABASE_NAME = "agency_app.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_CUSTOMERS = "customers";
    public static final String COL_ID = "id";
    public static final String COL_BUSINESS = "business_name";
    public static final String COL_CONTACT = "contact_person";
    public static final String COL_CITY = "city";

    public CustomerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create customers table with only the required columns: id, business_name, contact_person, city
        String sql = "CREATE TABLE " + TABLE_CUSTOMERS + " ("
                + COL_ID + " TEXT PRIMARY KEY,"
                + COL_BUSINESS + " TEXT,"
                + COL_CONTACT + " TEXT,"
                + COL_CITY + " TEXT"
                + ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For schema change to simple customers table, drop and recreate.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
        onCreate(db);
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
        cv.put(COL_CITY, c.getCity());

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
        Cursor cursor = db.query(TABLE_CUSTOMERS, null, null, null, null, null, COL_BUSINESS + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Customer c = cursorToCustomer(cursor);
                list.add(c);
            }
            cursor.close();
        }
        return list;
    }

    public Customer getCustomerById(String id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CUSTOMERS, null, COL_ID + " = ?", new String[]{id}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Customer c = cursorToCustomer(cursor);
                cursor.close();
                return c;
            }
            cursor.close();
        }
        return null;
    }

    public List<Customer> searchCustomers(String query) {
        List<Customer> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String q = "%" + query + "%";
        Cursor cursor = db.query(TABLE_CUSTOMERS, null, COL_BUSINESS + " LIKE ? OR " + COL_CONTACT + " LIKE ? OR " + COL_CITY + " LIKE ?",
                new String[]{q, q, q}, null, null, COL_BUSINESS + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToCustomer(cursor));
            }
            cursor.close();
        }
        return list;
    }

    private Customer cursorToCustomer(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID));
        String business = cursor.getString(cursor.getColumnIndexOrThrow(COL_BUSINESS));
        String contact = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT));
        String city = cursor.getString(cursor.getColumnIndexOrThrow(COL_CITY));
        // Phone and address are no longer stored in the simplified schema
        return new Customer(id, business, contact, "", "", city);
    }
}




