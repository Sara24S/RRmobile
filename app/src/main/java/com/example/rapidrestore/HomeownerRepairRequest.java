package com.example.rapidrestore;

public class HomeownerRepairRequest {
    private String id;
    private String providerId;
    private String providerName;
    private String date;
    private String status;
    private String location;

    public HomeownerRepairRequest() {}

    public HomeownerRepairRequest(String id, String providerId, String providerName, String date, String status, String location) {
        this.id = id;
        this.providerId = providerId;
        this.providerName = providerName;
        this.date = date;
        this.status = status;
        this.location = location;
    }

    public String getProviderName() { return providerName; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public String getLocation() { return location; }

    public void setStatus(String status) { this.status = status; }

    public String getId() { return id; }
    public String getProviderId() { return providerId; }
}
