package com.hfad.agencyapp.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "customers")
public class Customer {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String phone;
    public String email;
    public String address;
    public long createdAt;

    public Customer(String name, String phone, String email, String address) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.createdAt = System.currentTimeMillis();
    }
}

