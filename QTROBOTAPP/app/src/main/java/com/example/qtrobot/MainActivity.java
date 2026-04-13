package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

<<<<<<< HEAD
import android.widget.Button;
import com.example.qtrobot.data.local.dao.ChildProfileDao;
import com.example.qtrobot.data.local.dao.ParentAccountDao;
import com.example.qtrobot.data.local.database.AppDatabase;
=======
import androidx.lifecycle.ViewModelProvider;


import com.example.qtrobot.data.local.entity.ParentAccount;
import com.example.qtrobot.data.repository.OnLoginCallback;
import com.example.qtrobot.ui.viewmodel.ParentViewModel;
import com.google.android.gms.common.SignInButton;
>>>>>>> welcome-feature-backup

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

<<<<<<< HEAD
        // Initialise Room (touch DB so it is created on first launch)
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        ParentAccountDao parentAccountDao = db.parentAccountDao();
        ChildProfileDao  childProfileDao  = db.childProfileDao();
        AppDatabase.databaseWriteExecutor.execute(parentAccountDao::getSingleParent);

        ImageView robotImage = findViewById(R.id.qtrobot_image);
        RobotImageHelper.applyRobot(robotImage, this);

        passwordInput  = findViewById(R.id.password_input);
=======
        //-- ViewModel initialization and local user data (Room DB)--
        parentViewModel = new ViewModelProvider(this).get(ParentViewModel.class);

        // Dev note: changes in code -- AppRoomDatabase now initialises in the DataRepository constructor

        ImageView robotImage = findViewById(R.id.qtrobot_image);
        RobotImageHelper.applyRobot(robotImage, this);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
>>>>>>> welcome-feature-backup
        passwordToggle = findViewById(R.id.password_toggle);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.already_have_account_button);

        loginButton.setOnClickListener(v -> loginUser());

        if (passwordToggle != null) {
            passwordToggle.setOnClickListener(v -> togglePasswordVisibility());
        }

<<<<<<< HEAD
        // Email/password login — placeholder until that flow is fully implemented.
        // Does NOT bypass auth; routes to GoogleSignInActivity for now.
        Button loginButton = findViewById(R.id.login_button);
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                // TODO: implement email/password auth against the backend.
                // For now redirect to Google Sign-In so there is always a valid session.
                startActivity(new Intent(MainActivity.this, GoogleSignInActivity.class));
            });
        }

=======
        if (registerButton != null) {
            registerButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(intent);
            });
        }
>>>>>>> welcome-feature-backup

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
        String email = emailInput.getText().toString().trim();
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
                Toast.makeText(MainActivity.this, "Welcome back, " + parent.firstName + "!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                finish();
            }

            @Override
            public void onFailure(String reason) {
                Toast.makeText(MainActivity.this, reason, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
