package com.example.qtrobot.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_rewards")
public class UserReward {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "asset_name")
    private String assetName;

    @ColumnInfo(name = "date_unlocked")
    private long dateUnlocked;

    @ColumnInfo(name = "is_displayed")
    private boolean isDisplayed;

    @ColumnInfo(name = "is_synced")
    private boolean isSynced;

    public UserReward() {
    }

    public UserReward(String userId, String assetName, long dateUnlocked, boolean isDisplayed, boolean isSynced) {
        this.userId = userId;
        this.assetName = assetName;
        this.dateUnlocked = dateUnlocked;
        this.isDisplayed = isDisplayed;
        this.isSynced = isSynced;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public long getDateUnlocked() {
        return dateUnlocked;
    }

    public void setDateUnlocked(long dateUnlocked) {
        this.dateUnlocked = dateUnlocked;
    }

    public boolean isDisplayed() {
        return isDisplayed;
    }

    public void setDisplayed(boolean displayed) {
        isDisplayed = displayed;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
