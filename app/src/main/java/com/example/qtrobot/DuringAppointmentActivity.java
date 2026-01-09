package com.example.qtrobot;

import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.widget.ImageButton;
import java.util.ArrayList;
import java.util.List;

public class DuringAppointmentActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_during_appointment);

        ImageButton goBackButton = findViewById(R.id.go_back_button);
        goBackButton.setOnClickListener(v -> finish());

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        
        List<CardModel> cards = new ArrayList<>();
        cards.add(new CardModel("The dentist", "Will explain what they are doing before they begin", R.drawable.qt_dentist_explain));
        cards.add(new CardModel("During your appointment", "You'll sit comfortably in the dental chair", R.drawable.qt_on_dentist_table));
        cards.add(new CardModel("Checking Teeth", "The dentist will count your teeth and make sure they're strong.", R.drawable.qt_getting_teeth_count));

        CardAdapter adapter = new CardAdapter(cards);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        playSound(R.raw.dentist_will_explain_before_begin);
                        break;
                    case 1:
                        playSound(R.raw.during_visit_you_sit_comfortably_in_dentists_chair);
                        break;
                    case 2:
                        playSound(R.raw.comfort_are_priority);
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
