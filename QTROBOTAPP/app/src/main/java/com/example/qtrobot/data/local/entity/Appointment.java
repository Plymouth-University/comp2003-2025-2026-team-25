package com.example.qtrobot.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "appointments",
        foreignKeys = @ForeignKey(
                entity = ChildProfile.class,
                parentColumns = "id",
                childColumns = "child_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("child_id")}
)
public class Appointment {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "remote_id")
    public String remoteId;

    @ColumnInfo(name = "child_id")
    public long childId;

    @ColumnInfo(name = "dentist_id")
    public String dentistId;    // remote dentist ID

    @ColumnInfo(name = "scheduled_at")
    public long scheduledAt;    // date+time millis

    public String location;

    public String details;

    public String status;  // "UPCOMING", "COMPLETED", etc

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @ColumnInfo(name = "is_dirty")
    public boolean isDirty; // cloud sync flag

    @ColumnInfo(name = "is_deleted")
    public boolean isDeleted;
}
