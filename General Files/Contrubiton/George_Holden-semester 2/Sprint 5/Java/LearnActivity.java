package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

public class LearnActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        ImageButton goBackButton = findViewById(R.id.go_back_button);
        if (goBackButton != null) {
            goBackButton.setOnClickListener(v -> finish());
        }

        ImageButton comfortButton = findViewById(R.id.comfort_button);
        if (comfortButton != null) {
            comfortButton.setOnClickListener(v ->
                    startActivity(new Intent(this, ComfortActivity.class)));
        }

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
