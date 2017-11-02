package com.pinillos.auxilium;

public class Incidencia
{
    //------------------------------- PROPIEDADES -----------------------------------------------//
    //BASICAS
    private boolean estado;
    private double lat;
    private double lon;
    private String tipo;
    private String timestamp;
    //------------------------------- FIN PROPIEDADES --------------------------------------------//

    //------------------------------- CONSTRUCTORES ----------------------------------------------//
    //CONSTRUCTOR POR DEFECTO
    public Incidencia()
    {

    }

    //CONSTRUCTOR SOBRECARGADO
    public Incidencia(boolean estado, double lat, double lon, String tipo, String timestamp)
    {
        this.estado = estado;
        this.lat = lat;
        this.lon = lon;
        this.tipo = tipo;
        this.timestamp = timestamp;
    }

    //CONSTRUCTOR DE COPIA
    //------------------------------- FIN CONSTRUCTORES ------------------------------------------//

    //------------------------------- METODOS CONSULTORES ----------------------------------------//


    public boolean getEstado() {
        return estado;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getTipo() {
        return tipo;
    }

    public String getTimestamp() {
        return timestamp;
    }
    //------------------------------- FIN METODOS CONSULTORES ------------------------------------//

    //------------------------------- METODOS MODIFICADORES --------------------------------------//

    public void setEstado(boolean estado) {
        this.estado = estado;
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

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    //------------------------------- FIN METODOS MODIFICADORES ----------------------------------//

    //------------------------------- METODOS HEREDADOS ------------------------------------------//
    //------------------------------- FIN METODOS HEREDADOS --------------------------------------//

    //------------------------------- METODOS AÑADIDOS -------------------------------------------//
    //------------------------------- FIN METODOS AÑADIDOS ---------------------------------------//
}
