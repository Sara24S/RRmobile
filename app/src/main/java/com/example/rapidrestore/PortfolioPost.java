package com.example.rapidrestore;

import java.util.ArrayList;
import java.util.List;

/*public class PortfolioPost {
    public List<String> imageUrls;  // ImageKit URLs
    public String description;

    public PortfolioPost(List<String> imageUrls, String description) {
        this.description = description != null ? description : "";
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }
}

 */
public class PortfolioPost {
    public String description;
    public List<String> imageUrls;

    public PortfolioPost() {} // Needed for Firestore

    public PortfolioPost(String description, List<String> imageUrls) {
        this.description = description;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }
}
