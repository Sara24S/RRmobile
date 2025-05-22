package com.example.rapidrestore;
public class Review {
    private String comment;
    private Long rating;
    private String userName;

    public Review() {
        // Firestore needs a public no-arg constructor
    }

    public Review(String comment, Long rating, String userName) {
        this.comment = comment;
        this.rating = rating;
        this.userName = userName;
    }

    public String getComment() {
        return comment;
    }
    public String getUserName() {
        return userName;
    }

    public Long getRating() {
        return rating;
    }
}