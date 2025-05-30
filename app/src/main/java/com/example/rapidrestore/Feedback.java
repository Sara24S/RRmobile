package com.example.rapidrestore;

import com.google.firebase.Timestamp;

public class Feedback {
    private String userId;
    private String userType;
    private String comment;
    private Timestamp timestamp;

    public Feedback() {} // Needed for Firebase

    // Getters
    public String getUserId() { return userId; }
    public String getUserType() { return userType; }
    public String getComment() { return comment; }
    public Timestamp getTimestamp() { return timestamp; }
}
