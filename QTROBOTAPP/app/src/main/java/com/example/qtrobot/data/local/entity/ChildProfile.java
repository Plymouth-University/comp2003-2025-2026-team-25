package com.example.qtrobot.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "child_profile",
        foreignKeys = @ForeignKey(
                entity = ParentAccount.class,
                parentColumns = "id",
                childColumns = "parent_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("parent_id")}
)

public class ChildProfile {

    @PrimaryKey(autoGenerate = true)
    public long id; // local profile id, eg., 3

    @ColumnInfo(name = "remote_id")
    public String remoteId;     // cloud unique ID

    @ColumnInfo(name = "parent_id")
    public long parentId;   // FK -> ParentAccount.id

    @ColumnInfo(name = "preferred_name")
    public String preferredName;    // e.g., Alex

    @ColumnInfo(name = "date_of_birth")
    public String dateOfBirth;      //e.g. "2020-03-15"

    @ColumnInfo(name = "avatar_uri")  // uri = link to the resource
    public String avatarUri;    // e.g. "content://app/avatars/bunny.png" or "res://avatar_boy"

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @ColumnInfo(name = "is_dirty")
    public boolean isDirty;     // sync flag. True if changed locally

    @ColumnInfo(name = "is_deleted")
    public boolean isDeleted;      // false. True if user removed locally and needs to be deleted from cloud next.
}
