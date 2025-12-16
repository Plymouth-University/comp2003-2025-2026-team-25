package com.example.qtrobot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnOpenTest = findViewById(R.id.btn_open_test);

        // When clicked, navigate to the Test Screen
        btnOpenTest.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.qtrobot.RoomDBTest.class);
            startActivity(intent);
        });
    }
}