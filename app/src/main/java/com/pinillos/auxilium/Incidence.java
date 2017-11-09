package com.pinillos.auxilium;

import android.graphics.drawable.Drawable;

public class Incidence
{
    //attributes
    private String creatorID;
    private boolean status;
    private double lat;
    private double lon;
    private String type;
    private String timestamp;
    private Drawable imagen;

    //constructors
    public Incidence()
    {

    }

    public Incidence(String creatorID, double lat, double lon, boolean status, String timestamp, String type, Drawable imagen)
    {
        this.creatorID = creatorID;
        this.lat = lat;
        this.lon = lon;
        this.status = status;
        this.timestamp = timestamp;
        this.type = type;

        this.imagen = imagen;
    }

    public Drawable getImage() {
        return imagen;
    }

    public void setImagen(Drawable imagen) {
        this.imagen = imagen;
    }

    //getters
    public String getCreatorID() {
        return creatorID;
    }
    public boolean getStatus() {
        return status;
    }
    public double getLat() {
        return lat;
    }
    public double getLon() {
        return lon;
    }
    public String getType() {
        return type;
    }
    public String getTimestamp() {
        return timestamp;
    }

    //setters
    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }
    public void setStatus(boolean status) {
        this.status = status;
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
    public void setType(String type) {
        this.type = type;
    }
}
