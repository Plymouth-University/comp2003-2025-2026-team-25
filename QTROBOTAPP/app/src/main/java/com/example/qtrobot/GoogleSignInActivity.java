package com.example.qtrobot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;

public class GoogleSignInActivity extends AppCompatActivity {

    private static final String TAG = "GoogleSignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar progressBar;
    private Button googleSignInButton;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_signin);

        progressBar = findViewById(R.id.progressBar);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        statusText = findViewById(R.id.statusText);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(v -> signIn());

        // Check if user is already signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            navigateToHome(account);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "signInResult: Success");
            progressBar.setVisibility(android.view.View.VISIBLE);
            statusText.setText("Signing in...");

            // Get the ID token and send it to your backend
            String idToken = account.getIdToken();
            if (idToken != null) {
                sendTokenToBackend(idToken);
            } else {
                Toast.makeText(this, "Failed to get ID token", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(android.view.View.GONE);
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult: failed code=" + e.getStatusCode(), e);
            Toast.makeText(this, "Sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(android.view.View.GONE);
        }
    }

    private void sendTokenToBackend(String idToken) {
        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/") // 10.0.2.2 is localhost for Android emulator
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // Call your backend endpoint (you'll need to add this)
        Call<TokenResponse> call = apiService.exchangeToken("Bearer " + idToken);
        call.enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                progressBar.setVisibility(android.view.View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Token exchanged successfully");
                    // Save the access token
                    SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                    prefs.edit().putString("access_token", response.body().access_token).apply();

                    // Get the signed-in account
                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(GoogleSignInActivity.this);
                    navigateToHome(account);
                } else {
                    statusText.setText("Authentication failed");
                    Log.e(TAG, "Token exchange failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                progressBar.setVisibility(android.view.View.GONE);
                statusText.setText("Network error");
                Log.e(TAG, "Network error", t);
                Toast.makeText(GoogleSignInActivity.this, "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHome(GoogleSignInAccount account) {
        if (account != null) {
            Intent intent = new Intent(GoogleSignInActivity.this, HomeActivity.class);
            intent.putExtra("user_name", account.getDisplayName());
            intent.putExtra("user_email", account.getEmail());
            startActivity(intent);
            finish();
        }
    }

    // API Service interfaces
    public interface ApiService {
        @GET("/auth/google")
        Call<TokenResponse> exchangeToken(@Header("Authorization") String token);
    }

    public static class TokenResponse {
        public String access_token;
        public String token_type;
        public long expires_in;
    }
}
