package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class LearnActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        ImageButton goBackButton = findViewById(R.id.go_back_button);
        goBackButton.setOnClickListener(v -> finish());

        findViewById(R.id.arrival_button).setOnClickListener(v -> {
            startActivity(new Intent(this, ArrivalActivity.class));
        });

        findViewById(R.id.before_appt_button).setOnClickListener(v -> {
            startActivity(new Intent(this, BeforeAppointmentActivity.class));
        });

        findViewById(R.id.during_appt_button).setOnClickListener(v -> {
            startActivity(new Intent(this, DuringAppointmentActivity.class));
        });

        findViewById(R.id.after_appt_button).setOnClickListener(v -> {
            startActivity(new Intent(this, AfterAppointmentActivity.class));
        });
    }
}
