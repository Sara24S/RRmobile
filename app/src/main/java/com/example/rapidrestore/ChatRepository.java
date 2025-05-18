package com.example.rapidrestore;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public interface OnMessagesFetchedListener {
        void onMessagesUpdated(List<Message> messageList);
    }

    public void listenForMessages(String chatRoomId, OnMessagesFetchedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    List<Message> messages = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Message msg = doc.toObject(Message.class);
                        messages.add(msg);
                    }
                    listener.onMessagesUpdated(messages);
                });
    }

}
