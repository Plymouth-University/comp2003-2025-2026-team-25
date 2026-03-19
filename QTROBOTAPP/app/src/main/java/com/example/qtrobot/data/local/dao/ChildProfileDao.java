package com.example.qtrobot.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.qtrobot.data.local.entity.ChildProfile;

import java.util.List;

@Dao
public interface ChildProfileDao {
    //@Query("SELECT * FROM child_profile LIMIT 1")
    @Query("SELECT * FROM child_profile LIMIT 1")
    LiveData<ChildProfile> getSingleChild();
    // Note: LiveData Auto-updates UI when Room data changes — no polling, no manual refresh.


    @Query("DELETE FROM child_profile")void deleteAllChildren();

    // If need to access by id:
    // @Query("SELECT * FROM child_profile WHERE id = :childId LIMIT 1")
    @Query("SELECT * FROM child_profile WHERE id = :childId LIMIT 1")
    LiveData<ChildProfile> getChildById(long childId);

    @Query("SELECT * FROM child_profile WHERE remote_id = :remoteId LIMIT 1")
    LiveData<ChildProfile> getChildByRemoteId(String remoteId);


    @Query("SELECT * FROM child_profile LIMIT 1")
    LiveData<ChildProfile> getFirstChild(); // single child assumption

    // all children for a parent
    @Query("SELECT * FROM child_profile WHERE parent_id = :parentId")
    LiveData<List<ChildProfile>> getChildrenByParent(long parentId);


    // insert new child profile (use when first time creating child)
    @Insert
    void insertChild(ChildProfile childProfile);

    // use it when not sure if child exists in RoomDB (eg., to update from AWS)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ChildProfile childProfile);

    //update child's profile (if you know child exists in Room)
    @Update
    int updateChild(ChildProfile childProfile);

    @Delete
    int deleteChild(ChildProfile childProfile);

    @Query("SELECT * FROM child_profile WHERE is_dirty = 1")
    List<ChildProfile> getUnsyncedChildren();

}
