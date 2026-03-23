package com.example.qtrobot;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import nl.dionsegijn.konfetti.core.models.Size;

import android.os.Handler;
import android.os.Looper;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.example.qtrobot.data.LearnSectionConstants;
import com.example.qtrobot.data.local.database.AppRoomDatabase;
import com.example.qtrobot.data.local.entity.LearnProgress;
import com.example.qtrobot.data.repository.DataRepository;
import com.example.qtrobot.ui.viewmodel.ChildViewModel;

import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class LearnActivity extends BaseActivity {

    private CardView cardArrival, cardBefore, cardDuring, cardAfter;
    private ImageView badgeArrival, badgeBefore, badgeDuring, badgeAfter;
    private TextView progressLabel;

    private ChildViewModel childViewModel;
    private long currentChildId = -1;
    private boolean isGuest = true;
    private String pendingSectionId = null;
    private KonfettiView konfettiView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        // --- Find all views ---
        cardArrival = findViewById(R.id.card_arrival);
        cardBefore = findViewById(R.id.card_before_appointment);
        cardDuring = findViewById(R.id.card_during_appointment);
        cardAfter = findViewById(R.id.card_after_appointment);

        badgeArrival = findViewById(R.id.badge_arrival);
        badgeBefore = findViewById(R.id.badge_before);
        badgeDuring = findViewById(R.id.badge_during);
        badgeAfter = findViewById(R.id.badge_after);

        progressLabel = findViewById(R.id.tutorial_progress_label);

        konfettiView = findViewById(R.id.konfettiView);


        ImageButton goBackButton = findViewById(R.id.go_back_button);
        if (goBackButton != null) {
            goBackButton.setOnClickListener(v -> finish());
        }

        // --- Check if user is a guest ---
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        isGuest = prefs.getBoolean("is_guest", true);

        // --- If logged in, get child ID and load their saved progress ---
        if (!isGuest) {
            childViewModel = new ViewModelProvider(this).get(ChildViewModel.class);
            childViewModel.getChildFromRoom().observe(this, child -> {
                if (child == null) return;
                currentChildId = child.id;

                // Load completed sections from Room on a background thread
                AppRoomDatabase.databaseWriteExecutor.execute(() -> {
                    List<LearnProgress> completedList =
                            DataRepository.getInstance(getApplication())
                                    .getCompletedSectionsList(currentChildId);

                    // Update the UI back on the main thread
                    runOnUiThread(() -> {
                        for (LearnProgress p : completedList) {
                            showBadge(p.sectionId);
                        }
                        updateProgressLabel();
                    });
                });
            });
        }

        // --- Card click listeners ---
        cardArrival.setOnClickListener(v -> {
            pendingSectionId = LearnSectionConstants.SECTION_ARRIVAL;
            startActivity(new Intent(this, ArrivalActivity.class));
        });

        cardBefore.setOnClickListener(v -> {
            pendingSectionId = LearnSectionConstants.SECTION_BEFORE;
            startActivity(new Intent(this, BeforeAppointmentActivity.class));
        });

        cardDuring.setOnClickListener(v -> {
            pendingSectionId = LearnSectionConstants.SECTION_DURING;
            startActivity(new Intent(this, DuringAppointmentActivity.class));
        });

        cardAfter.setOnClickListener(v -> {
            pendingSectionId = LearnSectionConstants.SECTION_AFTER;
            startActivity(new Intent(this, AfterAppointmentActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (pendingSectionId != null) {
            if (!isGuest && currentChildId != -1) {
                childViewModel.recordSectionProgress(currentChildId, pendingSectionId);
            }
            showBadge(pendingSectionId);
            pendingSectionId = null;
            updateProgressLabel();

        }
    }

    // Makes the checkmark badge visible on the correct card
    private void showBadge(String sectionId) {
        if (sectionId.equals(LearnSectionConstants.SECTION_ARRIVAL)) {
            badgeArrival.setVisibility(ImageView.VISIBLE);
        } else if (sectionId.equals(LearnSectionConstants.SECTION_BEFORE)) {
            badgeBefore.setVisibility(ImageView.VISIBLE);
        } else if (sectionId.equals(LearnSectionConstants.SECTION_DURING)) {
            badgeDuring.setVisibility(ImageView.VISIBLE);
        } else if (sectionId.equals(LearnSectionConstants.SECTION_AFTER)) {
            badgeAfter.setVisibility(ImageView.VISIBLE);
        }
    }

    // Updates the "X of 4 videos completed" label
    private void updateProgressLabel() {
        int count = 0;
        if (badgeArrival.getVisibility() == ImageView.VISIBLE) count++;
        if (badgeBefore.getVisibility() == ImageView.VISIBLE) count++;
        if (badgeDuring.getVisibility() == ImageView.VISIBLE) count++;
        if (badgeAfter.getVisibility() == ImageView.VISIBLE) count++;
        progressLabel.setText(count + " of 4 videos completed");

        // if all completed and not navigating anywhere:
        if (count == 4 && pendingSectionId == null) {
            showCompletionCelebration(); // Show confetti and congrats alert
        }
    }

    private void showCompletionCelebration() {
        Party party = new PartyFactory(new Emitter(Long.MAX_VALUE, TimeUnit.SECONDS).perSecond(80))
                .spread(360)
                .sizes(new Size(12, 4f, 1f))
                .build();
        konfettiView.start(party);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("🎉 Well Done! ")
                    .setMessage("You've completed all the lessons!\nYou're ready for your dentist visit!")
                    //.setIcon(R.drawable.qtrobot)
                    .setPositiveButton("Thanks!", (dialog, which) -> {
                            konfettiView.reset();
                            dialog.dismiss();
                    })
                    .setOnDismissListener(dialog -> konfettiView.reset())
                    .show(); }, 1500);

    }
}
