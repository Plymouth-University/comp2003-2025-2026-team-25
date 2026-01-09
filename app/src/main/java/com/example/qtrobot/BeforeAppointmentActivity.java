package com.example.qtrobot;

import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.widget.ImageButton;
import java.util.ArrayList;
import java.util.List;

public class BeforeAppointmentActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_before_appointment);

        ImageButton goBackButton = findViewById(R.id.go_back_button);
        goBackButton.setOnClickListener(v -> finish());

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        
        List<CardModel> cards = new ArrayList<>();
        cards.add(new CardModel("Before your appointment", "The dental team prepare everything for you", R.drawable.qt_robot_observing_staff));
        cards.add(new CardModel("Waiting Room", "This time helps make sure your visit goes smoothly", R.drawable.qt_in_dentist_room));

        CardAdapter adapter = new CardAdapter(cards);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        playSound(R.raw.dental_team_will_prepare_everything);
                        break;
                    case 1:
                        playSound(R.raw.this_time_helps_make_sure_your_visits_goes_smoothly);
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
