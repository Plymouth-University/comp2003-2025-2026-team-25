package com.example.qtrobot.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * The Room database for this app.
 */
@Database(entities = {UserProfile.class, QuestLog.class, UserReward.class, QuestType.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "qtrobot_database";

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, DATABASE_NAME)
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not part of this exercise.
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }

    public abstract UserProfileDao userProfileDao();

    public abstract QuestLogDao questLogDao();

    public abstract UserRewardDao userRewardDao();

    public abstract QuestTypeDao questTypeDao();
}
