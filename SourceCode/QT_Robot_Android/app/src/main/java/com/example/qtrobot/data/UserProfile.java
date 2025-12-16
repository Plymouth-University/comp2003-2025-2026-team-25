package com.example.qtrobot.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profile")
public class UserProfile {

    //DEFAULT local ID for current user ()
    // Use this anywhere the app: UserProfile.DEFAULT_ID
    @Ignore
    public static final String DEFAULT_ID = "CURRENT_USER";

   // LOCAL PRIMARY KEY FOR A SINGLE USER
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "local_id")
    private String localId = DEFAULT_ID;

    // CLOUD PRIMARY KEY
    @ColumnInfo(name = "user_id")
    private String userId;

    // OTHER TABLE FIELDS HERE:
    @ColumnInfo(name = "username")
    private String username;

    @ColumnInfo(name = "avatar_image_file")
    private String avatarImageFile;

    @ColumnInfo(name = "robot_speech_speed")
    private float robotSpeechSpeed;

    @ColumnInfo(name = "robot_volume_level")
    private int robotVolumeLevel;

    @ColumnInfo(name = "app_theme_colour")
    private String appThemeColour;

    @ColumnInfo(name = "score_balance")
    private int scoreBalance;

    @ColumnInfo(name = "is_synced")
    private boolean isSynced;

    @ColumnInfo(name = "last_updated_at")
    private long lastUpdatedAt;

    public UserProfile() {
    }

    public UserProfile(@NonNull String localId, String userId, String username, String avatarImageFile, float robotSpeechSpeed, int robotVolumeLevel, String appThemeColour, int scoreBalance, boolean isSynced, long lastUpdatedAt) {
        this.localId = localId;
        this.userId = userId;
        this.username = username;
        this.avatarImageFile = avatarImageFile;
        this.robotSpeechSpeed = robotSpeechSpeed;
        this.robotVolumeLevel = robotVolumeLevel;
        this.appThemeColour = appThemeColour;
        this.scoreBalance = scoreBalance;
        this.isSynced = isSynced;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    @NonNull
    public String getLocalId() {
        return localId;
    }

    public void setLocalId(@NonNull String localId) {
        this.localId = localId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarImageFile() {
        return avatarImageFile;
    }

    public void setAvatarImageFile(String avatarImageFile) {
        this.avatarImageFile = avatarImageFile;
    }

    public float getRobotSpeechSpeed() {
        return robotSpeechSpeed;
    }

    public void setRobotSpeechSpeed(float robotSpeechSpeed) {
        this.robotSpeechSpeed = robotSpeechSpeed;
    }

    public int getRobotVolumeLevel() {
        return robotVolumeLevel;
    }

    public void setRobotVolumeLevel(int robotVolumeLevel) {
        this.robotVolumeLevel = robotVolumeLevel;
    }

    public String getAppThemeColour() {
        return appThemeColour;
    }

    public void setAppThemeColour(String appThemeColour) {
        this.appThemeColour = appThemeColour;
    }

    public int getScoreBalance() {
        return scoreBalance;
    }

    public void setScoreBalance(int scoreBalance) {
        this.scoreBalance = scoreBalance;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public long getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(long lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
