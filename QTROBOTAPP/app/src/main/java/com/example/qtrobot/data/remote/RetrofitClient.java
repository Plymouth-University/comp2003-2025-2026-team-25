package com.example.qtrobot.data.remote;

import android.content.Context;

import com.example.qtrobot.AppConfig;
import com.example.qtrobot.SessionManager;
import com.example.qtrobot.data.remote.api.BackendApi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static volatile RetrofitClient instance;
    private static volatile RetrofitClient qrInstance;

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) instance = new RetrofitClient(null, AppConfig.getBaseUrl());
        return instance;
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) instance = new RetrofitClient(context.getApplicationContext(), AppConfig.getBaseUrl());
        return instance;
    }

    public static BackendApi getMainApi(Context context) {
        return getInstance(context).getBackendApi();
    }

    public static synchronized RetrofitClient getQrInstance() {
        if (qrInstance == null) qrInstance = new RetrofitClient(null, AppConfig.getQrApiUrl());
        return qrInstance;
    }

    public static BackendApi getQrApi() {
        return getQrInstance().getBackendApi();
    }

    public static synchronized void reset() {
        instance = null;
        qrInstance = null;
    }

    private final BackendApi backendApi;

    private RetrofitClient(Context appContext, String baseUrl) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().addInterceptor(logging);

        if (appContext != null) {
            clientBuilder.addInterceptor(chain -> {
                Request original = chain.request();
                String token = new SessionManager(appContext).getAccessToken();
                if (token != null && !token.isEmpty()) {
                    return chain.proceed(original.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build());
                }
                return chain.proceed(original);
            });
        }

        backendApi = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BackendApi.class);
    }

    public BackendApi getBackendApi() { return backendApi; }
}
