package com.hfad.agencyapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.hfad.agencyapp.data.entities.StockMovement;

import java.util.List;

@Dao
public interface StockMovementDao {
    @Insert
    long insert(StockMovement movement);

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY createdAt DESC LIMIT :limit")
    List<StockMovement> getRecentByProduct(long productId, int limit);
}

