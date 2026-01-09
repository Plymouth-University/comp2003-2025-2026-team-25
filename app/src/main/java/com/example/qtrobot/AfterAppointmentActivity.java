package com.example.qtrobot;

import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.widget.ImageButton;
import java.util.ArrayList;
import java.util.List;

public class AfterAppointmentActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_appointment);

        ImageButton goBackButton = findViewById(R.id.go_back_button);
        goBackButton.setOnClickListener(v -> finish());

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        
        List<CardModel> cards = new ArrayList<>();
        cards.add(new CardModel("The dentist", "Will talk to you about next steps", R.drawable.qt_next_steps));
        cards.add(new CardModel("You are welcome", "To ask questions at any time", R.drawable.farewell_qt));
        cards.add(new CardModel("Thank you!", "For taking the time to learn with me today", R.drawable.qt_thanks));

        CardAdapter adapter = new CardAdapter(cards);
        viewPager.setAdapter(adapter);


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        playSound(R.raw.after_your_appointment_the_dentists_will_talk_about_next_steps);
                        break;
                    case 1:
                        playSound(R.raw.qt_ask_questions);
                        break;
                    case 2:
                        playSound(R.raw.thankyou_for_learning);
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
