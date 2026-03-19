package com.example.qtrobot.ui.viewmodel;


import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.qtrobot.data.local.entity.ChildProfile;
import com.example.qtrobot.data.repository.DataRepository;

// code source reference: https://google-developer-training.github.io/android-developer-advanced-course-practicals/unit-6-working-with-architecture-components/lesson-14-room,-livedata,-viewmodel/14-1-a-room-livedata-viewmodel/14-1-a-room-livedata-viewmodel.html#task7intro

public class ChildViewModel extends AndroidViewModel {

    private DataRepository repository;
    private LiveData<ChildProfile> childLiveData;

    //constructor gets reference to the repository and child profile
    public ChildViewModel(Application application) {
        super(application);
        repository = DataRepository.getInstance(application);
        childLiveData = repository.getLocalChild();
    }

    // methods from repository (hides it from UI)
    public LiveData<ChildProfile> getChildFromRoom() {
        return childLiveData;
    }

    public void refreshProfileLocalAndCloud(String remoteChildId) {
        repository.refreshChild(remoteChildId);
    }

    public void saveProfileToLocalAndCloud(ChildProfile childProfile) {
        repository.saveChild(childProfile);
    }
}