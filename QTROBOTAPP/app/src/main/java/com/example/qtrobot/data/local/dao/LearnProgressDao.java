package com.example.qtrobot.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.qtrobot.data.local.entity.LearnProgress;

import java.util.List;

// code reference: https://google-developer-training.github.io/android-developer-fundamentals-course-concepts-v2/unit-4-saving-user-data/lesson-10-storing-data-with-room/10-1-c-room-livedata-viewmodel/10-1-c-room-livedata-viewmodel.html#dao
@Dao
public interface LearnProgressDao {
    // The conflict strategy defines what happens,
    // if there is an existing entry.
    // The default action is ABORT.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LearnProgress progress);

    // Update multiple entries with one call. (can pass multiple object IDs at once for update)
    @Update
    void updateProgress(LearnProgress progress);

    // Delete multiple entries with one call
    @Query("DELETE FROM learn_progress")
    void deleteAllProgress();

    // Simple query without parameters that returns values.
    @Query("SELECT * from learn_progress ORDER BY id ASC")
    List<LearnProgress> getAllProgress();

    // All tutorials completed by one child (for progress overview screen or similar)
    @Query("SELECT * FROM learn_progress WHERE child_id = :childId")
    List<LearnProgress> getAllProgressForChild(long childId);

    // Check if a child has already completed a specific tutorial
    @Query("SELECT * FROM learn_progress WHERE child_id = :childId AND section_id = :sectionId LIMIT 1")
    LearnProgress findProgress(long childId, String sectionId);

    // Calculate total score for a child
    @Query("SELECT SUM(score) FROM learn_progress WHERE child_id = :childId")
    int getTotalScore(long childId);

    // Count completed tutorials for a child (e.g. "3 of 5 completed")
    @Query("SELECT COUNT(*) FROM learn_progress WHERE child_id = :childId AND is_completed = 1")
    int getCompletedCount(long childId);

    // Fetch all records that need pushing to DynamoDB
    @Query("SELECT * FROM learn_progress WHERE is_dirty = 1")
    List<LearnProgress> getDirtyRecords();

    // updates or inserts if does not exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(LearnProgress progress);

    // LiveData, takes a new list whenever any progress row for this child changes
    @Query("SELECT * FROM learn_progress WHERE child_id = :childId AND is_completed = 1")
    LiveData<List<LearnProgress>> getCompletedSectionsLive(long childId);

    // Returns only completed sections for a child — plain List, no LiveData
    @Query("SELECT * FROM learn_progress WHERE child_id = :childId AND is_completed = 1")
    List<LearnProgress> getCompletedSectionsList(long childId);

}
