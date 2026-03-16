package com.example.qtrobot;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.qtrobot.data.remote.RetrofitClient;
import com.example.qtrobot.data.remote.dto.QrHashResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrScanPage extends BaseActivity {

    private static final String TAG = "QrScanPage";

    private ImageView qrCodeImageView;
    private TextView timerTextView;
    private TextView expiryMessageTextView;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = AppConfig.QR_EXPIRY_MILLIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan_page);

        ImageView robotImage = findViewById(R.id.qtrobot_image);
        RobotImageHelper.applyRobot(robotImage, this);

        qrCodeImageView       = findViewById(R.id.large_qr_code);
        timerTextView         = findViewById(R.id.countdown_timer);
        expiryMessageTextView = findViewById(R.id.expiry_message);

        ImageButton backButton = findViewById(R.id.back_button);
        if (backButton != null) backButton.setOnClickListener(v -> finish());

        SessionManager session = new SessionManager(this);
        String userId = session.getParentEmail();

        if (userId == null || userId.isEmpty()) {
            showMessage(getString(R.string.qr_not_available));
            return;
        }

        fetchQrFromApi(userId);
    }

    private void fetchQrFromApi(String userId) {
        showMessage("Generating your QR code...");

        RetrofitClient.getQrApi().generateQrHash(userId).enqueue(new Callback<QrHashResponse>() {
            @Override
            public void onResponse(@NonNull Call<QrHashResponse> call, @NonNull Response<QrHashResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String rawString = response.body().rawString;
                    if (rawString != null && !rawString.isEmpty()) {
                        showQr(rawString);
                    } else {
                        showMessage(getString(R.string.qr_not_available));
                    }
                } else {
                    showMessage(getString(R.string.qr_not_available));
                }
            }

            @Override
            public void onFailure(@NonNull Call<QrHashResponse> call, @NonNull Throwable t) {
                Log.w(TAG, "QR API unreachable: " + t.getMessage());
                showMessage(getString(R.string.qr_not_available));
            }
        });
    }

    private void showQr(String qrContent) {
        Bitmap qrBitmap = generateQrCode(qrContent);
        if (qrBitmap != null) {
            qrCodeImageView.setImageBitmap(qrBitmap);
            qrCodeImageView.setAlpha(1.0f);
        }
        if (expiryMessageTextView != null) expiryMessageTextView.setVisibility(View.GONE);
        if (timerTextView != null)         timerTextView.setVisibility(View.VISIBLE);
        startTimer();
    }

    private void showMessage(String message) {
        qrCodeImageView.setAlpha(0.15f);
        if (expiryMessageTextView != null) {
            expiryMessageTextView.setText(message);
            expiryMessageTextView.setVisibility(View.VISIBLE);
        }
        if (timerTextView != null) timerTextView.setVisibility(View.GONE);
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override public void onTick(long ms) { timeLeftInMillis = ms; updateCountDownText(); }
            @Override public void onFinish() { timeLeftInMillis = 0; updateCountDownText(); handleTimerFinished(); }
        }.start();
    }

    private void updateCountDownText() {
        if (timerTextView == null) return;
        int m = (int)(timeLeftInMillis / 1000) / 60;
        int s = (int)(timeLeftInMillis / 1000) % 60;
        timerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", m, s));
    }

    private void handleTimerFinished() {
        if (timerTextView != null) timerTextView.setVisibility(View.INVISIBLE);
        if (expiryMessageTextView != null) {
            expiryMessageTextView.setText(R.string.qr_expired);
            expiryMessageTextView.setVisibility(View.VISIBLE);
        }
        qrCodeImageView.setAlpha(0.2f);
    }

    private Bitmap generateQrCode(String text) {
        int size = 800;
        try {
            BitMatrix bm = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size);
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++)
                for (int y = 0; y < size; y++)
                    bitmap.setPixel(x, y, bm.get(x, y) ? Color.BLACK : Color.WHITE);
            return bitmap;
        } catch (WriterException e) { e.printStackTrace(); return null; }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
