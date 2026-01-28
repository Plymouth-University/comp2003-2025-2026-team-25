package com.example.qtrobot.data.local.dao;

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
    @Query("SELECT * FROM child_profile LIMIT 1")
    ChildProfile getSingleChild();

    // If need to access by id:
    @Query("SELECT * FROM child_profile WHERE id = :childId LIMIT 1")
    ChildProfile getChildById(long childId);

    // get the child which needs to be synced to cloud
    @Query("SELECT * FROM child_profile WHERE is_dirty = 1 LIMIT 1")
    ChildProfile getUnsyncedChild();

    // insert new child profile
    @Insert
    void insertChild(ChildProfile childProfile);

    //update child's profile
    @Update
    int updateChild(ChildProfile childProfile);

    @Delete
    int deleteChild(ChildProfile childProfile);
}
