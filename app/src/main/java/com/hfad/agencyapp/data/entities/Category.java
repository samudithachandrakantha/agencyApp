package com.hfad.agencyapp.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;

    public String description;

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

