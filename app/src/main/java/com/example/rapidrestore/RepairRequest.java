package com.example.rapidrestore;


import java.util.Date;
import java.util.List;

public class RepairRequest {
    private String id;
    private String name;
    private String state;
    private Date timestamp;
    private String description;
    private List<String> images;

    public RepairRequest() {}  // Firestore requires empty constructor

    public RepairRequest(String id, String name, String state, Date timestamp, String description, List<String> images) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.timestamp = timestamp;
        this.description = description;
        this.images = images;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getState() { return state; }
    public Date getTimestamp() { return timestamp; }
    public String getDescription() { return description; }
    public List<String> getImages() { return images; }
}
