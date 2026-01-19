package com.example.qtrobot.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Purpose: Who is using the app?
// Entity that defines ParentAccount table in the RoomDB
@Entity(tableName = "parent_account")
public class ParentAccount {

    @PrimaryKey(autoGenerate = true)
    public long id;     // local PK

    @ColumnInfo(name = "remote_id")
    public String remoteId;     // server ID (nullable)

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;

    public String email;

    @ColumnInfo(name = "date_of_birth")
    public String dateOfBirth;

    @ColumnInfo(name = "password_token")
    public String passwordToken;   // NEVER store plain password

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @ColumnInfo(name = "is_dirty")
    public boolean isDirty;     // Cloud sync flag
}