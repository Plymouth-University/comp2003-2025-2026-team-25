package com.example.qtrobot.data.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.qtrobot.data.local.dao.ChildProfileDao;
import com.example.qtrobot.data.local.dao.ParentAccountDao;
import com.example.qtrobot.data.local.database.AppDatabase;
import com.example.qtrobot.data.local.entity.ChildProfile;
import com.example.qtrobot.data.local.entity.ParentAccount;

import com.example.qtrobot.data.repository.OnParentIdCallBack;


// code reference: https://stackoverflow.com/questions/64017799/how-to-use-executorservice-with-android-room

// Repository manages all data and operation, and the only class that should talk to DAOs.
// Defines where the app data comes from (local or network)
public class DataRepository {

    private final ParentAccountDao parentAccountDao;
    private final ChildProfileDao childProfileDao;

    // Constructor gets the DAOs from the database instance.
    // Passes the Application context to get the database instance.
    public DataRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.parentAccountDao = db.parentAccountDao();
        this.childProfileDao = db.childProfileDao();
    }

    // --- ParentAccount Methods ---
    public void insertParent(ParentAccount parentAccount, OnParentIdCallBack callback) {
        // Using existing background thread executor from our AppDatabase class
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if(parentAccount!=null)
            {
                long newParentId = parentAccountDao.insertParent(parentAccount);
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onParentIdReceived(newParentId);
                });
            }
            else {
                Log.e("DataRepository","parentAccount is null");
            }

        });
    }

    // --- ChildProfile Methods ---
    public void insertChild(final ChildProfile childProfile) {
        // Using existing executor from our AppDatabase class
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if(childProfile!=null)
            {
                childProfileDao.insertChild(childProfile);
            }
            else {
                Log.e("DataRepository","childProfile is null");
            }

        });
    }
}
