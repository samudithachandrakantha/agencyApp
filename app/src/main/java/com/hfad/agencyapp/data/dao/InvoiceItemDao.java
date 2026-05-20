package com.hfad.agencyapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.lifecycle.LiveData;

import com.hfad.agencyapp.data.entities.InvoiceItem;

import java.util.List;

@Dao
public interface InvoiceItemDao {
    @Insert
    long insert(InvoiceItem item);

    @Update
    int update(InvoiceItem item);

    @Delete
    int delete(InvoiceItem item);

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId ORDER BY id ASC")
    LiveData<List<InvoiceItem>> getByInvoiceId(long invoiceId);

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId ORDER BY id ASC")
    List<InvoiceItem> getByInvoiceIdOnce(long invoiceId);

    @Query("SELECT * FROM invoice_items WHERE id = :id LIMIT 1")
    InvoiceItem getById(long id);

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    void deleteByInvoiceId(long invoiceId);
}

