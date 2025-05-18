package com.example.rapidrestore;
public class ImageUtils {
    private static final String BASE_UPLOAD_URL = "http://192.168.1.112:5000/upload";
    private static final String BASE_RETRIEVE_URL = "http://192.168.1.112:5000/uploads/";

    public static String getImageUrl(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        return BASE_RETRIEVE_URL + filename;
    }
    public static String getUrl() {

        return BASE_UPLOAD_URL;
    }
}
