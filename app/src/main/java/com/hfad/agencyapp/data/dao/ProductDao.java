package com.hfad.agencyapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.lifecycle.LiveData;

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
    LiveData<List<Product>> getAll();

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    Product getById(long id);

    @Query("SELECT * FROM products WHERE name LIKE :q OR sku LIKE :q OR brand LIKE :q ORDER BY name ASC")
    List<Product> search(String q);

    @Query("DELETE FROM products WHERE id = :id")
    int deleteById(long id);
}
