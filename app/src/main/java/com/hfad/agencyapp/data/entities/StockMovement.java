package com.hfad.agencyapp.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "stock_movements",
        foreignKeys = @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "productId", onDelete = ForeignKey.CASCADE),
        indices = {@Index("productId")}
)
public class StockMovement {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long productId;
    public String type; // IN / OUT
    public int quantity;
    public String reason;
    public String notes;
    public long createdAt;

    public StockMovement(long productId, String type, int quantity, String reason, String notes) {
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
        this.reason = reason;
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }
}

