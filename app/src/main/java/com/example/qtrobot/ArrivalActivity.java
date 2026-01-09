package com.example.qtrobot;

import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.widget.ImageButton;
import java.util.ArrayList;
import java.util.List;

public class ArrivalActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrival);

        ImageButton goBackButton = findViewById(R.id.go_back_button);
        goBackButton.setOnClickListener(v -> finish());

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        
        List<CardModel> cards = new ArrayList<>();
        cards.add(new CardModel("Welcome!", "When you arrive at the clinic you will be welcomed by our team", R.drawable.qt_robot_arriving));
        cards.add(new CardModel("Checking In", "You may wait for a short time before your appointment begins", R.drawable.qt_waiting_room));

        CardAdapter adapter = new CardAdapter(cards);
        viewPager.setAdapter(adapter);


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        playSound(R.raw.when_you_arrive_at_clinic);
                        break;
                    case 1:
                        playSound(R.raw.might_wait_for_short_time);
                        break;
                    default:
                        stopSound();
                        break;
                }
            }
        });
    }

    private void playSound(int resId) {
        stopSound();
        try {
            mediaPlayer = MediaPlayer.create(this, resId);
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopSound() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSound();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSound();
    }
}
