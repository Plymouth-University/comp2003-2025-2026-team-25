package com.example.qtrobot;

import static android.text.TextUtils.isEmpty;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.qtrobot.data.local.entity.ChildProfile;
import com.example.qtrobot.data.repository.DataRepository;

import java.util.Calendar;
import java.util.Locale;

public class NewProfileActivity extends BaseActivity {

    private EditText childNameInput;
    private EditText childDobInput;
    private DataRepository dataRepository;
    private long parentId;
    public static final String PARENT_ID_KEY = "PARENT_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newprofile);

        // Get Parent ID from Intent or fall back to active session
        Intent intent = getIntent();
        parentId = intent.getLongExtra(PARENT_ID_KEY, -1);
        if (parentId == -1) {
            parentId = new SessionManager(this).getParentId();
        }
        // parentId == -1 is allowed for guest users — only set FK for real accounts

        dataRepository = new DataRepository(getApplication());
        childNameInput = findViewById(R.id.child_name_input);
        childDobInput  = findViewById(R.id.child_dob_input);

        ImageView robotImage = findViewById(R.id.qtrobot_image);
        RobotImageHelper.applyRobot(robotImage, this);

        // Go back button — only show if user already has a profile (not first login)
        ImageButton goBackButton = findViewById(R.id.go_back_button);
        if (goBackButton != null) {
            SessionManager session = new SessionManager(this);
            if (session.hasChildProfile()) {
                goBackButton.setVisibility(View.VISIBLE);
                goBackButton.setOnClickListener(v -> finish());
            } else {
                // First login — can't go back, must complete profile
                goBackButton.setVisibility(View.GONE);
            }
        }

        // Date of birth — open DatePickerDialog when tapped
        childDobInput.setFocusable(false);
        childDobInput.setClickable(true);
        childDobInput.setOnClickListener(v -> showDatePicker());

        android.widget.TextView continueButton = findViewById(R.id.continue_button);
        if (continueButton != null) {
            continueButton.setOnClickListener(v -> registerChildProfile());
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year  = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day   = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format as DD/MM/YYYY for display
                    String dob = String.format(Locale.getDefault(),
                            "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    childDobInput.setText(dob);
                },
                year, month, day);

        // Children — cap the max date at today
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void registerChildProfile() {
        String childName = childNameInput.getText().toString().trim();
        String childDob  = childDobInput.getText().toString().trim();

        if (isEmpty(childName)) {
            Toast.makeText(this, "Please enter the child's name.", Toast.LENGTH_SHORT).show();
            return;
        }

        ChildProfile newChild = new ChildProfile();
        newChild.preferredName = childName;
        newChild.dateOfBirth   = childDob;
        if (this.parentId != -1) {
            newChild.parentId = this.parentId;
        }
        newChild.isDirty       = true;
        newChild.createdAt     = System.currentTimeMillis();
        newChild.updatedAt     = System.currentTimeMillis();

        // Insert child then navigate only after DB write is confirmed
        com.example.qtrobot.data.local.database.AppDatabase.databaseWriteExecutor.execute(() -> {
            com.example.qtrobot.data.local.database.AppDatabase.getInstance(this)
                    .childProfileDao().insertChild(newChild);

            // Mark that this parent now has a child profile
            new SessionManager(this).setHasChildProfile(true);

            runOnUiThread(() -> {
                Toast.makeText(this, "Profile created!", Toast.LENGTH_SHORT).show();
                Intent homeIntent = new Intent(NewProfileActivity.this, HomeActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(homeIntent);
            });
        });
    }
}
