package com.example.qtrobot.data.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.qtrobot.data.local.dao.ChildProfileDao;
import com.example.qtrobot.data.local.dao.LearnProgressDao;
import com.example.qtrobot.data.local.dao.ParentAccountDao;
import com.example.qtrobot.data.local.database.AppRoomDatabase;
import com.example.qtrobot.data.local.entity.ChildProfile;
import com.example.qtrobot.data.local.entity.LearnProgress;
import com.example.qtrobot.data.local.entity.ParentAccount;

import com.example.qtrobot.data.remote.RetrofitClient;
import com.example.qtrobot.data.remote.api.BackendApi;
import com.example.qtrobot.data.remote.dto.GetChildResponse;
import com.example.qtrobot.data.remote.dto.ChildDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Callback;

// code reference https://google-developer-training.github.io/android-developer-fundamentals-course-concepts-v2/unit-4-saving-user-data/lesson-10-storing-data-with-room/10-1-c-room-livedata-viewmodel/10-1-c-room-livedata-viewmodel.html
// code reference 2: https://stackoverflow.com/questions/64017799/how-to-use-executorservice-with-android-room

// Repository manages all data and operation, and the only class that should talk to DAOs.
// Defines where the app data comes from (local or network)
// Repository is to implement the logic for deciding whether to fetch data from a network or use results cached in the database.
public class DataRepository {

    private final ParentAccountDao parentAccountDao;  // access to Room (local)
    private final ChildProfileDao childProfileDao;
    private final LearnProgressDao learnProgressDao;
    private final BackendApi backendApi;    // access to AWS (remote)
    public static DataRepository ourInstance;


    // Private constructor - initializing: the DAOs, API and DB instance.

    // Constructor gets the DAOs from the database instance. Passes the Application context to get the database instance.
    private DataRepository(Application application) {
        AppRoomDatabase db = AppRoomDatabase.getDatabaseInstance(application);
        this.parentAccountDao = db.parentAccountDao();
        this.childProfileDao = db.childProfileDao();
        this.learnProgressDao = db.learnProgressDao();
        this.backendApi = RetrofitClient.getInstance().getBackendApi();
        // This runs once when the app starts.
        // The Repository picks up its tools: Room DAO and Retrofit API.
    }

    // static method to get the singleton instance
    public static DataRepository getInstance(Application application) {
        if (ourInstance == null) {
            synchronized (DataRepository.class) {
                if (ourInstance == null) {
                    ourInstance = new DataRepository(application);
                }
            }
        }
        return ourInstance;
    }
    public static void init(Application application) {
        if (ourInstance == null) {
            ourInstance = new DataRepository(application);
        }
    }



    // --- ParentAccount Methods ---

