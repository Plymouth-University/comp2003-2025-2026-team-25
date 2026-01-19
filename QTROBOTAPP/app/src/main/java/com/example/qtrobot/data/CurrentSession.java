package com.example.qtrobot.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


// Dev note: integrate only when we have one parent - many children profiles scenario
@Entity(tableName = "current_session")
public class CurrentSession {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "current_parent_id")
    public Long currentParentId; // nullable
    // which parent account is logged in right now

    @ColumnInfo(name = "current_child_id")
    public Long currentChildId;  // nullable
    // which child profile is currently selected on the screen

    @ColumnInfo(name = "last_login_at")
    public long lastLoginAt; // when this session was started, useful for analytics or auto‑logout.
}
