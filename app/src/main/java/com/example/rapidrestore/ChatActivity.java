package com.example.rapidrestore;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private EditText messageInput;
    private Button sendButton;
    private RecyclerView messagesRecyclerView;

    private MessageAdapter messageAdapter;
    private ChatRepository chatRepository;
    private List<Message> messageList = new ArrayList<>();
    private String chatId = "homeownerId_providerId";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize views
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);

        // Initialize adapter and repository
        messageAdapter = new MessageAdapter();
        chatRepository = new ChatRepository();


        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);


        chatRepository.listenForMessages(chatId, updatedMessages -> {
            messageList.clear();
            messageList.addAll(updatedMessages);
            messageAdapter.notifyDataSetChanged();
            messagesRecyclerView.scrollToPosition(messageList.size() - 1);
        });

        // Send message logic
        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                chatRepository.sendMessage(chatId, currentUserId, text);
                messageInput.setText("");
            }
        });
    }

}

