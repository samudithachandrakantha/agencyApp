package com.hfad.agencyapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.hfad.agencyapp.data.entities.Payment;

import java.util.List;

@Dao
public interface PaymentDao {
    @Insert
    long insert(Payment payment);

    @Update
    int update(Payment payment);

    @Delete
    int delete(Payment payment);

    @Query("SELECT * FROM payments WHERE invoiceId = :invoiceId ORDER BY timestamp DESC")
    List<Payment> getByInvoiceId(long invoiceId);

    @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
    Payment getById(long id);

    @Query("SELECT SUM(amount) FROM payments WHERE invoiceId = :invoiceId")
    double getTotalPaidByInvoiceId(long invoiceId);
}

