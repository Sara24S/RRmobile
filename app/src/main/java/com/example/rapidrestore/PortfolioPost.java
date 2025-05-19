package com.example.rapidrestore;

import java.util.ArrayList;
import java.util.List;

public class PortfolioPost {
    public String description;
    public String date;
    public List<String> imageUrls;
    public PortfolioPost() {} // Needed for Firestore
    public PortfolioPost(String description, List<String> imageUrls, String date) {
        this.description = description;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.date = date;

    }
}
