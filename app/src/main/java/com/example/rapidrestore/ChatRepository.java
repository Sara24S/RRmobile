package com.example.rapidrestore;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class ChatRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void sendMessage(String chatId, String senderId, String text) {
        CollectionReference messagesRef = db.collection("chats")
                .document(chatId)
                .collection("messages");

        Map<String, Object> message = new HashMap<>();
        message.put("senderId", senderId);
        message.put("text", text);
        message.put("timestamp", Timestamp.now());

        messagesRef.add(message);
    }

    public Query getMessages(String chatId) {
        return db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING);
    }
}
