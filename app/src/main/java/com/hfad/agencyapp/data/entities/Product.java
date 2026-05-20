package com.hfad.agencyapp.data.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class Product {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String brand;
    public double costPrice;
    public double sellingPrice;

    /** Buy this many units to qualify for free issue; 0 = no free-issue offer */
    public int buyQtyForFreeIssue;
    /** Free units given when the buy threshold is met */
    public int freeIssueQty;
    /** Discount percentage applied to this product (0–100) */
    public double discountPercent;

    /** Auto-generated code for search / internal use */
    public String sku;
    /** Default category when not chosen in UI (e.g. General) */
    public long categoryId;
    public int stock;
    public int lowStockThreshold;

    public long createdAt;
    public long updatedAt;
    public String syncStatus;

    @Ignore
    public String categoryName;

    public Product() {
    }

    /**
     * Creates a new product row with sensible defaults for stock and metadata.
     */
    public static Product createNew(String name, String brand, double costPrice, double sellingPrice,
                                    int buyQtyForFreeIssue, int freeIssueQty, double discountPercent,
                                    long categoryId, String sku) {
        Product p = new Product();
        p.name = name;
        p.brand = brand != null ? brand : "";
        p.costPrice = costPrice;
        p.sellingPrice = sellingPrice;
        p.buyQtyForFreeIssue = buyQtyForFreeIssue;
        p.freeIssueQty = freeIssueQty;
        p.discountPercent = discountPercent;
        p.categoryId = categoryId;
        p.sku = sku != null ? sku : "";
        p.stock = 0;
        p.lowStockThreshold = 5;
        long now = System.currentTimeMillis();
        p.createdAt = now;
        p.updatedAt = now;
        p.syncStatus = "LOCAL";
        return p;
    }
}
