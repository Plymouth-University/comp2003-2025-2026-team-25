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
    @Query("SELECT * FROM parent_account")
    ParentAccount getSingleParent();

    @Query("DELETE FROM parent_account") // Delete all local parent accounts (for logout logic)
    void deleteAllParents();

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

    // For email login — find parent by email and password
    @Query("SELECT * FROM parent_account WHERE email = :email AND password_token = :password LIMIT 1")
    ParentAccount getParentByEmailAndPassword(String email, String password);





}
