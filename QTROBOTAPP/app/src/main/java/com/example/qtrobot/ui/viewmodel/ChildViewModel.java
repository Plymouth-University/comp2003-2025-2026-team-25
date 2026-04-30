package com.example.qtrobot.ui.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.qtrobot.data.local.entity.ChildProfile;
import com.example.qtrobot.data.repository.DataRepository;

public class ChildViewModel extends AndroidViewModel {

    private final DataRepository repository;
    private final MutableLiveData<Long> parentIdLive = new MutableLiveData<>();
    private final LiveData<ChildProfile> childForCurrentParent;

    public ChildViewModel(Application application) {
        super(application);
        repository = new DataRepository(application);
        childForCurrentParent = Transformations.switchMap(parentIdLive, parentId -> {
            if (parentId == null || parentId < 0) {
                return new MutableLiveData<>(null);
            }
            return repository.getChildForParent(parentId);
        });
    }

    public void setParentId(long parentId) {
        parentIdLive.setValue(parentId);
    }

    public LiveData<ChildProfile> getChildForCurrentParent() {
        return childForCurrentParent;
    }

    public LiveData<ChildProfile> getChildFromRoom() {
        return repository.getLocalChild();
    }

    public LiveData<ChildProfile> getChildByLocalId(long localId) {
        if (localId < 0) {
            return new MutableLiveData<>(null);
        }
        return repository.getChildByLocalId(localId);
    }

    public void refresh(String remoteChildId) {
        repository.refreshChild(remoteChildId);
    }

    public void saveChild(ChildProfile childProfile) {
        repository.saveChild(childProfile);
    }
}