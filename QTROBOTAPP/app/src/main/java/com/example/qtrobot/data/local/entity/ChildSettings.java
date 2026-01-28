package com.example.qtrobot.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;


// Purpose: this is Robot specific settings for a child profile which must to be synced with Robot
@Entity(
        tableName = "child_settings",
        foreignKeys = @ForeignKey(
                entity = ChildProfile.class,
                parentColumns = "id",
                childColumns = "child_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("child_id")}
)
public class ChildSettings {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "child_id")
    public long childId;

    @ColumnInfo(name = "favourite_song")
    public String favouriteSong;     // name or ID

    @ColumnInfo(name = "preferred_greeting")
    public String preferredGreeting;

    @ColumnInfo(name = "volume_level")
    public String volumeLevel;  // "MUTED", "LOW", "MEDIUM", "HIGH" .....
    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @ColumnInfo(name = "is_dirty")
    public boolean isDirty; // cloud sync flag
}

