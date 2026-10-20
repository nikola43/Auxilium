package com.pinillos.auxilium.model;


import android.graphics.drawable.Drawable;

public class    UserInformation
{
    //attributes
    private String userID, firstName, lastName1, lastName2, dni, email, group, category;
    private Drawable imagen;

    //Constructors
    public UserInformation()
    {

    }

    public UserInformation(String category, String dni, String email, String firstName, String group, String lastName1, String lastName2, String userID, Drawable imagen)
    {
        this.category = category;
        this.dni = dni;
        this.email = email;
        this.firstName = firstName;
        this.group = group;
        this.lastName1 = lastName1;
        this.lastName2 = lastName2;
        this.userID = userID;
        this.imagen = imagen;
    }

    //getters


    public Drawable getImagen() {
        return imagen;
    }

    public String getUserID() {
        return userID;
    }

    public String getFirstName() {

        return firstName;
    }
    public String getLastName1() {
        return lastName1;
    }

    public String getLastName2() {
        return lastName2;
    }

    public String getDni() {
        return dni;
    }

    public String getEmail() {
        return email;
    }


    public String getGroup() {
        return group;
    }

    public String getCategory() {
        return category;
    }

    //setters


    public void setImagen(Drawable imagen) {
        this.imagen = imagen;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName1(String lastName1) {
        this.lastName1 = lastName1;
    }

    public void setLastName2(String lastName2) {
        this.lastName2 = lastName2;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
