package com.example.qtrobot.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "learn_progress",
        foreignKeys = {
                @ForeignKey(
                        entity = ChildProfile.class,
                        parentColumns = "id",
                        childColumns = "child_id",
                        onDelete = ForeignKey.CASCADE
                )
                //section IDs are managed via LearnSectionConstants
        },
        indices = {@Index("child_id"), @Index("section_id")}
)
public class LearnProgress {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "remote_id")
    public String remoteId; // The ID from AWS/Cloud database

    @ColumnInfo(name = "child_id")
    public long childId;

    @ColumnInfo(name = "section_id") // which tutorial watched
    public String sectionId;

    @ColumnInfo(name = "is_completed")
    public boolean completed;

    @ColumnInfo(name = "score")
    public int score;

    @ColumnInfo(name = "created_at") // when first watched
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @ColumnInfo(name = "is_dirty")
    public boolean isDirty; // sync flag
}
