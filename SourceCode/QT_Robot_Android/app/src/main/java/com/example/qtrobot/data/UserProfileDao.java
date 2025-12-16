package com.example.qtrobot.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserProfileDao {
    //Save User. If already exists with "CURRENT_USER" ID - overwrite it
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setProfile(UserProfile user);

    //Read Profile data live to display on UI
    @Query("SELECT * FROM user_profile WHERE local_id = 'CURRENT_USER'")
    LiveData<UserProfile> getLiveProfile();

    //For Logic
    @Query("SELECT * FROM user_profile WHERE local_id = 'CURRENT_USER'")
    UserProfile getProfileSync();
}
