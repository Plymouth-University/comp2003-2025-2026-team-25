package com.example.qtrobot;

import static android.text.TextUtils.isEmpty;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.qtrobot.data.local.entity.ParentAccount;
import com.example.qtrobot.data.repository.DataRepository;

public class RegistrationActivity extends AppCompatActivity {

    // Declare Views (UI)
    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText emailInput;
    private EditText dobInput;
    private EditText passwordInput;
    private Button registerButton;

    // Declare Repository variable for data handling
    private DataRepository dataRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // -- DB repository initializing --
        dataRepository = new DataRepository(getApplication());

        // -- UI initializing section --
        firstNameInput = findViewById(R.id.first_name_input);
        lastNameInput = findViewById(R.id.last_name_input);
        emailInput = findViewById(R.id.email_input);
        dobInput = findViewById(R.id.dob_input);
        passwordInput = findViewById(R.id.password_input);
        registerButton = findViewById(R.id.register_button);


        ImageButton goBackButton = findViewById(R.id.go_back_button);
        goBackButton.setOnClickListener(v -> finish());

        Button alreadyHaveAccountButton = findViewById(R.id.already_have_account_button);
        alreadyHaveAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // -- Register Parent account button - event listener
        registerButton.setOnClickListener(v -> {
            registerParent();
        });
    }

    // method to handle the Parent Account registration
    private void registerParent(){
        // Get user input and use trim() to remove whitespace
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String dob = dobInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim(); // implement hash function later

        // Input validation
        if (isEmpty(firstName) || isEmpty(lastName) || isEmpty(email) || isEmpty(dob) || isEmpty(password)) {
            Toast.makeText(this, "Please, fill all require fields", Toast.LENGTH_SHORT).show();
            return; //stop and exit the method
        }

        // Create a new ParentAccount entity
        ParentAccount newParent = new ParentAccount();
        newParent.firstName = firstName;
        newParent.lastName = lastName;
        newParent.email = email;
        newParent.dateOfBirth = dob;
        newParent.passwordToken = password;

        // other metadata settings
        newParent.createdAt = System.currentTimeMillis();
        newParent.updatedAt = System.currentTimeMillis();
        newParent.isDirty = true; //not yet synced to cloud

        // Calls the Repository to insert the new parent account on the background thread
        dataRepository.insertParent(newParent);

        Toast.makeText(this, "Parent Registration Successful!", Toast.LENGTH_SHORT).show();

        // Navigate to the NewProfileActivity to complete child's profile
        Intent intent = new Intent(RegistrationActivity.this, NewProfileActivity.class);
        startActivity(intent);

        // finish the current activity
        finish();


    }
}