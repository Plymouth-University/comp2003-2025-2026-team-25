package com.example.qtrobot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Random;

public class BrushTimerActivity extends AppCompatActivity {

    private static final long START_TIME_IN_MILLIS = 120000; // 2 minutes

    private TextView timerText;
    private TextView titleText;
    private Button startTimerButton;
    private FrameLayout bubbleContainer;
    private Handler bubbleHandler = new Handler();

    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long timeLeftInMillis = START_TIME_IN_MILLIS;

    private MediaPlayer mediaPlayer;
    private boolean cue90Played = false;
    private boolean cue60Played = false;
    private boolean cue30Played = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brushtimer);

        ImageButton goBackButton = findViewById(R.id.go_back_button);
        goBackButton.setOnClickListener(v -> finish());

        timerText = findViewById(R.id.timer_text);
        titleText = findViewById(R.id.brush_teeth_title);
        startTimerButton = findViewById(R.id.start_timer_button);
        bubbleContainer = findViewById(R.id.bubble_container);

        startTimerButton.setText("Start Brushing");
        startTimerButton.setOnClickListener(v -> startTimer());
        updateCountDownText();
        playSound(R.raw.press_the_button_when_ready);
        bubbleContainer.post(this::startBubbleAnimation);
    }
    private void startTimer() {
        if (timerRunning) return;

        playSound(R.raw.brush_teeth_together);
        startTimerButton.setVisibility(View.GONE);

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
                checkAudioCues(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                titleText.setText("You did it! Well done!");
                timerText.setText("00:00");
            }
        }.start();

        timerRunning = true;
    }

    private void checkAudioCues(long millisUntilFinished) {
        long secondsLeft = Math.round(millisUntilFinished / 1000.0);

        if (secondsLeft == 90 && !cue90Played) {
            titleText.setText("Well done, keep it up!");
            playSound(R.raw.well_done_keep_it_up);
            cue90Played = true;
        } else if (secondsLeft == 60 && !cue60Played) {
            titleText.setText("You are halfway there!");
            playSound(R.raw.you_are_halfway_there);
            cue60Played = true;
        } else if (secondsLeft == 30 && !cue30Played) {
            titleText.setText("Nearly done!");
            playSound(R.raw.nearly_done);
            cue30Played = true;
        }
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerText.setText(timeLeftFormatted);
    }

    private void playSound(int resId) {
        try {
            stopSound();
            mediaPlayer = MediaPlayer.create(this, resId);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    if (mediaPlayer == mp) mediaPlayer = null;
                });
            }
        } catch (Exception e) {
            Log.e("BrushTimer", "Error playing sound", e);
        }
    }

    private void stopSound() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            Log.e("BrushTimer", "Error stopping sound", e);
        }
    }

    private void startBubbleAnimation() {
        bubbleHandler.post(new Runnable() {
            @Override
            public void run() {
                createBubble();
                bubbleHandler.postDelayed(this, 300);
            }
        });
    }

    private void createBubble() {
        int containerWidth = bubbleContainer.getWidth();
        int containerHeight = bubbleContainer.getHeight();
        if (containerWidth <= 0 || containerHeight <= 0) return;

        ImageView bubble = new ImageView(this);
        bubble.setImageResource(R.drawable.bubble);
        Random random = new Random();
        int size = random.nextInt(100) + 50;
        int maxLeft = containerWidth - size;
        if (maxLeft <= 0) maxLeft = 1;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        params.leftMargin = random.nextInt(maxLeft);
        bubble.setLayoutParams(params);
        bubbleContainer.addView(bubble);

        ObjectAnimator animator = ObjectAnimator.ofFloat(bubble, "translationY", containerHeight, -size);
        animator.setDuration(random.nextInt(3000) + 2000);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                bubbleContainer.removeView(bubble);
            }
        });
        animator.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSound();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bubbleHandler.removeCallbacksAndMessages(null);
        stopSound();
    }
}
