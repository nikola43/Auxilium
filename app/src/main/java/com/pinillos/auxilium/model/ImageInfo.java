package com.pinillos.auxilium.model;


public class ImageInfo
{
    //attributes
    private String name;
    private String url;
    private String creadorID;
    private double lat;
    private double lon;
    private String timeStamp;


    //constructors
    public ImageInfo()
    {

    }

    public ImageInfo(String name, String url, String creadorID, double lat, double lon, String timeStamp)
    {
        this.name = name;
        this.url = url;
        this.creadorID = creadorID;
        this.lat = lat;
        this.lon = lon;
        this.timeStamp = timeStamp;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getCreadorID() {
        return creadorID;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCreadorID(String creadorID) {
        this.creadorID = creadorID;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}

