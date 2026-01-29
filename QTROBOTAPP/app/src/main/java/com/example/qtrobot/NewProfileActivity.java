package com.example.qtrobot;

import static android.text.TextUtils.isEmpty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qtrobot.data.local.entity.ChildProfile;
import com.example.qtrobot.data.repository.DataRepository;

public class NewProfileActivity extends AppCompatActivity {

    // -- View components
    private EditText childNameInput;
    private EditText childDobInput;
    private Button continueButton;

    // DataRepository and Parent ID
    private DataRepository dataRepository;
    private long parentId;
    public static final String PARENT_ID_KEY = "PARENT_ID";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newprofile);

        // -- Get Parent ID from previous --
        Intent intent = getIntent();
        parentId = intent.getLongExtra(PARENT_ID_KEY, -1);

        // check if Parent ID is valid
        if (parentId == -1) {
            Toast.makeText(this, "Error: No parent account found.", Toast.LENGTH_SHORT).show();
            finish(); //close the activity
            return;
        }

        // Initialize repository and views
        dataRepository = new DataRepository(getApplication());
        childNameInput = findViewById(R.id.child_name_input);
        childDobInput = findViewById(R.id.child_dob_input);
        continueButton = findViewById(R.id.continue_button);


        ImageButton goBackButton = findViewById(R.id.go_back_button);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(NewProfileActivity.this, HomeActivity.class);
                //startActivity(intent);
                registerChildProfile();
            }
        });
    }

    private void registerChildProfile() {
        String childName = childNameInput.getText().toString().trim();
        String childDob = childDobInput.getText().toString().trim();

        // Data Validation
        if (isEmpty(childName)) {
            Toast.makeText(this, "Please enter the child's name.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Create the ChildProfile Entity ---
        ChildProfile newChild = new ChildProfile();
        newChild.preferredName = childName;
        newChild.dateOfBirth = childDob;
        newChild.parentId = this.parentId;

        //other metadata
        newChild.isDirty = true;
        newChild.createdAt = System.currentTimeMillis();
        newChild.updatedAt = System.currentTimeMillis();
        // avatarUri can be set later

        // --- Call Repository to save the child ---
        dataRepository.insertChild(newChild);

        // Goes to home screen
        Toast.makeText(this, "Child's Profile created!", Toast.LENGTH_SHORT).show();
        Intent homeIntent = new Intent(NewProfileActivity.this, HomeActivity.class);
        startActivity(homeIntent);
        finishAffinity(); // to finish this and the previous registration activity
    }
}