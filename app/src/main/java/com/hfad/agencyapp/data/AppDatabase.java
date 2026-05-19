package com.hfad.agencyapp.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.hfad.agencyapp.data.dao.CategoryDao;
import com.hfad.agencyapp.data.dao.ProductDao;
import com.hfad.agencyapp.data.dao.CustomerDao;
import com.hfad.agencyapp.data.dao.InvoiceDao;
import com.hfad.agencyapp.data.dao.InvoiceItemDao;
import com.hfad.agencyapp.data.dao.PaymentDao;
import com.hfad.agencyapp.data.dao.ChequePaymentDao;
import com.hfad.agencyapp.data.dao.StockMovementDao;
import com.hfad.agencyapp.data.entities.Category;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.data.entities.Customer;
import com.hfad.agencyapp.data.entities.Invoice;
import com.hfad.agencyapp.data.entities.InvoiceItem;
import com.hfad.agencyapp.data.entities.Payment;
import com.hfad.agencyapp.data.entities.ChequePayment;
import com.hfad.agencyapp.data.entities.StockMovement;

@Database(entities = {
        Category.class,
        Product.class,
        Customer.class,
        Invoice.class,
        InvoiceItem.class,
        Payment.class,
        ChequePayment.class,
        StockMovement.class
}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract CategoryDao categoryDao();
    public abstract ProductDao productDao();
    public abstract CustomerDao customerDao();
    public abstract InvoiceDao invoiceDao();
    public abstract InvoiceItemDao invoiceItemDao();
    public abstract PaymentDao paymentDao();
    public abstract ChequePaymentDao chequePaymentDao();
    public abstract StockMovementDao stockMovementDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "agency_app.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

