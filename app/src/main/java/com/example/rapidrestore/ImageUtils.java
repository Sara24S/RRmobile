package com.example.rapidrestore;
public class ImageUtils {
    // Replace this with your actual IP or hostname
    private static final String BASE_URL = "http://192.168.1.105:5000/uploads/";

    public static String getImageUrl(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        return BASE_URL + filename;
    }
}
