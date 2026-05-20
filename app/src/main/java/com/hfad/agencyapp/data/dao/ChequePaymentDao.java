package com.hfad.agencyapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.hfad.agencyapp.data.entities.ChequePayment;

import java.util.List;

@Dao
public interface ChequePaymentDao {
    @Insert
    long insert(ChequePayment chequePayment);

    @Update
    int update(ChequePayment chequePayment);

    @Delete
    int delete(ChequePayment chequePayment);

    @Query("DELETE FROM cheque_payments WHERE paymentId = :paymentId")
    void deleteByPaymentId(long paymentId);

    @Query("SELECT * FROM cheque_payments WHERE paymentId = :paymentId LIMIT 1")
    ChequePayment getByPaymentId(long paymentId);

    @Query("SELECT * FROM cheque_payments WHERE status = :status ORDER BY chequeDate DESC")
    List<ChequePayment> getByStatus(String status);

    @Query("SELECT * FROM cheque_payments WHERE id = :id LIMIT 1")
    ChequePayment getById(long id);
}

