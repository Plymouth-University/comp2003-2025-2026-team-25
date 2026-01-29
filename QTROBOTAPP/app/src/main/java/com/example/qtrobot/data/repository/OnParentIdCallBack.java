package com.example.qtrobot.data.repository;

// created to allow the background thread to send Parent ID as a result back to the main thread
public interface OnParentIdCallBack {
    void onParentIdReceived(long parentId);
}
