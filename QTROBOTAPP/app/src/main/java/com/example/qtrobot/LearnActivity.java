package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.cardview.widget.CardView;

public class LearnActivity extends BaseActivity {
    private CardView cardArrival;
    private CardView cardBefore;
    private CardView cardDuring;
    private CardView cardAfter;;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);
        cardArrival = findViewById(R.id.card_arrival);
        cardBefore = findViewById(R.id.card_before_appointment);
        cardDuring = findViewById(R.id.card_during_appointment);
        cardAfter = findViewById(R.id.card_after_appointment);



        ImageButton goBackButton = findViewById(R.id.go_back_button);
        if (goBackButton != null) {
            goBackButton.setOnClickListener(v -> finish());
        }

        cardArrival.setOnClickListener(v -> {
            startActivity(new Intent(this, ArrivalActivity.class));
        });

        cardBefore.setOnClickListener(v -> {
            startActivity(new Intent(this, BeforeAppointmentActivity.class));
        });

        cardDuring.setOnClickListener(v -> {
            startActivity(new Intent(this, DuringAppointmentActivity.class));
        });

        cardAfter.setOnClickListener(v -> {
            startActivity(new Intent(this, AfterAppointmentActivity.class));
        });
    }
}
