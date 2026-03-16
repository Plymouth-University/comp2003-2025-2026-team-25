package com.example.qtrobot.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class QrHashResponse {
    @SerializedName("status")
    public String status;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("auth_hash")
    public String authHash;

    @SerializedName("raw_string")
    public String rawString;
}
