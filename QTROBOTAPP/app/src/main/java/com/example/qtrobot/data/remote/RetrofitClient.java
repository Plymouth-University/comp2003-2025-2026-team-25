/*
How to use it:

BackendApi api = RetrofitClient.getInstance().getBackendApi();
api.getChild("test-child-1").enqueue(...);
*/

package com.example.qtrobot.data.remote;

import com.example.qtrobot.data.remote.api.BackendApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class RetrofitClient {

    // PART 1: the addresses of our AWS API :
    private static final String BASE_URL = "https://mvtp4g6tsa.execute-api.eu-west-2.amazonaws.com";

    // PART 2: build Retrofit once
    private static RetrofitClient instance;
    public static synchronized RetrofitClient getInstance(){
        if (instance == null) {
            // build it
            instance = new RetrofitClient();
        }
        return instance; //returns the same instance every time
    }

    // PART 3: represents what Retrofit "machine" actually is:

    private BackendApi backendApi; //uses the menu of endpoints/urls

    private RetrofitClient() {

        // adding logging to display HTTP calls in logcat
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // the actual HTTP "engine"
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        // Glue all together:  Base URL + HTTP engine + JSON converter → ready to call AWS
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

            //stick BackendApi interface (urls menu) to that
            backendApi = retrofit.create(BackendApi.class);
    }
    // getter
    public BackendApi getBackendApi() {
        return backendApi;
    }



}
