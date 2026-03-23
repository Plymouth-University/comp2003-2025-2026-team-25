package com.example.qtrobot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.example.qtrobot.data.local.entity.ParentAccount;
import com.example.qtrobot.data.repository.DataRepository;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class GoogleSignInActivity extends BaseActivity {

    private static final String TAG = "GoogleSignInActivity";

    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar progressBar;
    private TextView statusText;
    private boolean isSigningIn = false;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                isSigningIn = false;
                Intent data = result.getData();

                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Extracting the account to ensure the task was successful
                    task.getResult(ApiException.class);
                    handleSignInResult(task);
                } catch (ApiException e) {
                    int statusCode = e.getStatusCode();
                    Log.e(TAG, "Sign-in failed. Google Error Code: " + statusCode);
                    
                    String message;
                    switch (statusCode) {
                        case CommonStatusCodes.DEVELOPER_ERROR:
                            message = "Developer Error (10): Check SHA-1/Package Name in Google Console";
                            break;
                        case 12500:
                            message = "Sign-in failed (12500): Check OAuth Config / Test Users";
                            break;
                        case CommonStatusCodes.NETWORK_ERROR:
                            message = "Network error - check your connection";
                            break;
                        case 12501:
                            message = "Sign-in cancelled";
                            break;
                        default:
                            message = "Sign-in error: " + statusCode;
                    }
                    
                    if (statusCode != 12501 && !isFinishing()) {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_google_signin);

            progressBar = findViewById(R.id.progressBar);
            SignInButton googleSignInButton = findViewById(R.id.googleSignInButton);
            statusText = findViewById(R.id.statusText);

            if (googleSignInButton != null) {
                googleSignInButton.setSize(SignInButton.SIZE_WIDE);
                googleSignInButton.setOnClickListener(v -> signIn());
            }

            // Configure Google Sign-In
            String serverClientId = getString(R.string.server_client_id);
            Log.d(TAG, "Using Server Client ID: " + serverClientId);

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(serverClientId)
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            // Check if user is already signed in
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null) {
                navigateToHome(account);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            if (!isFinishing()) Toast.makeText(this, "Initialization error", Toast.LENGTH_SHORT).show();
        }
    }

    private void signIn() {
        if (isSigningIn) return;
        
        isSigningIn = true;
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                signInLauncher.launch(signInIntent);
            });
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account == null) return;
            
            Log.d(TAG, "Sign-in success for user: " + account.getEmail());
            if (statusText != null) statusText.setText(R.string.signing_in);

            String idToken = account.getIdToken();
            if (idToken != null) {
                Log.d(TAG, "ID Token obtained successfully");
                sendTokenToBackend(idToken);
            } else {
                Log.e(TAG, "Failed to obtain ID Token");
                if (!isFinishing()) Toast.makeText(this, "Error: No ID Token", Toast.LENGTH_SHORT).show();
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        } catch (ApiException e) {
            // Handled in the launcher
        }
    }

    private void sendTokenToBackend(String idToken) {
        // Use 10.0.2.2 for emulator.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/") 
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        GoogleTokenExchangeRequest body = new GoogleTokenExchangeRequest();
        body.google_id_token = idToken;

        Log.d(TAG, "Sending ID Token to backend for exchange...");
        Call<TokenResponse> call = apiService.exchangeToken(body);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<TokenResponse> call, @NonNull Response<TokenResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Backend token exchange successful");
                    SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                    prefs.edit().putString("access_token", response.body().access_token).apply();

                    navigateToHome(GoogleSignIn.getLastSignedInAccount(GoogleSignInActivity.this));
                } else {
                    Log.e(TAG, "Backend rejected token. Code: " + response.code());
                    if (statusText != null && !isFinishing()) {
                        statusText.setText(R.string.auth_failed);
                        Toast.makeText(GoogleSignInActivity.this, "Server rejected token (Code " + response.code() + ")", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TokenResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Network failure when reaching backend", t);
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (statusText != null && !isFinishing()) {
                    statusText.setText(R.string.network_error);
                    Toast.makeText(GoogleSignInActivity.this, "Cannot reach server", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

//    private void navigateToHome(GoogleSignInAccount account) {
//        if (account != null && !isFinishing()) {
//            Intent intent = new Intent(this, HomeActivity.class);
//            intent.putExtra("user_name", account.getDisplayName());
//            intent.putExtra("user_email", account.getEmail());
//            intent.putExtra("auth_type", "google");
//            startActivity(intent);
//            finish();
//        }
//    }

    private void navigateToHome(GoogleSignInAccount account) {
        if (account == null || isFinishing()) return;

        // saves google user to Room DB as a ParentAccount
        ParentAccount parent = new ParentAccount();
        parent.firstName = account.getGivenName();   // first name from Google
        parent.lastName = account.getFamilyName();  // last name from Google
        parent.email = account.getEmail();
        parent.createdAt = System.currentTimeMillis();
        parent.updatedAt = System.currentTimeMillis();
        parent.isDirty = true;

        // Set as logged-in user
        getSharedPreferences("user_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("is_guest", false)
                .apply();

        // Save to Room DB then navigate to HomeActivity
        Toast.makeText(this, "Welcome back, " + parent.firstName + "!", Toast.LENGTH_SHORT).show();
        DataRepository.getInstance(getApplication())
                .insertParent(parent, newParentId -> {
                    Intent intent = new Intent(this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                });
    }


    public interface ApiService {
        @POST("/auth/exchange-google-token")
        Call<TokenResponse> exchangeToken(@Body GoogleTokenExchangeRequest request);
    }

    public static class TokenResponse {
        public String access_token;
    }

    public static class GoogleTokenExchangeRequest {
        public String google_id_token;
    }
}
