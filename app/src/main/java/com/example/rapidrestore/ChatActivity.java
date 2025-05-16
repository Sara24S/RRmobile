package com.example.rapidrestore;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class ChatActivity extends AppCompatActivity {

    private EditText messageInput;
    private Button sendButton;
    private RecyclerView messagesRecyclerView;

    private MessageAdapter messageAdapter;
    private ChatRepository chatRepository;
    private String chatId = "chat_room_1"; // You can replace this with a dynamic chat ID if needed

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);

        chatRepository = new ChatRepository();
        messageAdapter = new MessageAdapter();

        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);




        chatRepository.getMessages(chatId)
                .addSnapshotListener((QuerySnapshot value, FirebaseFirestoreException error) -> {
                    if (value != null) {
                        messageAdapter.setMessages(value.toObjects(Message.class));
                    }
                });


        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                chatRepository.sendMessage(chatId, currentUserId, text);
                messageInput.setText("");
            }
        });

    }
}