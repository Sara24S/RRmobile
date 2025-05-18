package com.example.rapidrestore;

import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class ProvRequest {
    private String name;
    private String id;
  /*  private String email;
    private int number;
    private String address;
    private String profession;
    private String certification;
    private String image;
    private String experience;
    private String region;
    private String providerID;

   */
    private String dateSubmitted;
    private String state;

    // Required empty constructor for Firestore deserialization
    public ProvRequest() {}

    public ProvRequest(String id, String name, String dateSubmitted, String state) {
        this.id = id;
        this.name = name;
        this.dateSubmitted = dateSubmitted;
        this.state = state;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }

    public String getState() { return state; }
    public String getDateSubmitted() { return dateSubmitted; }

}

