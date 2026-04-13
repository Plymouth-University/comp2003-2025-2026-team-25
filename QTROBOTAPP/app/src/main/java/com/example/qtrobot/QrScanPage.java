package com.example.qtrobot;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

<<<<<<< HEAD
import com.example.qtrobot.data.local.database.AppDatabase;
import com.example.qtrobot.data.local.entity.ChildProfile;
=======
import androidx.lifecycle.ViewModelProvider;

import com.example.qtrobot.qrcode.QrGenerator;
import com.example.qtrobot.ui.viewmodel.ChildViewModel;
>>>>>>> welcome-feature-backup
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class QrScanPage extends BaseActivity {

    private static final String TAG = "QrScanPage";

    // Shared AES-128 key — must match exactly what is on the robot side
    // Both key and IV must be exactly 16 characters (128 bits)
    private static final String AES_KEY = "QTRobotSecretKey";  // 16 chars
    private static final String AES_IV  = "QTRobotInitVect1";  // 16 chars

    private ImageView qrCodeImageView;
    private TextView timerTextView;
    private TextView expiryMessageTextView;
    private Button generateButton;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = AppConfig.QR_EXPIRY_MILLIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan_page);

<<<<<<< HEAD
=======

        // Apply robot image theme
>>>>>>> welcome-feature-backup
        ImageView robotImage = findViewById(R.id.qtrobot_image);
        RobotImageHelper.applyRobot(robotImage, this);

        qrCodeImageView       = findViewById(R.id.large_qr_code);
        timerTextView         = findViewById(R.id.countdown_timer);
        expiryMessageTextView = findViewById(R.id.expiry_message);
        generateButton        = findViewById(R.id.btn_generate_qr);

        ImageButton backButton = findViewById(R.id.back_button);
        if (backButton != null) backButton.setOnClickListener(v -> finish());

        if (generateButton != null) {
            generateButton.setOnClickListener(v -> generateQrLocally());
        }
    }

<<<<<<< HEAD
    private void generateQrLocally() {
        showMessage("Generating your QR code...");
        if (generateButton != null) generateButton.setVisibility(View.GONE);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Build userId from local parent ID, email, or guest fallback
            SessionManager session = new SessionManager(this);
            long parentId = session.getParentId();
            String userId;
            if (parentId != -1) {
                userId = "parent_" + parentId;
            } else if (session.getParentEmail() != null && !session.getParentEmail().isEmpty()) {
                userId = session.getParentEmail();
            } else {
                userId = "guest";
            }

            // Read child from local DB
            ChildProfile child = AppDatabase.getInstance(this)
                    .childProfileDao().getFirstChildSync();

            String childName = (child != null && child.preferredName != null)
                    ? child.preferredName : "";

            // Mood is empty string if child has not used the comfort page
            String mood = (child != null && child.settingsPreferredGreeting != null)
                    ? child.settingsPreferredGreeting : "";

            // Payload format: userId|childName|mood
            String payload = userId + "|" + childName + "|" + mood;
            Log.d(TAG, "QR payload: " + payload);

            String encrypted = encryptAes(payload);
            if (encrypted == null) {
                runOnUiThread(() -> {
                    showMessage(getString(R.string.qr_not_available));
                    if (generateButton != null) generateButton.setVisibility(View.VISIBLE);
                });
                return;
            }

            runOnUiThread(() -> showQr(encrypted));
        });
    }

    /**
     * AES-128 CBC encryption with PKCS5 padding.
     * Returns a Base64-encoded string the robot can decrypt using the same key and IV.
     */
    private String encryptAes(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(
                    AES_IV.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "AES encryption failed: " + e.getMessage());
            return null;
        }
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
=======
//        // Generate and display QR
//        String userUuid = getOrCreateUuid();
//        Bitmap qrBitmap = generateQrCode(userUuid);
//        if (qrBitmap != null) {
//            qrCodeImageView.setImageBitmap(qrBitmap);
//        }

        //startTimer();

        String qrString =
                "f9f694f2-a217-48ac-b984-f527a7e530f7:5b01bdb74ca1775649572c26668c744224fc94633394c3b39d02edbfd0a35cb6";
        QrGenerator generator = new QrGenerator();
        try {
            Bitmap qrBitmap = generator.generateQRCodeImage(qrString);
            if (qrBitmap != null) {
                qrCodeImageView.setImageBitmap(qrBitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

>>>>>>> welcome-feature-backup

    private void showMessage(String message) {
        if (qrCodeImageView != null) qrCodeImageView.setAlpha(0.15f);
        if (expiryMessageTextView != null) {
            expiryMessageTextView.setText(message);
            expiryMessageTextView.setVisibility(View.VISIBLE);
        }
        if (timerTextView != null) timerTextView.setVisibility(View.GONE);
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        timeLeftInMillis = AppConfig.QR_EXPIRY_MILLIS;
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
        if (timerTextView != null)         timerTextView.setVisibility(View.INVISIBLE);
        if (expiryMessageTextView != null) {
            expiryMessageTextView.setText(R.string.qr_expired);
            expiryMessageTextView.setVisibility(View.VISIBLE);
        }
        if (qrCodeImageView != null)       qrCodeImageView.setAlpha(0.2f);
        // Let the user generate a fresh QR
        if (generateButton != null)        generateButton.setVisibility(View.VISIBLE);
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
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