    // INSERT parent to Room (offline)
    public void insertParent(ParentAccount parentAccount, OnParentIdCallBack callback) {
        // Using existing background thread executor from our AppRoomDatabase class
        AppRoomDatabase.databaseWriteExecutor.execute(() -> {
            if(parentAccount!=null)
            {
                //Clear existing profiles data
                parentAccountDao.deleteAllParents();

                //Insert new user
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

    public void clearAllLocalData() {
        AppRoomDatabase.databaseWriteExecutor.execute(() -> {
            parentAccountDao.deleteAllParents();
            childProfileDao.deleteAllChildren();
        });
    }

    // to login parent locally with email:
    public void loginParent(String email, String password, OnLoginCallback callback){
        AppRoomDatabase.databaseWriteExecutor.execute(() -> {
            ParentAccount parent = parentAccountDao.getParentByEmailAndPassword(email, password);

            new Handler(Looper.getMainLooper()).post(() -> {
                if (parent != null) {
                    callback.onSuccess(parent);
                    } else {
                    callback.onFailure("Invalid email or password");
                }
            });
        });
    }

    // --- ChildProfile Methods ---

    // INSERT child to Room (offline)
    public void insertChild(final ChildProfile childProfile) {
        // Using existing executor from our AppRoomDatabase class
        AppRoomDatabase.databaseWriteExecutor.execute(() -> {
            if(childProfile!=null)
            {
                // delete previous data profile
                childProfileDao.deleteAllChildren();
                // insert a new child
                childProfileDao.insertChild(childProfile);
            }
            else {
                Log.e("DataRepository","childProfile is null");
            }

        });
    }

    //READ/get child from Room (offline - cached logic)
    public LiveData<ChildProfile> getChild(String remoteId) {
        return childProfileDao.getChildByRemoteId(remoteId);
    }

    // get only the first child method
    public LiveData<ChildProfile> getLocalChild() {
        return childProfileDao.getFirstChild();
    }



    /*
    * REMOTE/CLOUD logic and SYNC methods
     */

    //SYNC: fetch up-to-date child data from AWS (via remote id)-> and save to Room
    public void refreshChild(String remoteChildId) {
        backendApi.getChild(remoteChildId).enqueue(new Callback<GetChildResponse>() {
            @Override
            public void onResponse(Call<GetChildResponse> call, Response<GetChildResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChildDto childDto = response.body().child;
                    ChildProfile childProfile = new ChildProfile();
                   childProfile.remoteId = childDto.childId;
                   childProfile.parentRemoteId = childDto.parentId;
                   childProfile.preferredName = childDto.preferred_name;
//                   childProfile.dateOfBirth = childDto.date_of_birth;
                   childProfile.avatarUri = childDto.avatar_uri;
                   childProfile.score = childDto.score;
                   childProfile.settingsFavouriteSong  = childDto.settings_favourite_song;
                   childProfile.settingsPreferredGreeting = childDto.settings_preferred_greeting;
                   childProfile.settingsVolumeLevel = childDto.settings_volume_level;
                   childProfile.createdAt = childDto.created_at;
                   childProfile.updatedAt = childDto.updated_at;
                   childProfile.isDeleted = childDto.is_deleted;
                   childProfile.isDirty = false;
                   childProfile.qr_string = response.body().qr_string;  //get QR string generated in AWS

                   // insert refreshed child object to Room
                   AppRoomDatabase.databaseWriteExecutor.execute(() -> {
                       childProfileDao.upsert(childProfile);
                   });

                }
            }
            @Override
            public void onFailure(Call<GetChildResponse> call, Throwable t) {
                Log.e("DataRepository", "refreshChild failed: " + t.getMessage());
            }
            });
        }

    // WRITE: Save child to Room immediately + send to AWS
    public void saveChild(ChildProfile child) {
        AppRoomDatabase.databaseWriteExecutor.execute(() ->
                childProfileDao.upsert(child)
        );

        // new data transfer object mapping for the AWS API (packing our data to be ready to send)
        // dto. ... is exact name in AWS table
        // child. ... is the exact name as local in Room DB
        ChildDto dto = new ChildDto();
        dto.childId = child.remoteId;
        dto.preferred_name = child.preferredName;
//        dto.date_of_birth = child.dateOfBirth;
        dto.avatar_uri = child.avatarUri;
        dto.score = child.score;
        dto.settings_favourite_song = child.settingsFavouriteSong;
        dto.settings_preferred_greeting = child.settingsPreferredGreeting;
        dto.settings_volume_level = child.settingsVolumeLevel;
        dto.created_at = child.createdAt;
        dto.updated_at = child.updatedAt;
        dto.is_deleted = child.isDeleted;

        // actually sends body to AWS (saves to AWS via POST request)
        backendApi.createChild(dto).enqueue(new Callback<GetChildResponse>() {

            @Override
            public void onResponse(Call<GetChildResponse> call, Response<GetChildResponse> response) {
                // isDirty sync flag = false if response is successful
                child.isDirty = !response.isSuccessful();
                if (response.isSuccessful() && response.body() != null) {
                    child.qr_string = response.body().qr_string;
                }
                //update locally
                AppRoomDatabase.databaseWriteExecutor.execute(() ->
                        childProfileDao.upsert(child)
                );
            }

            @Override
            public void onFailure(Call<GetChildResponse> call, Throwable t) {
                Log.e("DataRepository", "saveChild failed: " + t.getMessage());
                child.isDirty = true;
                // update locally only
                AppRoomDatabase.databaseWriteExecutor.execute(() ->
                        childProfileDao.upsert(child)
                );
            }
        });
    }

    /*
    * LearnProgress table methods
    * */

    // Functions for LOCAL Room: use to read/write locally

    public void recordProgress(long childId, String sectionId) {
        // run in background thread
        AppRoomDatabase.databaseWriteExecutor.execute(() -> {
            // check if record already exists
            LearnProgress existing = learnProgressDao.findProgress(childId, sectionId);

            if (existing != null && existing.completed) {
                return;     // Already recorded in Room, do nothing
            }

            // ternary code
            LearnProgress progress = (existing != null) ? existing : new LearnProgress();
            progress.childId = childId;
            progress.sectionId = sectionId;
            progress.completed = true;
            progress.updatedAt = System.currentTimeMillis();
            progress.isDirty = true;    // flags that it needs cloud sync

            // if record does not exist, set time creation
            if (existing == null) {
                progress.createdAt = System.currentTimeMillis();
            }

            learnProgressDao.upsert(progress);      //insert or update records in Room

        });
    }

    // method to return data of all completed sessions for a single child
    public LiveData<List<LearnProgress>> getCompletedSections(long childId) {
        return learnProgressDao.getCompletedSectionsLive(childId);
    }

    // plain list
    public List<LearnProgress> getCompletedSectionsList(long childId) {
        return learnProgressDao.getCompletedSectionsList(childId);
    }
    public LiveData<List<ChildProfile>> getChildrenByParent(long parentId) {
        return childProfileDao.getChildrenByParent(parentId);
    }


}
