package com.example.qtrobot;

import static android.text.TextUtils.isEmpty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.qtrobot.data.local.database.AppDatabase;
import com.example.qtrobot.data.local.entity.ChildProfile;
import com.example.qtrobot.data.repository.DataRepository;
import com.example.qtrobot.qrcode.ChildQrHelper;

import java.util.regex.Pattern;

public class NewProfileActivity extends BaseActivity {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,24}$");

    public static final String PARENT_ID_KEY = "PARENT_ID";
    /** When true, finish() returns to {@link ChildSelectionActivity} instead of Home. */
    public static final String EXTRA_RETURN_TO_CHILD_SELECTION = "RETURN_TO_CHILD_SELECTION";

    private EditText childNameInput;
    private EditText childUsernameInput;
    private DataRepository dataRepository;
    private long parentId;
    private boolean returnToChildSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newprofile);

        Intent intent = getIntent();
        parentId = intent.getLongExtra(PARENT_ID_KEY, -1);
        if (parentId == -1) {
            parentId = new SessionManager(this).getParentId();
        }
        returnToChildSelection = intent.getBooleanExtra(EXTRA_RETURN_TO_CHILD_SELECTION, false);

        dataRepository = new DataRepository(getApplication());
        childNameInput = findViewById(R.id.child_name_input);
        childUsernameInput = findViewById(R.id.child_username_input);

        ImageView robotImage = findViewById(R.id.qtrobot_image);
        RobotImageHelper.applyRobot(robotImage, this);

        ImageButton goBackButton = findViewById(R.id.go_back_button);
        if (goBackButton != null) {
            SessionManager session = new SessionManager(this);
            if (session.hasChildProfile() || returnToChildSelection) {
                goBackButton.setVisibility(View.VISIBLE);
                goBackButton.setOnClickListener(v -> finish());
            } else {
                goBackButton.setVisibility(View.GONE);
            }
        }

        android.widget.TextView continueButton = findViewById(R.id.continue_button);
        if (continueButton != null) {
            continueButton.setOnClickListener(v -> registerChildProfile());
        }
    }

    private void registerChildProfile() {
        String childName = childNameInput.getText().toString().trim();
        String usernameRaw = childUsernameInput != null ? childUsernameInput.getText().toString().trim() : "";

        if (isEmpty(childName)) {
            Toast.makeText(this, "Please enter the child's name.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isEmpty(usernameRaw)) {
            Toast.makeText(this, getString(R.string.child_username_invalid), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!USERNAME_PATTERN.matcher(usernameRaw).matches()) {
            Toast.makeText(this, getString(R.string.child_username_invalid), Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            com.example.qtrobot.data.local.dao.ChildProfileDao dao =
                    AppDatabase.getInstance(NewProfileActivity.this).childProfileDao();
            int taken;
            if (parentId != -1) {
                taken = dao.countUsernameForParent(parentId, usernameRaw);
            } else {
                taken = dao.countUsernameForGuest(usernameRaw);
            }
            if (taken > 0) {
                runOnUiThread(() -> Toast.makeText(NewProfileActivity.this,
                        R.string.child_username_taken, Toast.LENGTH_SHORT).show());
                return;
            }

            ChildProfile newChild = new ChildProfile();
            newChild.preferredName = childName;
            newChild.childUsername = usernameRaw;
            if (parentId != -1) {
                newChild.parentId = parentId;
            }
            newChild.qr_string = ChildQrHelper.ensureQrToken(newChild);
            newChild.isDirty = true;
            newChild.createdAt = System.currentTimeMillis();
            newChild.updatedAt = System.currentTimeMillis();

            dataRepository.insertChildAndNotify(newChild, rowId -> {
                SessionManager session = new SessionManager(NewProfileActivity.this);
                session.setSelectedChildId(rowId);
                session.setHasChildProfile(true);
                Toast.makeText(NewProfileActivity.this, "Profile created!", Toast.LENGTH_SHORT).show();
                if (returnToChildSelection) {
                    Intent pick = new Intent(NewProfileActivity.this, ChildSelectionActivity.class);
                    pick.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(pick);
                } else {
                    Intent homeIntent = new Intent(NewProfileActivity.this, HomeActivity.class);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homeIntent);
                }
                finish();
            });
        });
    }
}
