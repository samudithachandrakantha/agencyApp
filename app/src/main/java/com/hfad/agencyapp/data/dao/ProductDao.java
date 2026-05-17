package com.hfad.agencyapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.hfad.agencyapp.data.entities.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    long insert(Product product);

    @Update
    int update(Product product);

    @Delete
    int delete(Product product);

    @Query("SELECT * FROM products ORDER BY name ASC")
    List<Product> getAll();

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    Product getById(long id);

    @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY name ASC")
    List<Product> getByCategoryId(long categoryId);
}

