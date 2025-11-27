package com.example.qtrobot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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

        startTimerButton.setOnClickListener(v -> {
            if (timerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        updateCountDownText();
        startBubbleAnimation();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
                updateTitleText();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                startTimerButton.setText(R.string.start);
                titleText.setText(R.string.brushing_finished);
            }
        }.start();

        timerRunning = true;
        startTimerButton.setText(R.string.pause);
    }

    private void pauseTimer() {
        countDownTimer.cancel();
        timerRunning = false;
        startTimerButton.setText(R.string.start);
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerText.setText(timeLeftFormatted);
    }

    private void updateTitleText() {
        if (timeLeftInMillis <= 30000) {
            titleText.setText(R.string.brushing_nearly_done);
        } else if (timeLeftInMillis <= 60000) {
            titleText.setText(R.string.brushing_halfway);
        } else if (timeLeftInMillis <= 90000) {
            titleText.setText(R.string.brushing_doing_well);
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
        ImageView bubble = new ImageView(this);
        bubble.setImageResource(R.drawable.bubble);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        Random random = new Random();
        int size = random.nextInt(100) + 50;
        params.width = size;
        params.height = size;
        params.leftMargin = random.nextInt(bubbleContainer.getWidth() - size);
        bubble.setLayoutParams(params);
        bubbleContainer.addView(bubble);

        ObjectAnimator animator = ObjectAnimator.ofFloat(bubble, "translationY", bubbleContainer.getHeight(), -size);
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
    protected void onDestroy() {
        super.onDestroy();
        bubbleHandler.removeCallbacksAndMessages(null);
    }
}
