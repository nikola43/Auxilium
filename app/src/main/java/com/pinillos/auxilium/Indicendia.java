package com.pinillos.auxilium;

public class Indicendia
{
    //------------------------------- PROPIEDADES -----------------------------------------------//
    //BASICAS
    private boolean estado;
    private double latitud, longitud;
    private long numeroIncidencia;
    private String tipo, fechaHora;
    //------------------------------- FIN PROPIEDADES --------------------------------------------//

    //------------------------------- CONSTRUCTORES ----------------------------------------------//
    //CONSTRUCTOR POR DEFECTO
    public Indicendia()
    {
        this.numeroIncidencia = 0;
        this.estado = false;
        this.latitud = 0;
        this.longitud = 0;
        this.tipo = null;
        this.fechaHora = null;
    }

    //CONSTRUCTOR SOBRECARGADO
    public Indicendia(long numeroIncidencia,boolean estado, double latitud, double longitud, String tipo, String fechaHora)
    {
        this.numeroIncidencia = numeroIncidencia;
        this.estado = estado;
        this.latitud = latitud;
        this.longitud = longitud;
        this.tipo = tipo;
        this.fechaHora = fechaHora;
    }

    //CONSTRUCTOR DE COPIA
    //------------------------------- FIN CONSTRUCTORES ------------------------------------------//

    //------------------------------- METODOS CONSULTORES ----------------------------------------//


    public boolean getEstado() {
        return estado;
    }

    public long getNumeroIncidencia() {
        return numeroIncidencia;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public String getTipo() {
        return tipo;
    }

    public String getFechaHora() {
        return fechaHora;
    }
    //------------------------------- FIN METODOS CONSULTORES ------------------------------------//

    //------------------------------- METODOS MODIFICADORES --------------------------------------//

    public void setNumeroIncidencia(long numeroIncidencia) {
        this.numeroIncidencia = numeroIncidencia;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
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
