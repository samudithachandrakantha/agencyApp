package com.hfad.agencyapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.lifecycle.LiveData;

import com.hfad.agencyapp.data.entities.Category;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    long insert(Category category);

    @Update
    int update(Category category);

    @Delete
    int delete(Category category);

    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<Category>> getAll();

    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<Category> getAllOnce();

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    Category getById(long id);
}

