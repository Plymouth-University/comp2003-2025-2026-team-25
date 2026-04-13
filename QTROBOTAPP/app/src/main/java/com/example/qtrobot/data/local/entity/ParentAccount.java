package com.example.qtrobot.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

// Purpose: Who is using the app?
// Entity that defines ParentAccount table in the RoomDB
@Entity(tableName = "parent_account")
public class ParentAccount {

    @PrimaryKey(autoGenerate = true)
    public long id;     // local PK

    @SerializedName("parentId")
    @ColumnInfo(name = "remote_id")
    public String remoteId;     // server ID (nullable) e.g, parent_9f23ab

    @SerializedName("first_name")
    @ColumnInfo(name = "first_name")
    public String firstName;    //e.g., Alice

    @SerializedName("last_name")
    @ColumnInfo(name = "last_name")
    public String lastName;     //e.g. Smith

    @SerializedName("email")
    public String email;    //e.g., "alice.smith@example.com"

    @ColumnInfo(name = "password_token")
    public String passwordToken;   // NEVER store plain password. something like hashed password string.

    @SerializedName("created_at")
    @ColumnInfo(name = "created_at")
    public long createdAt;      // when account was created

    @SerializedName("updated_at")
    @ColumnInfo(name = "updated_at")
    public long updatedAt;      // when account was last updated


    @ColumnInfo(name = "is_dirty")
    public boolean isDirty;     // Cloud sync flag. If was was edited/created offline, - True - must be synced
}