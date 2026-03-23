package com.example.qtrobot.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.qtrobot.data.local.dao.ChildProfileDao;
import com.example.qtrobot.data.local.dao.LearnProgressDao;
import com.example.qtrobot.data.local.dao.ParentAccountDao;
import com.example.qtrobot.data.local.entity.ChildProfile;
import com.example.qtrobot.data.local.entity.LearnProgress;
import com.example.qtrobot.data.local.entity.ParentAccount;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// reference 1: https://developer.android.com/training/data-storage/room
// reference 2: https://developer.android.com/codelabs/android-room-with-a-view#7
// reference 3: https://dev.to/theplebdev/android-room-database-5an
// code reference 4: https://google-developer-training.github.io/android-developer-fundamentals-course-concepts-v2/unit-4-saving-user-data/lesson-10-storing-data-with-room/10-1-c-room-livedata-viewmodel/10-1-c-room-livedata-viewmodel.html#room

// class that holds the database and serves as the main access point for the underlying connection to your app's persisted, relational data.
// dev note: use AppRoomDatabase.databaseWriteExecutor.execute { ... } for all writes (insert/update/delete, sync tasks)

@Database(
        entities = {
                // Put all tables to expose here:
                ParentAccount.class,
                ChildProfile.class,
                LearnProgress.class
        }, version = 6, exportSchema = false)

public abstract class AppRoomDatabase extends RoomDatabase {

    // Put all DAOs to expose here:
    public abstract ParentAccountDao parentAccountDao();
    public abstract ChildProfileDao childProfileDao();
    public abstract LearnProgressDao learnProgressDao();



    //Singleton instance of the database for the whole app
    private static AppRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    public static AppRoomDatabase getDatabaseInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppRoomDatabase.class,
                                    "qtrobot.db"   // database file name
                            )
                            // Wipes and rebuilds instead of migrating
                            // if no Migration object.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
