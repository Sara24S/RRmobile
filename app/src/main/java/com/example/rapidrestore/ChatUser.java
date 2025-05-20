package com.example.rapidrestore;

public class ChatUser {
    private String uid;
    private String name;

    public ChatUser(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }
}
