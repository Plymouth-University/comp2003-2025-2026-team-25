package com.example.qtrobot.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quest_logs")
public class QuestLog {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "log_id")
    private long logId;

    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "quest_id")
    private String questId;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @ColumnInfo(name = "mood_rating")
    private int moodRating;

    @ColumnInfo(name = "duration_seconds")
    private int durationSeconds;

    @ColumnInfo(name = "scores_earned")
    private int scoresEarned;

    @ColumnInfo(name = "is_synced")
    private boolean isSynced;

    public QuestLog() {
    }

    public QuestLog(String userId, String questId, long timestamp, int moodRating, int durationSeconds, int scoresEarned, boolean isSynced) {
        this.userId = userId;
        this.questId = questId;
        this.timestamp = timestamp;
        this.moodRating = moodRating;
        this.durationSeconds = durationSeconds;
        this.scoresEarned = scoresEarned;
        this.isSynced = isSynced;
    }

    //Getters and Setters

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQuestId() {
        return questId;
    }

    public void setQuestId(String questId) {
        this.questId = questId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getMoodRating() {
        return moodRating;
    }

    public void setMoodRating(int moodRating) {
        this.moodRating = moodRating;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public int getScoresEarned() {
        return scoresEarned;
    }

    public void setScoresEarned(int scoresEarned) {
        this.scoresEarned = scoresEarned;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
