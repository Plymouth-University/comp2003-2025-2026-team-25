package com.example.qtrobot.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quest_types")
public class QuestType {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "quest_id")
    private String questId;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "reward_score")
    private int rewardScore;

    @ColumnInfo(name = "icon_name")
    private String iconName;

    @ColumnInfo(name = "anim_name")
    private String animName;

    public QuestType() {
    }

    public QuestType(@NonNull String questId, String title, String description, int rewardScore, String iconName, String animName) {
        this.questId = questId;
        this.title = title;
        this.description = description;
        this.rewardScore = rewardScore;
        this.iconName = iconName;
        this.animName = animName;
    }

    @NonNull
    public String getQuestId() {
        return questId;
    }

    public void setQuestId(@NonNull String questId) {
        this.questId = questId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRewardScore() {
        return rewardScore;
    }

    public void setRewardScore(int rewardScore) {
        this.rewardScore = rewardScore;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getAnimName() {
        return animName;
    }

    public void setAnimName(String animName) {
        this.animName = animName;
    }
}
