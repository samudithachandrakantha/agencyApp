package com.hfad.agencyapp.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class Product {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String sku;
    public long categoryId;
    public double price;
    public int stock;
    public String imageUrl;
    public long createdAt;

    public Product(String name, String sku, long categoryId, double price, int stock, String imageUrl) {
        this.name = name;
        this.sku = sku;
        this.categoryId = categoryId;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.createdAt = System.currentTimeMillis();
    }
}

