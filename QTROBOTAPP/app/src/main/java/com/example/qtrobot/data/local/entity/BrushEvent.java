package com.example.qtrobot.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "brush_events",
        foreignKeys = @ForeignKey(
                entity = ChildProfile.class,
                parentColumns = "id",
                childColumns = "child_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("child_id")}
)
public class BrushEvent {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "remote_id")
    public String remoteId;

    @ColumnInfo(name = "child_id")
    public long childId;

    @ColumnInfo(name = "brushed_at")
    public long brushedAt;      // timestamp (millis)

    @ColumnInfo(name = "duration_seconds")
    public int durationSeconds;

    public String source;       // "TIMER", "MANUAL"

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @ColumnInfo(name = "is_dirty")
    public boolean isDirty;

    @ColumnInfo(name = "is_deleted")
    public boolean isDeleted;
}
