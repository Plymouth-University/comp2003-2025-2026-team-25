package com.example.qtrobot.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


// In case to be used instead of SharedPreferences

@Entity(tableName = "app_settings")
public class AppSettings {

    @PrimaryKey
    public long id = 1;

    @ColumnInfo(name = "notifications_enabled")
    public boolean notificationsEnabled;

    @ColumnInfo(name = "sound_enabled")
    public boolean soundEnabled;

    public String theme;

    @ColumnInfo(name = "last_version_seen")
    public int lastVersionSeen;
}

