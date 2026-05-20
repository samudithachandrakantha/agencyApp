package com.hfad.agencyapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.lifecycle.LiveData;

import com.hfad.agencyapp.data.entities.Customer;

import java.util.List;

@Dao
public interface CustomerDao {
    @Insert
    long insert(Customer customer);

    @Update
    int update(Customer customer);

    @Delete
    int delete(Customer customer);

    @Query("SELECT * FROM customers ORDER BY name ASC")
    LiveData<List<Customer>> getAll();

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    Customer getById(long id);

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :name || '%'")
    List<Customer> searchByName(String name);
}

