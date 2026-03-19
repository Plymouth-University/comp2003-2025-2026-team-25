package com.example.qtrobot.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

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

    @SerializedName("childId")
    @ColumnInfo(name = "remote_id")
    public String remoteId;     // cloud unique ID

    @SerializedName("parentId")
    @ColumnInfo(name = "parent_id")
    public long parentId;   // FK -> ParentAccount.id

    @ColumnInfo(name = "parent_remote_id")
    public String parentRemoteId;

    @ColumnInfo(name = "qr_string")
    public String qr_string;

    @SerializedName("preferred_name")
    @ColumnInfo(name = "preferred_name")
    public String preferredName;    // e.g., Alex

//    @SerializedName("date_of_birth")
//    @ColumnInfo(name = "date_of_birth")
//    public String dateOfBirth;      //e.g. "2020-03-15"

    @SerializedName("avatar_uri")
    @ColumnInfo(name = "avatar_uri")  // uri = link to the resource
    public String avatarUri;    // e.g. "content://app/avatars/bunny.png" or "res://avatar_boy"

    @SerializedName("score")
    @ColumnInfo(name = "score", defaultValue = "0")
    public int score; // Total score earned by the child

    @SerializedName("settings_favourite_song")
    @ColumnInfo(name = "settings_favourite_song")
    public String settingsFavouriteSong;

    @SerializedName("settings_preferred_greeting")
    @ColumnInfo(name = "settings_preferred_greeting")
    public String settingsPreferredGreeting;

    @SerializedName("settings_volume_level")
    @ColumnInfo(name = "settings_volume_level")
    public String settingsVolumeLevel;


    @SerializedName("created_at")
    @ColumnInfo(name = "created_at")
    public long createdAt;

    @SerializedName("updated_at")
    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @ColumnInfo(name = "is_dirty")
    public boolean isDirty;     // sync flag. True if changed locally

    @ColumnInfo(name = "is_deleted")
    public boolean isDeleted;      // false. True if user removed locally and needs to be deleted from cloud next.
}
