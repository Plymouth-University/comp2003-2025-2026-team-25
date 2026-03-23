package com.example.qtrobot.ui.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import com.example.qtrobot.data.local.entity.ParentAccount;
import com.example.qtrobot.data.repository.DataRepository;
import com.example.qtrobot.data.repository.OnLoginCallback;
import com.example.qtrobot.data.repository.OnParentIdCallBack;

// note for devs: do not use AndroidViewModel, but ViewModel
public class ParentViewModel extends AndroidViewModel {

    private final DataRepository dataRepository;

    public ParentViewModel(Application application) {
        super(application);
        dataRepository = DataRepository.getInstance(application);
    }

    // Registration - is called in RegistrationActivity
    public void registerParent(ParentAccount parent, OnParentIdCallBack callback) {
        dataRepository.insertParent(parent, callback);
    }

    // Email login (not google!) - is called on Login screen
    public void loginWithEmail(String email, String password, OnLoginCallback callback) {
        dataRepository.loginParent(email, password, callback);
    }

    // logout - clears all local data
    public void logout() {
        dataRepository.clearAllLocalData();
    }


}
