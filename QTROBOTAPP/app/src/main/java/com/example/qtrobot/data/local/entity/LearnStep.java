package com.example.qtrobot.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "learn_step",
        foreignKeys = @ForeignKey(
                entity = LearnSection.class,
                parentColumns = "id",
                childColumns = "section_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("section_id")}
)
public class LearnStep {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "section_id") //eg, ARRIVAL, BEFORE, DURING, AFTER
    public long sectionId;

    public int order;  // step order in sequence, eg. 1,2,3,...

    public String title;

    public String body;

    @ColumnInfo(name = "image_asset_name")
    public String imageAssetName;    // optional
}
