package com.pinillos.auxilium;

public class Incidence
{
    //------------------------------- PROPIEDADES -----------------------------------------------//
    //BASICAS
    private boolean status;
    private double lat;
    private double lon;
    private String type;
    private String timestamp;
    //------------------------------- FIN PROPIEDADES --------------------------------------------//

    //------------------------------- CONSTRUCTORES ----------------------------------------------//
    //CONSTRUCTOR POR DEFECTO
    public Incidence()
    {

    }

    //CONSTRUCTOR SOBRECARGADO
    public Incidence(double lat, double lon, boolean status, String timestamp, String type)
    {

        this.lat = lat;
        this.lon = lon;
        this.status = status;
        this.timestamp = timestamp;
        this.type = type;

    }

    //CONSTRUCTOR DE COPIA
    //------------------------------- FIN CONSTRUCTORES ------------------------------------------//

    //------------------------------- METODOS CONSULTORES ----------------------------------------//


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
    //------------------------------- FIN METODOS CONSULTORES ------------------------------------//

    //------------------------------- METODOS MODIFICADORES --------------------------------------//

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
    //------------------------------- FIN METODOS MODIFICADORES ----------------------------------//

    //------------------------------- METODOS HEREDADOS ------------------------------------------//
    //------------------------------- FIN METODOS HEREDADOS --------------------------------------//

    //------------------------------- METODOS AÑADIDOS -------------------------------------------//
    //------------------------------- FIN METODOS AÑADIDOS ---------------------------------------//
}
