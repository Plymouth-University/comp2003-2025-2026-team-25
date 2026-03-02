package com.example.qtrobot;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.Locale;
import java.util.UUID;

public class QrScanPage extends AppCompatActivity {

    private static final String PREFS_NAME = "QTrobotPrefs";
    private static final String KEY_USER_UUID = "user_uuid";
    private static final long START_TIME_IN_MILLIS = 300000; // 5 minutes

    private ImageView qrCodeImageView;
    private TextView timerTextView;
    private TextView expiryMessageTextView;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = START_TIME_IN_MILLIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan_page);

        qrCodeImageView = findViewById(R.id.large_qr_code);
        timerTextView = findViewById(R.id.countdown_timer);
        expiryMessageTextView = findViewById(R.id.expiry_message);
        ImageButton backButton = findViewById(R.id.back_button);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Generate and display QR
        String userUuid = getOrCreateUuid();
        Bitmap qrBitmap = generateQrCode(userUuid);
        if (qrBitmap != null) {
            qrCodeImageView.setImageBitmap(qrBitmap);
        }

        startTimer();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateCountDownText();
                handleTimerFinished();
            }
        }.start();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerTextView.setText(timeLeftFormatted);
    }

    private void handleTimerFinished() {
        timerTextView.setVisibility(View.INVISIBLE);
        expiryMessageTextView.setVisibility(View.VISIBLE);
        
        // Visually fade the QR code to indicate it is expired
        qrCodeImageView.setAlpha(0.2f);
    }

    private String getOrCreateUuid() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uuid = prefs.getString(KEY_USER_UUID, null);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            prefs.edit().putString(KEY_USER_UUID, uuid).apply();
        }
        return uuid;
    }

    private Bitmap generateQrCode(String text) {
        int size = 800; // Larger for the dedicated page
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
