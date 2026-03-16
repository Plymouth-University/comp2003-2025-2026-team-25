package com.example.qtrobot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;

public class BrushTimerActivity extends BaseActivity {

    private static final long START_TIME_IN_MILLIS = 120000;
    private static final float MUSIC_FULL_VOLUME   = 1.0f;
    private static final float MUSIC_DUCK_VOLUME   = 0.15f; // quieter during speech
    private static final int   FADE_IN_DURATION_MS = 3000;

    private TextView timerText;
    private TextView titleText;
    private Button startTimerButton;
    private FrameLayout bubbleContainer;
    private final Handler bubbleHandler = new Handler(Looper.getMainLooper());
    private final Handler fadeHandler   = new Handler(Looper.getMainLooper());

    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long timeLeftInMillis = START_TIME_IN_MILLIS;

    // Background music (happy.mp3)
    private MediaPlayer musicPlayer;

    // Speech player (QT robot cues)
    private MediaPlayer speechPlayer;

    private boolean cue90Played = false;
    private boolean cue60Played = false;
    private boolean cue30Played = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brushtimer);

        ImageView robotImage = findViewById(R.id.qtrobot_image);
        RobotImageHelper.applyRobot(robotImage, this);

        ImageButton goBackButton = findViewById(R.id.go_back_button);
        if (goBackButton != null) goBackButton.setOnClickListener(v -> finish());

        timerText        = findViewById(R.id.timer_text);
        titleText        = findViewById(R.id.brush_teeth_title);
        startTimerButton = findViewById(R.id.start_timer_button);
        bubbleContainer  = findViewById(R.id.bubble_container);

        if (startTimerButton != null) startTimerButton.setOnClickListener(v -> startTimer());

        updateCountDownText();
        playSpeech(R.raw.press_the_button_when_ready);
        if (bubbleContainer != null) bubbleContainer.post(this::startBubbleAnimation);
    }

    // -----------------------------------------------------------------------
    // Timer
    // -----------------------------------------------------------------------

    private void startTimer() {
        if (timerRunning) return;

        startBackgroundMusic();

        if (startTimerButton != null) startTimerButton.setVisibility(View.INVISIBLE);

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override public void onTick(long ms) {
                timeLeftInMillis = ms;
                updateCountDownText();
                checkAudioCues(ms);
            }
            @Override public void onFinish() {
                timerRunning = false;
                stopMusic();
                if (titleText != null) titleText.setText("You did it! Well done!");
                if (timerText  != null) timerText.setText("00:00");
            }
        }.start();

        timerRunning = true;
    }

    private void checkAudioCues(long ms) {
        long s = Math.round(ms / 1000.0);
        if (s == 90 && !cue90Played) {
            if (titleText != null) titleText.setText("Well done, keep it up!");
            playSpeech(R.raw.well_done_keep_it_up);
            cue90Played = true;
        } else if (s == 60 && !cue60Played) {
            if (titleText != null) titleText.setText("You are halfway there!");
            playSpeech(R.raw.you_are_halfway_there);
            cue60Played = true;
        } else if (s == 30 && !cue30Played) {
            if (titleText != null) titleText.setText("Nearly done!");
            playSpeech(R.raw.nearly_done);
            cue30Played = true;
        }
    }

    private void updateCountDownText() {
        if (timerText == null) return;
        int m = (int)(timeLeftInMillis / 1000) / 60;
        int s = (int)(timeLeftInMillis / 1000) % 60;
        timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", m, s));
    }

    // -----------------------------------------------------------------------
    // Background music — happy.mp3, fades in from 0 to full volume
    // -----------------------------------------------------------------------

    private void startBackgroundMusic() {
        try {
            musicPlayer = MediaPlayer.create(this, R.raw.happy);
            if (musicPlayer == null) return;
            musicPlayer.setLooping(true);
            musicPlayer.setVolume(0f, 0f);
            musicPlayer.start();
            fadeInMusic();
        } catch (Exception e) {
            Log.e("BrushTimer", "Error starting music", e);
        }
    }

    private void fadeInMusic() {
        final int STEPS = 30;
        final long interval = FADE_IN_DURATION_MS / STEPS;
        final float[] vol = {0f};
        fadeHandler.post(new Runnable() {
            @Override public void run() {
                if (musicPlayer == null) return;
                vol[0] = Math.min(vol[0] + (MUSIC_FULL_VOLUME / STEPS), MUSIC_FULL_VOLUME);
                try { musicPlayer.setVolume(vol[0], vol[0]); } catch (Exception ignored) {}
                if (vol[0] < MUSIC_FULL_VOLUME) fadeHandler.postDelayed(this, interval);
            }
        });
    }

    private void setMusicVolume(float volume) {
        try {
            if (musicPlayer != null && musicPlayer.isPlaying()) {
                musicPlayer.setVolume(volume, volume);
            }
        } catch (Exception ignored) {}
    }

    private void stopMusic() {
        fadeHandler.removeCallbacksAndMessages(null);
        try {
            if (musicPlayer != null) {
                if (musicPlayer.isPlaying()) musicPlayer.stop();
                musicPlayer.release();
                musicPlayer = null;
            }
        } catch (Exception e) {
            Log.e("BrushTimer", "Error stopping music", e);
        }
    }

    // -----------------------------------------------------------------------
    // Speech — ducks music while playing, restores after
    // -----------------------------------------------------------------------

    private void playSpeech(int resId) {
        try {
            stopSpeech();
            setMusicVolume(MUSIC_DUCK_VOLUME);

            speechPlayer = MediaPlayer.create(this, resId);
            if (speechPlayer == null) {
                setMusicVolume(MUSIC_FULL_VOLUME);
                return;
            }
            speechPlayer.start();
            speechPlayer.setOnCompletionListener(mp -> {
                mp.release();
                if (speechPlayer == mp) speechPlayer = null;
                setMusicVolume(MUSIC_FULL_VOLUME); // restore music
            });
        } catch (Exception e) {
            Log.e("BrushTimer", "Error playing speech", e);
        }
    }

    private void stopSpeech() {
        try {
            if (speechPlayer != null) {
                if (speechPlayer.isPlaying()) speechPlayer.stop();
                speechPlayer.release();
                speechPlayer = null;
            }
        } catch (Exception e) {
            Log.e("BrushTimer", "Error stopping speech", e);
        }
    }

    // -----------------------------------------------------------------------
    // Bubbles
    // -----------------------------------------------------------------------

    private void startBubbleAnimation() {
        bubbleHandler.post(new Runnable() {
            @Override public void run() {
                createBubble();
                bubbleHandler.postDelayed(this, 300);
            }
        });
    }

    private void createBubble() {
        if (bubbleContainer == null) return;
        int w = bubbleContainer.getWidth();
        int h = bubbleContainer.getHeight();
        if (w <= 0 || h <= 0) return;

        ImageView bubble = new ImageView(this);
        bubble.setImageResource(R.drawable.bubble);
        if (ThemePrefs.isPinkTheme(this)) {
            bubble.setColorFilter(0xFFFAD1DC, PorterDuff.Mode.SRC_IN);
        } else {
            bubble.setColorFilter(0xFF2196F3, PorterDuff.Mode.SRC_IN);
        }

        Random rnd = new Random();
        int size   = rnd.nextInt(100) + 50;
        int maxL   = Math.max(w - size, 1);

        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(size, size);
        p.leftMargin = rnd.nextInt(maxL);
        bubble.setLayoutParams(p);
        bubbleContainer.addView(bubble);

        ObjectAnimator anim = ObjectAnimator.ofFloat(bubble, "translationY", h, -size);
        anim.setDuration(rnd.nextInt(3000) + 2000);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator a) { bubbleContainer.removeView(bubble); }
        });
        anim.start();
    }

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    @Override
    protected void onPause() {
        super.onPause();
        stopSpeech();
        stopMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bubbleHandler.removeCallbacksAndMessages(null);
        fadeHandler.removeCallbacksAndMessages(null);
        stopSpeech();
        stopMusic();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
