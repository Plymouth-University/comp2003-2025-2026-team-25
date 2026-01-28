package com.example.qtrobot.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "learn_sections")
public class LearnSection {

    @PrimaryKey(autoGenerate = true)
    public long id;

    // e.g. "ARRIVAL", "BEFORE", "DURING", "AFTER"
    public String code;

    public String title;

    public String description;
}
