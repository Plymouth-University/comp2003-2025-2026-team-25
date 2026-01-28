package com.example.qtrobot.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.qtrobot.data.local.entity.ParentAccount;

@Dao
public interface ParentAccountDao {

    //Get a single parent on this device
    @Query("SELECT * FROM parent_account LIMIT 1")
    ParentAccount getSingleParent();

    // insert a new parent profile
    @Insert
    long insertParent(ParentAccount parentAccount);

    //update parent account
    @Update
    int updateParentAccount(ParentAccount parentAccount);

    @Delete
    int deleteParentAccount(ParentAccount parentAccount);

    //get parent account which needs sync to the cloud
    @Query("SELECT * FROM parent_account WHERE is_dirty = 1 LIMIT 1")
    ParentAccount getUnsyncedParent();

}
