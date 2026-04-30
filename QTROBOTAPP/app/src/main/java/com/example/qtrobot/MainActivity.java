package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;


import com.example.qtrobot.data.local.entity.ParentAccount;
import com.example.qtrobot.data.repository.OnLoginCallback;
import com.example.qtrobot.ui.viewmodel.ParentViewModel;

/**
 * Legacy email/password login screen.
 *
 * NOTE: The primary login path is now GoogleSignInActivity, reached via SplashActivity.
 * This screen is kept for future email/password support, but:
 *  - The login button now requires a valid session to proceed (no bypass).
 *  - The Google Sign-In button routes directly to GoogleSignInActivity.
 */
public class MainActivity extends BaseActivity {

    private EditText passwordInput;
    private ImageButton passwordToggle;
    private Button loginButton;
    private Button registerButton;
    private EditText emailInput;
    private boolean isPasswordVisible = false;
    private ParentViewModel parentViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If a session already exists, skip this screen entirely
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        //-- ViewModel initialization and local user data (Room DB)--
        parentViewModel = new ViewModelProvider(this).get(ParentViewModel.class);

        // Dev note: changes in code -- AppRoomDatabase now initialises in the DataRepository constructor

        ImageView robotImage = findViewById(R.id.qtrobot_image);
        RobotImageHelper.applyRobot(robotImage, this);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        passwordToggle = findViewById(R.id.password_toggle);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.already_have_account_button);

        loginButton.setOnClickListener(v -> loginUser());

        if (passwordToggle != null) {
            passwordToggle.setOnClickListener(v -> togglePasswordVisibility());
        }

        if (registerButton != null) {
            registerButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(intent);
            });
        }

        // Google Sign-In button — primary auth path
        Button googleSignInButton = findViewById(R.id.googleSignInButton);
        if (googleSignInButton != null) {
            googleSignInButton.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, GoogleSignInActivity.class)));
        }
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordInput.setInputType(129); // textPassword
            passwordToggle.setImageResource(R.drawable.passwordvistoggle);
        } else {
            passwordInput.setInputType(145); // textVisiblePassword
            passwordToggle.setImageResource(R.drawable.passwordvistoggle);
        }
        isPasswordVisible = !isPasswordVisible;
        passwordInput.setSelection(passwordInput.getText().length());
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
        String password = passwordInput.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        parentViewModel.loginWithEmail(email, password, new OnLoginCallback() {
            @Override
            public void onSuccess(ParentAccount parent) {
                getSharedPreferences("user_prefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("is_guest", false)
                        .apply();
                SessionManager session = new SessionManager(MainActivity.this);
                String fullName = ((parent.firstName != null ? parent.firstName : "") + " "
                        + (parent.lastName != null ? parent.lastName : "")).trim();
                session.saveSession(parent.id, fullName, parent.email, null);
                Toast.makeText(MainActivity.this, "Welcome back, " + parent.firstName + "!", Toast.LENGTH_SHORT).show();
                Intent next = new Intent(MainActivity.this, ChildSelectionActivity.class);
                next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(next);
                finish();
            }

            @Override
            public void onFailure(String reason) {
                Toast.makeText(MainActivity.this, reason, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
