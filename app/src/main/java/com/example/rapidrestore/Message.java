package com.example.rapidrestore;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Message {
    private String senderId;
    private String text;

    @ServerTimestamp
    private Date timestamp;

    // Required empty constructor for Firestore
    public Message() {}

    // Full constructor used when sending messages
    public Message(String senderId, String text, Date timestamp) {
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters
    public String getSenderId() {
        return senderId;
    }

    public String getText() {
        return text;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
