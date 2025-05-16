package com.example.rapidrestore;

import java.util.List;

public class PortfolioPost {
    public List<String> imageUrls;  // ImageKit URLs
    public String description;

    public PortfolioPost(List<String> imageUrls, String description) {
        this.imageUrls = imageUrls;
        this.description = description;
    }
}
