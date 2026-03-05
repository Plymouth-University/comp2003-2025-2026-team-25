package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.qtrobot.data.local.dao.ChildProfileDao;
import com.example.qtrobot.data.local.dao.ParentAccountDao;
import com.example.qtrobot.data.local.database.AppDatabase;


public class MainActivity extends BaseActivity {

    //-- Room DB declarations block starts here --
    private AppDatabase db;
    private ParentAccountDao parentAccountDao;
    private ChildProfileDao childProfileDao;

    private EditText passwordInput;
    private ImageButton passwordToggle;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // get singleton DB instance
        db = AppDatabase.getInstance(getApplicationContext());

        // get DAO instances
        parentAccountDao = db.parentAccountDao();
        childProfileDao = db.childProfileDao();

        //use DB on background thread (for test)
        AppDatabase.databaseWriteExecutor.execute(() -> {
            ParentAccountDao dao = AppDatabase.getInstance(getApplicationContext()).parentAccountDao();
            // this will just touch the DB so it’s created
            dao.getSingleParent();
        });

        // -- Room DB integration ends here --

        ImageView robotImage = findViewById(R.id.qtrobot_image);
        RobotImageHelper.applyRobot(robotImage, this);

        passwordInput = findViewById(R.id.password_input);
        passwordToggle = findViewById(R.id.password_toggle);

        passwordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        Button registerButton = findViewById(R.id.already_have_account_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });
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
