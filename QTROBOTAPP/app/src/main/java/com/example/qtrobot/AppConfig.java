package com.example.qtrobot;

public class AppConfig {

    // Set to false and fill in AWS URLs when backend is live
    public static final boolean USE_LOCAL_SERVER = true;

    private static final String LOCAL_BASE_URL = "http://10.0.2.2:8000/";
    private static final String LOCAL_QR_URL   = "http://10.0.2.2:8000/";

    private static final String AWS_BASE_URL = "https://mvtp4g6tsa.execute-api.eu-west-2.amazonaws.com";
    private static final String AWS_QR_URL   = "https://REPLACE_WITH_QR_API_URL/";

    public static String getBaseUrl() {
        return USE_LOCAL_SERVER ? LOCAL_BASE_URL : AWS_BASE_URL;
    }

    public static String getQrApiUrl() {
        return USE_LOCAL_SERVER ? LOCAL_QR_URL : AWS_QR_URL;
    }

    public static final long QR_EXPIRY_MILLIS = 300_000;
    public static final String AUTH_ENDPOINT = "/auth/exchange-google-token";
}
