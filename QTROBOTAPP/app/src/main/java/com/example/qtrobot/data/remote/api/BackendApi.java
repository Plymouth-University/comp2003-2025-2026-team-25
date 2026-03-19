package com.example.qtrobot.data.remote.api;

import com.example.qtrobot.data.remote.dto.ChildDto;
import com.example.qtrobot.data.remote.dto.GetChildResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

//BackendApi is just a menu of available calls — Retrofit reads it and handles the actual HTTP work behind the scenes.

public interface BackendApi {
    // what to say to AWS (blueprint all URL endpoint to call):


    @GET("children")
    Call<GetChildResponse> getAllChildren();
    @GET("children/{childId}")
    // this is data we will get back (the wrapper object):
    Call<GetChildResponse> getChild(
            // the ID we are asking for:
            @Path("childId") String childId
    );

    // create a new child in AWS
    @POST("children")
    Call<GetChildResponse> createChild(
            @Body ChildDto body
    );
   // @Body ChildDto body tells Retrofit to serialize the ChildDto object into JSON and send it in the request body.
}
