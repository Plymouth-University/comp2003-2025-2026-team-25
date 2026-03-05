package com.example.qt;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RatingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        ImageView emoji1 = findViewById(R.id.emoji_1);
        ImageView emoji5 = findViewById(R.id.emoji_5);

        emoji5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RatingActivity.this, "Glad you had a great visit!", Toast.LENGTH_SHORT).show();
                v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(() -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
                });
            }
        });

    }
}