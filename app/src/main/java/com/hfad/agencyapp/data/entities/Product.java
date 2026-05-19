package com.hfad.agencyapp.data.entities;

import androidx.room.Ignore;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class Product {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String sku;
    public long categoryId;
    public String brand;
    public String description;
    public double sellingPrice;
    public double mrp;
    public double costPrice;
    public double taxRate;
    public int stock;
    public String unit;
    public int lowStockThreshold;
    public String location;
    public String imagePath;
    public long createdAt;
    public long updatedAt;
    public String syncStatus;

    @Ignore
    public String categoryName;

    public Product() {
    }

    public Product(String name, String sku, long categoryId, double sellingPrice, int stock, String imagePath) {
        this.name = name;
        this.sku = sku;
        this.categoryId = categoryId;
        this.sellingPrice = sellingPrice;
        this.mrp = sellingPrice;
        this.stock = stock;
        this.unit = "pcs";
        this.lowStockThreshold = 20;
        this.imagePath = imagePath;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.syncStatus = "LOCAL";
    }

    public Product(String name, String sku, long categoryId, String brand, String description,
                   double sellingPrice, double mrp, double costPrice, double taxRate,
                   int stock, String unit, int lowStockThreshold, String location, String imagePath) {
        this.name = name;
        this.sku = sku;
        this.categoryId = categoryId;
        this.brand = brand;
        this.description = description;
        this.sellingPrice = sellingPrice;
        this.mrp = mrp;
        this.costPrice = costPrice;
        this.taxRate = taxRate;
        this.stock = stock;
        this.unit = unit;
        this.lowStockThreshold = lowStockThreshold;
        this.location = location;
        this.imagePath = imagePath;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.syncStatus = "LOCAL";
    }
}

