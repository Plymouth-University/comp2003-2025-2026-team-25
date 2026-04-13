package com.example.qtrobot.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "learn_sections")
public class LearnSection {

    @PrimaryKey(autoGenerate = false)   // IDs are set manually in the TutorialManager
    @SerializedName("id")
    public long id;


    @SerializedName("category")
    public String category;     // e.g. "ARRIVAL", "BEFORE", "DURING", "AFTER"

    public String title;
    public String videoUrl;
    public String description;
}
