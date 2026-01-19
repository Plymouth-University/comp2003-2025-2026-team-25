package com.example.qtrobot.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "child_learn_progress",
        foreignKeys = {
                @ForeignKey(
                        entity = ChildProfile.class,
                        parentColumns = "id",
                        childColumns = "child_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = LearnSection.class,
                        parentColumns = "id",
                        childColumns = "section_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("child_id"), @Index("section_id")}
)
public class ChildLearnProgress {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "child_id")
    public long childId;

    @ColumnInfo(name = "section_id")
    public long sectionId;

    @ColumnInfo(name = "last_viewed_step_order")
    public int lastViewedStepOrder;

    public boolean completed;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @ColumnInfo(name = "is_dirty")
    public boolean isDirty; // sync flag
}
