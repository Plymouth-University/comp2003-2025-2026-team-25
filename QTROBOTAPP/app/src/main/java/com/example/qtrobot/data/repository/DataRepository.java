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
    private static volatile DataRepository INSTANCE;

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
        // Pass context so RetrofitClient can attach the JWT auth header
        this.backendApi = RetrofitClient.getInstance(application).getBackendApi();
        // This runs once when the app starts.
        // The Repository picks up its tools: Room DAO and Retrofit API.
    }

    public static DataRepository getInstance(Application application) {
        if (INSTANCE == null) {
            synchronized (DataRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DataRepository(application);
                }
            }
        }
        return INSTANCE;
    }


    // --- ParentAccount Methods ---

    /**
     * Upsert a Google-authenticated parent.
     * If a ParentAccount with the same email already exists in Room, we reuse it
     * (avoids duplicate rows on every login). Otherwise a new row is inserted.
     * The callback always receives the local Room id so the caller can persist the session.
     */
    public void upsertGoogleParent(ParentAccount parentAccount, OnParentIdCallBack callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (parentAccount == null || parentAccount.email == null) {
                Log.e("DataRepository", "upsertGoogleParent: parentAccount or email is null");
                return;
            }
            ParentAccount existing = parentAccountDao.getParentByEmail(parentAccount.email);
            long localId;
            if (existing != null) {
                // Update name / token fields in case they changed
                existing.firstName    = parentAccount.firstName;
                existing.lastName     = parentAccount.lastName;
                existing.passwordToken = parentAccount.passwordToken;
                existing.updatedAt    = System.currentTimeMillis();
                existing.isDirty      = parentAccount.isDirty;
                parentAccountDao.updateParentAccount(existing);
                localId = existing.id;
            } else {
                localId = parentAccountDao.insertParent(parentAccount);
            }
            final long finalId = localId;
            new Handler(Looper.getMainLooper()).post(() -> callback.onParentIdReceived(finalId));
        });
    }

    public void insertParent(ParentAccount parentAccount, OnParentIdCallBack callback) {
        // Using existing background thread executor from our AppDatabase class
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if(parentAccount!=null)
            {
                if (parentAccount.email != null) {
                    parentAccount.email = parentAccount.email.trim().toLowerCase(java.util.Locale.ROOT);
                }

                ParentAccount existing = parentAccount.email == null
                        ? null
                        : parentAccountDao.getParentByEmail(parentAccount.email);
                long newParentId;
                if (existing != null) {
                    existing.firstName = parentAccount.firstName;
                    existing.lastName = parentAccount.lastName;
                    existing.passwordToken = parentAccount.passwordToken;
                    existing.updatedAt = System.currentTimeMillis();
                    existing.isDirty = parentAccount.isDirty;
                    parentAccountDao.updateParentAccount(existing);
                    newParentId = existing.id;
                } else {
                    // Insert new user without clearing other local accounts
                    newParentId = parentAccountDao.insertParent(parentAccount);
                }
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

    public void loginParent(String email, String password, OnLoginCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            String normalizedEmail = email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
            ParentAccount parent = parentAccountDao.getParentByEmail(normalizedEmail);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (parent == null) {
                    callback.onFailure("Account not found");
                    return;
                }
                String savedPassword = parent.passwordToken == null ? "" : parent.passwordToken;
                if (!savedPassword.equals(password)) {
                    callback.onFailure("Invalid email or password");
                    return;
                }
                callback.onSuccess(parent);
            });
        });
    }

    // --- ChildProfile Methods ---

    // INSERT child to Room (offline)
    public void updateChildMood(long childId, String mood) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                childProfileDao.updateChildMood(childId, mood, System.currentTimeMillis())
        );
    }

    /** Inserts a new child without removing existing siblings. */
    public void insertChild(final ChildProfile childProfile) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (childProfile != null) {
                childProfileDao.insertChild(childProfile);
            } else {
                Log.e("DataRepository", "childProfile is null");
            }
        });
    }

    /** Inserts a child and returns the new row id on the main thread. */
    public void insertChildAndNotify(final ChildProfile childProfile, OnChildRowIdCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (childProfile == null) {
                Log.e("DataRepository", "insertChildAndNotify: childProfile is null");
                return;
            }
            long rowId = childProfileDao.insertChild(childProfile);
            new Handler(Looper.getMainLooper()).post(() -> callback.onRowId(rowId));
        });
    }

    public interface OnChildRowIdCallback {
        void onRowId(long rowId);
    }

    public LiveData<ChildProfile> getChildByLocalId(long childId) {
        return childProfileDao.getChildById(childId);
    }

    public LiveData<java.util.List<ChildProfile>> getChildrenForParent(long parentId) {
        return childProfileDao.getChildrenForParent(parentId);
    }

    public LiveData<java.util.List<ChildProfile>> getChildrenForGuest() {
        return childProfileDao.getChildrenForGuest();
    }

    //READ/get child from Room (offline)
    public LiveData<ChildProfile> getChild(String remoteId) {
        return childProfileDao.getChildByRemoteId(remoteId);
    }

    public LiveData<ChildProfile> getLocalChild() {
        return childProfileDao.getFirstChild();
    }

    /** Returns the first child scoped to the currently logged-in parent. */
    public LiveData<ChildProfile> getChildForParent(long parentId) {
        return childProfileDao.getChildByParentId(parentId);
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
