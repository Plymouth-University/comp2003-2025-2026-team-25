package com.example.qtrobot;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class ComfortActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comfort);

        ImageButton goBackButton = findViewById(R.id.go_back_button);
        if (goBackButton != null) goBackButton.setOnClickListener(v -> finish());

        TextView response = findViewById(R.id.comfort_response);

        // Each face button shows a friendly response when tapped
        // Functionality is limited for now — responses can be expanded later

        ImageButton faceHappy = findViewById(R.id.face_happy);
        if (faceHappy != null) faceHappy.setOnClickListener(v ->
                showResponse(response, "That's brilliant! We're so happy to hear that! 😊"));

        ImageButton faceLessHappy = findViewById(R.id.face_less_happy);
        if (faceLessHappy != null) faceLessHappy.setOnClickListener(v ->
                showResponse(response, "That's okay! We'll make sure you feel comfortable."));

        ImageButton faceUndecided = findViewById(R.id.face_undecided);
        if (faceUndecided != null) faceUndecided.setOnClickListener(v ->
                showResponse(response, "Not sure? That's totally fine — we're here to help!"));

        ImageButton faceSad = findViewById(R.id.face_sad);
        if (faceSad != null) faceSad.setOnClickListener(v ->
                showResponse(response, "We understand. We'll take good care of you."));

        ImageButton faceAnxious = findViewById(R.id.face_anxious);
        if (faceAnxious != null) faceAnxious.setOnClickListener(v ->
                showResponse(response, "It's okay to feel nervous. We'll go at your pace."));
    }

    private void showResponse(TextView responseView, String message) {
        if (responseView == null) return;
        responseView.setText(message);
        responseView.setVisibility(View.VISIBLE);
    }
}
