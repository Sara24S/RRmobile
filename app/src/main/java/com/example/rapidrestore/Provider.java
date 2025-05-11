package com.example.rapidrestore;

public class Provider {
    private String id;
    private String name;
    private String profession;
    private String region;
    private double rating;
    private double costperhour;
    private String profileimage;

    public Provider(String id, String name, String profession, double  rating, double price, String region, String image) {
        this.id = id;
        this.name = name;
        this.profession = profession;
        this.rating = rating;
        this.costperhour = price;
        this.profileimage = image;
        this.region = region;
    }

    public String getId() {
        return id;
    }

    public String getRegion() {
        return region;
    }

    public String getName() {
        return name;
    }

    public String getProfession() {
        return profession;
    }

    public double getRating() {
        return rating;
    }

    public double getPrice() {
        return costperhour;
    }

    public String getImage() {
        return profileimage;
    }

}