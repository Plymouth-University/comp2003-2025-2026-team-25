package com.example;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

public class AppRequester {
    private final OkHttpClient client = new OkHttpClient();

    public String getQrDataFromServer(String userId) throws Exception {

        String url = "http://192.168.1.50:8000/generate-hash/" + userId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Server Error: " + response.code());
            }

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            return json.getString("raw_string");
        }
    }
}