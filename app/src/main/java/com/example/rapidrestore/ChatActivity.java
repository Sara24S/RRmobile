package com.example.rapidrestore;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private EditText messageInput;
    private Button sendButton;
    private RecyclerView messagesRecyclerView;

    private MessageAdapter messageAdapter;
    private ChatRepository chatRepository;
    private List<Message> messageList = new ArrayList<>();
    private String chatId, homeownerId, providerId;
    FirebaseFirestore db;
    String id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        db = FirebaseFirestore.getInstance();
        // Get real IDs passed from intent
        homeownerId = getIntent().getStringExtra("homeownerId");
        providerId = getIntent().getStringExtra("providerId");
        //chatId = homeownerId + "_" + providerId;
        //Toast.makeText(ChatActivity.this, chatId, Toast.LENGTH_SHORT).show();

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
        //if the provider opened the chat
        if (!currentUserId.equals(homeownerId)) {
            providerId = currentUserId;
            Toast.makeText(ChatActivity.this, "provider", Toast.LENGTH_SHORT).show();
        }
        else Toast.makeText(ChatActivity.this, "homeowner", Toast.LENGTH_SHORT).show();
        chatId = homeownerId + "_" + providerId;


        db.collection("chats")
                .whereEqualTo("homeownerId",homeownerId)
                .whereEqualTo("providerId",providerId)
                //.whereEqualTo("name","banana")
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        id = doc.getId();
                        //String name = doc.getString("name");
                        Toast.makeText(ChatActivity.this, id, Toast.LENGTH_LONG).show();
                        if(id.isEmpty()){
                            Toast.makeText(ChatActivity.this, "no chat", Toast.LENGTH_LONG).show();
                        }
                    }
                });




        DocumentReference docIdRef = db.collection("chats").document(chatId);
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Toast.makeText(ChatActivity.this, "Document exists!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Document exists!");
                    } else {

                        Toast.makeText(ChatActivity.this, "Document does not exist!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Document does not exist!");

                        Map<String, Object> newChat = new HashMap<>();
                        newChat.put("homeownerId", homeownerId);
                        newChat.put("providerId", providerId);

                        db.collection("chats")
                                .document(chatId)
                                .set(newChat).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(ChatActivity.this, "chat added!!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });







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

        });

        // Send message
        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                try {
                    db.collection("users")
                            .document(currentUserId)
                            .get()
                            .addOnSuccessListener(userDoc -> {
                                String name = userDoc.getString("name");
                                chatRepository.sendMessage(chatId, name, text);
                                messageInput.setText("");
                            });
                } catch (Exception e) {
                    Log.e("ChatError", "Failed to send message", e);
                    Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //chatRepository.getMessages(chatId);




    }
}
