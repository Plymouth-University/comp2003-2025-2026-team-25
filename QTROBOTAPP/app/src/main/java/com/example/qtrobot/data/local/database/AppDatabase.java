package com.example.qtrobot.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.qtrobot.data.local.dao.ChildProfileDao;
import com.example.qtrobot.data.local.dao.ParentAccountDao;
import com.example.qtrobot.data.local.entity.ChildProfile;
import com.example.qtrobot.data.local.entity.LearnSection;
import com.example.qtrobot.data.local.entity.ParentAccount;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// reference: https://developer.android.com/training/data-storage/room
//reference 2: https://developer.android.com/codelabs/android-room-with-a-view#7
// class that holds the database and serves as the main access point for the underlying connection to your app's persisted, relational data.

// dev note: use AppDatabase.databaseWriteExecutor.execute { ... } for all writes (insert/update/delete, sync tasks)
@Database(
        entities = {
                ParentAccount.class,
                ChildProfile.class
        }, version = 1, exportSchema = false)

public abstract class AppDatabase extends RoomDatabase {


    // DAO to expose
    public abstract ParentAccountDao parentAccountDao();
    public abstract ChildProfileDao childProfileDao();

    //Singleton instance of the database for the whole app
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class,
                                    "qtrobot.db"   // database file name
                            )
                            .build();
                }
            }
        }
        return INSTANCE;
    }


}
