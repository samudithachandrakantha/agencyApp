package com.hfad.agencyapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.lifecycle.LiveData;

import com.hfad.agencyapp.data.entities.Invoice;

import java.util.List;

@Dao
public interface InvoiceDao {
    @Insert
    long insert(Invoice invoice);

    @Update
    int update(Invoice invoice);

    @Delete
    int delete(Invoice invoice);

    @Query("DELETE FROM invoices WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    LiveData<List<Invoice>> getAll();

    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    List<Invoice> getAllOnce();

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM invoices WHERE createdAt >= :startOfDay AND createdAt < :endOfDay")
    LiveData<Double> getTodaySales(long startOfDay, long endOfDay);

    @Query("SELECT COUNT(*) FROM invoices WHERE createdAt >= :startOfDay AND createdAt < :endOfDay")
    LiveData<Integer> getTodayInvoiceCount(long startOfDay, long endOfDay);

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    LiveData<Invoice> getById(long id);

    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY createdAt DESC")
    List<Invoice> getByCustomerId(long customerId);

    @Query("SELECT * FROM invoices WHERE status = :status ORDER BY createdAt DESC")
    List<Invoice> getByStatus(String status);
}

