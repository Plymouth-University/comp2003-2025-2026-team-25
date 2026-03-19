package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.qtrobot.data.local.database.AppRoomDatabase;
import com.google.android.gms.common.SignInButton;
import com.example.qtrobot.data.local.dao.ChildProfileDao;
import com.example.qtrobot.data.local.dao.ParentAccountDao;

public class MainActivity extends BaseActivity {

    private EditText passwordInput;
    private ImageButton passwordToggle;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //-- Room DB initialization (local variables since they are only used here) --
        AppRoomDatabase db = AppRoomDatabase.getDatabaseInstance(getApplicationContext());
        ParentAccountDao parentAccountDao = db.parentAccountDao();
        ChildProfileDao childProfileDao = db.childProfileDao();

        // use DB on background thread (for test)
        AppRoomDatabase.databaseWriteExecutor.execute(() -> {
            // this will just touch the DB so it’s created
            parentAccountDao.getSingleParent();
        });
        // -- Room DB integration ends here --

        ImageView robotImage = findViewById(R.id.qtrobot_image);
        RobotImageHelper.applyRobot(robotImage, this);

        passwordInput = findViewById(R.id.password_input);
        passwordToggle = findViewById(R.id.password_toggle);

        if (passwordToggle != null) {
            passwordToggle.setOnClickListener(v -> togglePasswordVisibility());
        }

        Button loginButton = findViewById(R.id.login_button);
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            });
        }

        Button registerButton = findViewById(R.id.already_have_account_button);
        if (registerButton != null) {
            registerButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(intent);
            });
        }

        // Add Google Sign-In Button redirection
        SignInButton googleSignInButton = findViewById(R.id.googleSignInButton);
        if (googleSignInButton != null) {
            googleSignInButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GoogleSignInActivity.class);
                startActivity(intent);
            });
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
}
