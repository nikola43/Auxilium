package com.pinillos.auxilium;


public class Position {

    //attributes
    private double lat, lon;
    private String timestamp;
    private String creatorID;

    //Constructors
    public Position() {

    }

    public Position(String creatorID, double lat, double lon, String timestamp) {
        this.creatorID  = creatorID;
        this.lat = lat;
        this.lon = lon;
        this.timestamp = timestamp;
    }

    //getters
    public double getLat() {
        return lat;
    }
    public double getLon() {
        return lon;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public String getCreatorID() {
        return creatorID;
    }
    public void setCreatorID(String creatorID) { this.creatorID = creatorID;
    }

    //setters
    public void setLat(double lat) {
        this.lat = lat;
    }
    public void setLon(double lon) {
        this.lon = lon;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
