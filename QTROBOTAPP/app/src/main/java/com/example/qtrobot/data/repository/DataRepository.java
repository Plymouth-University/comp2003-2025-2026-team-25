package com.example.qtrobot.data.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.qtrobot.data.local.dao.ChildProfileDao;
import com.example.qtrobot.data.local.dao.ParentAccountDao;
import com.example.qtrobot.data.local.database.AppDatabase;
import com.example.qtrobot.data.local.entity.ChildProfile;
import com.example.qtrobot.data.local.entity.ParentAccount;

import com.example.qtrobot.data.remote.RetrofitClient;
import com.example.qtrobot.data.remote.api.BackendApi;
import com.example.qtrobot.data.remote.dto.GetChildResponse;
import com.example.qtrobot.data.repository.OnParentIdCallBack;
import com.example.qtrobot.data.remote.dto.ChildDto;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Callback;



// code reference: https://stackoverflow.com/questions/64017799/how-to-use-executorservice-with-android-room
// Repository = the glue between remote and local data
// Repository manages all data and operation, and the only class that should talk to DAOs.
// Defines where the app data comes from (local or network)
public class DataRepository {

    // SECTION 1: the tools Repository "assistant" has

    // <-- access to Room (local) -->
    private final ParentAccountDao parentAccountDao;
    private final ChildProfileDao childProfileDao;

    // access to AWS (remote)
    private final BackendApi backendApi;

    // SECTION 2: SETTING UP REPOSITORY

    // Constructor gets the DAOs from the database instance.
    // Passes the Application context to get the database instance.
    public DataRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.parentAccountDao = db.parentAccountDao();
        this.childProfileDao = db.childProfileDao();
        this.backendApi = RetrofitClient.getInstance().getBackendApi();
        // This runs once when the app starts.
        // The Repository picks up its tools: Room DAO and Retrofit API.
    }


    // --- ParentAccount Methods ---
    public void insertParent(ParentAccount parentAccount, OnParentIdCallBack callback) {
        // Using existing background thread executor from our AppDatabase class
        AppDatabase.databaseWriteExecutor.execute(() -> {
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
        AppDatabase.databaseWriteExecutor.execute(() -> {
            parentAccountDao.deleteAllParents();
            childProfileDao.deleteAllChildren();
        });
    }

    // --- ChildProfile Methods ---

    // INSERT child to Room (offline)
    public void insertChild(final ChildProfile childProfile) {
        // Using existing executor from our AppDatabase class
        AppDatabase.databaseWriteExecutor.execute(() -> {
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

    //READ/get child from Room (offline)
    public LiveData<ChildProfile> getChild(String remoteId) {
        return childProfileDao.getChildByRemoteId(remoteId);
    }

    public LiveData<ChildProfile> getLocalChild() {
        return childProfileDao.getFirstChild();
    }


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
                   childProfile.dateOfBirth = childDto.date_of_birth;
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
                   AppDatabase.databaseWriteExecutor.execute(() -> {
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

    // <-- code section generated by AI Claude -->
    // WRITE: Save child to Room immediately + send to AWS
    public void saveChild(ChildProfile child) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                childProfileDao.upsert(child)
        );

        // new data transfer object mapping for the AWS API (packing our data to be ready to send)
        ChildDto dto = new ChildDto();
        dto.childId = child.remoteId;
        dto.preferred_name = child.preferredName;
        dto.date_of_birth = child.dateOfBirth;
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
                AppDatabase.databaseWriteExecutor.execute(() ->
                        childProfileDao.upsert(child)
                );
            }

            @Override
            public void onFailure(Call<GetChildResponse> call, Throwable t) {
                Log.e("DataRepository", "saveChild failed: " + t.getMessage());
                child.isDirty = true;
                // update locally only
                AppDatabase.databaseWriteExecutor.execute(() ->
                        childProfileDao.upsert(child)
                );
            }
        });
    }

}
