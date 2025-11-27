package com.example.qtrobot;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private EditText chatInput;
    private Button sendButton;
    private ImageButton micButton;
    private boolean isMicOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ImageButton goBackButton = findViewById(R.id.go_back_button);
        goBackButton.setOnClickListener(v -> finish());

        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        chatInput = findViewById(R.id.chat_input);
        sendButton = findViewById(R.id.send_button);
        micButton = findViewById(R.id.mic_on_button);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(v -> sendMessage());

        micButton.setOnClickListener(v -> {
            if (isMicOn) {
                micButton.setImageResource(R.drawable.mic_off);
            } else {
                micButton.setImageResource(R.drawable.mic_on);
            }
            isMicOn = !isMicOn;
        });
    }

    private void sendMessage() {
        String messageText = chatInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // User message
            ChatMessage userMessage = new ChatMessage(messageText, true);
            messageList.add(userMessage);
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            chatRecyclerView.scrollToPosition(messageList.size() - 1);
            chatInput.setText("");

            // Robot reply (dummy)
            ChatMessage robotMessage = new ChatMessage("This is a robot reply", false);
            messageList.add(robotMessage);
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            chatRecyclerView.scrollToPosition(messageList.size() - 1);
        }
    }
}
