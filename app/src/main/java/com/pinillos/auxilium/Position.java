package com.pinillos.auxilium;


public class Position {
    private double lat, lon;
    private String timestamp;
    private String creatorID;

    public Position() {

    }

    public Position(String creatorID, double lat, double lon, String timestamp) {
        this.creatorID  = creatorID;
        this.lat = lat;
        this.lon = lon;
        this.timestamp = timestamp;
    }

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

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

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
