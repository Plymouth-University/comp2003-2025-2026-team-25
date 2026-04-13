package com.example.qtrobot.data.repository;

import com.example.qtrobot.data.local.entity.ParentAccount;

public interface OnLoginCallback {
    void onSuccess(ParentAccount parent);
    void onFailure(String reason);
}
