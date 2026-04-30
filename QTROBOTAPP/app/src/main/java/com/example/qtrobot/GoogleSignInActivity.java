package com.example.qtrobot;

import android.content.Intent;
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

import java.util.Locale;

public class GoogleSignInActivity extends BaseActivity {

    private static final String TAG = "GoogleSignInActivity";

    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar progressBar;
    private TextView statusText;
    private boolean isSigningIn = false;

    private SessionManager sessionManager;
    private DataRepository dataRepository;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                isSigningIn = false;
                Intent data = result.getData();

                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
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

            sessionManager = new SessionManager(this);
            dataRepository  = new DataRepository(getApplication());

            progressBar = findViewById(R.id.progressBar);
            SignInButton googleSignInButton = findViewById(R.id.googleSignInButton);
            statusText = findViewById(R.id.statusText);

            android.widget.ImageView logoImage = findViewById(R.id.logo);
            if (logoImage != null && ThemePrefs.isPinkTheme(this)) {
                logoImage.setImageResource(R.drawable.qtlogo_pink);
            }

            if (googleSignInButton != null) {
                googleSignInButton.setSize(SignInButton.SIZE_WIDE);
                googleSignInButton.setOnClickListener(v -> signIn());
            }

            String serverClientId = getString(R.string.server_client_id);
            Log.d(TAG, "Using Server Client ID: " + serverClientId);

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(serverClientId)
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            // If we already have an active session, resolve child flow (home / picker / new profile)
            if (sessionManager.isLoggedIn()) {
                navigateAfterSessionReady(false);
                return;
            }

            // Fallback: Google has a cached account — re-hydrate the session
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null) {
                restoreOrCreateSession(account, null);
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
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account == null) return;

            Log.d(TAG, "Sign-in success for: " + account.getEmail());
            if (statusText != null) statusText.setText(R.string.signing_in);

            String idToken = account.getIdToken();
            if (idToken != null) {
                sendTokenToBackend(idToken, account);
            } else {
                Log.w(TAG, "No ID Token — creating local-only session");
                restoreOrCreateSession(account, null);
            }
        } catch (ApiException e) {
            // Handled in the launcher
        }
    }

    private void sendTokenToBackend(String idToken, GoogleSignInAccount account) {
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
                String accessToken = null;
                if (response.isSuccessful() && response.body() != null) {
                    accessToken = response.body().access_token;
                    Log.d(TAG, "Backend token exchange successful");
                } else {
                    Log.w(TAG, "Backend rejected token (code " + response.code() + ") — continuing with Google session");
                }
                restoreOrCreateSession(account, accessToken);
            }

            @Override
            public void onFailure(@NonNull Call<TokenResponse> call, @NonNull Throwable t) {
                Log.w(TAG, "Backend unreachable — continuing with Google session only: " + t.getMessage());
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                restoreOrCreateSession(account, null);
            }
        });
    }

    /**
     * Upserts the Google-authenticated user as a ParentAccount in Room,
     * then persists the local Row ID via SessionManager so every other
     * Activity can call SessionManager.getParentId() to scope their data.
     */
    private void restoreOrCreateSession(GoogleSignInAccount account, String accessToken) {
        if (account == null) {
            navigateAfterSessionReady(false);
            return;
        }

        String displayName = account.getDisplayName();
        String firstName = "";
        String lastName  = "";
        if (displayName != null && !displayName.isEmpty()) {
            int space = displayName.indexOf(' ');
            if (space >= 0) {
                firstName = displayName.substring(0, space);
                lastName  = displayName.substring(space + 1);
            } else {
                firstName = displayName;
            }
        }

        ParentAccount parent = new ParentAccount();
        parent.firstName     = firstName;
        parent.lastName      = lastName;
        parent.email         = account.getEmail() == null ? null : account.getEmail().trim().toLowerCase(Locale.ROOT);
        parent.passwordToken = null;
        parent.createdAt     = System.currentTimeMillis();
        parent.updatedAt     = System.currentTimeMillis();
        parent.isDirty       = false;

        final String finalAccessToken = accessToken;

        dataRepository.upsertGoogleParent(parent, parentLocalId -> {
            String normalizedEmail = account.getEmail() == null ? "" : account.getEmail().trim().toLowerCase(Locale.ROOT);
            sessionManager.saveSession(parentLocalId, displayName, normalizedEmail, finalAccessToken);
            Log.d(TAG, "Session saved — parentLocalId=" + parentLocalId);
            navigateAfterSessionReady(false);
        });
    }

    /** After login, always route to child selection to choose/add a child profile. */
    private void navigateAfterSessionReady(boolean isGuest) {
        if (isFinishing()) return;
        Intent intent = new Intent(GoogleSignInActivity.this, ChildSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
