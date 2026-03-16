package com.example.qtrobot.data.remote.api;

import com.example.qtrobot.data.remote.dto.ChildDto;
import com.example.qtrobot.data.remote.dto.GetChildResponse;
import com.example.qtrobot.data.remote.dto.QrHashResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface BackendApi {

    @GET("children/{childId}")
    Call<GetChildResponse> getChild(@Path("childId") String childId);

    @POST("children")
    Call<GetChildResponse> createChild(@Body ChildDto body);

    // QR Code — calls /generate-hash/{userId} on the QR API server
    // Returns raw_string (e.g. "parent_123:hash") which gets encoded as the QR code
    @GET("generate-hash/{userId}")
    Call<QrHashResponse> generateQrHash(@Path("userId") String userId);
}
