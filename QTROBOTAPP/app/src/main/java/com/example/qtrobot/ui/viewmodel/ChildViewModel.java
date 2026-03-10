package com.example.qtrobot.ui.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.qtrobot.data.local.entity.ChildProfile;
import com.example.qtrobot.data.repository.DataRepository;

public class ChildViewModel extends AndroidViewModel {

    private final DataRepository repository;
    private final LiveData<ChildProfile> child;

    public ChildViewModel(Application application) {
        super(application);
        repository = new DataRepository(application);
        child = repository.getLocalChild();
    }

    public LiveData<ChildProfile> getChildFromRoom() {
        return child;
    }

    public void refresh(String remoteChildId) {
        repository.refreshChild(remoteChildId);
    }

    public void saveChild(ChildProfile childProfile) {
        repository.saveChild(childProfile);
    }
}