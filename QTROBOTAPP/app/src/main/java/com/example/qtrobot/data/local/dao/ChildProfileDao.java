package com.example.qtrobot.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.qtrobot.data.local.entity.ChildProfile;
import com.example.qtrobot.data.local.entity.ParentAccount;

import java.util.List;

@Dao
public interface ChildProfileDao {
    //@Query("SELECT * FROM child_profile LIMIT 1")
    @Query("SELECT * FROM child_profile")
    LiveData<ChildProfile> getSingleChild();
    // Note: LiveData Auto-updates UI when Room data changes — no polling, no manual refresh.


    @Query("DELETE FROM child_profile")void deleteAllChildren();

    // If need to access by id:
    // @Query("SELECT * FROM child_profile WHERE id = :childId LIMIT 1")
    @Query("SELECT * FROM child_profile WHERE id = :childId")
    LiveData<ChildProfile> getChildById(long childId);

    @Query("SELECT * FROM child_profile WHERE remote_id = :remoteId")
    LiveData<ChildProfile> getChildByRemoteId(String remoteId);

    // get the child which needs to be synced to cloud
    //@Query("SELECT * FROM child_profile WHERE is_dirty = 1 LIMIT 1")
    @Query("SELECT * FROM child_profile WHERE is_dirty = 1")
    ChildProfile getUnsyncedChild(); // no need for live data as used on background, not UI

    @Query("SELECT * FROM child_profile LIMIT 1")
    LiveData<ChildProfile> getFirstChild(); // single child assumption

    @Query("SELECT * FROM child_profile LIMIT 1")
    ChildProfile getFirstChildSync(); // synchronous, for background thread use

    @Query("UPDATE child_profile SET settings_preferred_greeting = :mood, updated_at = :updatedAt, is_dirty = 1 WHERE id = :childId")
    void updateChildMood(long childId, String mood, long updatedAt);

    /** Returns the first child belonging to the given local parent id. */
    @Query("SELECT * FROM child_profile WHERE parent_id = :parentId LIMIT 1")
    LiveData<ChildProfile> getChildByParentId(long parentId);

    @Query("SELECT * FROM child_profile WHERE parent_id = :parentId ORDER BY created_at ASC")
    LiveData<List<ChildProfile>> getChildrenForParent(long parentId);

    @Query("SELECT COUNT(*) FROM child_profile WHERE parent_id = :parentId")
    int countChildrenForParent(long parentId);

    @Query("SELECT COUNT(*) FROM child_profile WHERE parent_id IS NULL")
    int countChildrenForGuest();

    @Query("SELECT * FROM child_profile WHERE parent_id = :parentId ORDER BY created_at ASC LIMIT 1")
    ChildProfile getFirstChildForParentSync(long parentId);

    @Query("SELECT * FROM child_profile WHERE parent_id IS NULL ORDER BY created_at ASC LIMIT 1")
    ChildProfile getFirstGuestChildSync();

    @Query("SELECT * FROM child_profile WHERE parent_id IS NULL ORDER BY created_at ASC")
    LiveData<List<ChildProfile>> getChildrenForGuest();

    @Query("SELECT COUNT(*) FROM child_profile WHERE parent_id = :parentId AND LOWER(child_username) = LOWER(:username)")
    int countUsernameForParent(long parentId, String username);

    @Query("SELECT COUNT(*) FROM child_profile WHERE parent_id IS NULL AND LOWER(child_username) = LOWER(:username)")
    int countUsernameForGuest(String username);

    @Query("SELECT * FROM child_profile WHERE id = :childId LIMIT 1")
    ChildProfile getChildByIdSync(long childId);

    // insert new child profile (use when first time creating child)
    @Insert
    long insertChild(ChildProfile childProfile);

    // use it when not sure if child exists in RoomDB (eg., to update from AWS)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ChildProfile childProfile);

    //update child's profile (if you know child exists in Room)
    @Update
    int updateChild(ChildProfile childProfile);

    @Delete
    int deleteChild(ChildProfile childProfile);
}
