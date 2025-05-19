package com.example.rapidrestore;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    private String chatId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get real IDs passed from intent
        String homeownerId = getIntent().getStringExtra("homeownerId");
        String providerId = getIntent().getStringExtra("providerId");


        // Generate a unique chatId for both sides
        chatId = getIntent().getStringExtra("chatId");

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get current user ID
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity safely
            return;
        }
        String currentUserId = auth.getCurrentUser().getUid();


        // Initialize views
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);

        // Initialize adapter and repository
        messageAdapter = new MessageAdapter();
        chatRepository = new ChatRepository();

        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        // Start listening to messages
        chatRepository.listenForMessages(chatId, updatedMessages -> {
            messageList.clear();
            messageList.addAll(updatedMessages);
            messageAdapter.setMessages(messageList);
            messagesRecyclerView.scrollToPosition(messageList.size() - 1);
            chatId = getIntent().getStringExtra("chatId");

        });

        // Send message
        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                try {
                    chatRepository.sendMessage(chatId, currentUserId, text);
                    messageInput.setText("");
                } catch (Exception e) {
                    Log.e("ChatError", "Failed to send message", e);
                    Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}
