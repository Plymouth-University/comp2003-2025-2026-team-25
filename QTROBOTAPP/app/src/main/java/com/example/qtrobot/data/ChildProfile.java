package com.example.qtrobot.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "child_profiles",
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
    public long id;

    @ColumnInfo(name = "remote_id")
    public String remoteId;

    @ColumnInfo(name = "parent_id")
    public long parentId;

    @ColumnInfo(name = "preferred_name")
    public String preferredName;

    @ColumnInfo(name = "date_of_birth")
    public String dateOfBirth;

    @ColumnInfo(name = "avatar_uri")
    public String avatarUri;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @ColumnInfo(name = "is_dirty")
    public boolean isDirty;     // sync flag

    @ColumnInfo(name = "is_deleted")
    public boolean isDeleted;
}
