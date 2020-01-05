package com.example.asistenciaautomatica;

public class Users {
    private String nombre;
    private String correo;
    private String telefono;
    private String latitud;
    private String longitud;
    private Lista idlista;
    private String photo;
    private String idUser;
    private String matricula;

    public Users() {
    }

    public Users(String nombre, String correo, String telefono, String idUser) {
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.idUser = idUser;
    }
    public Users(String nombre, String correo, String telefono, String latitud, String longitud, String idUser) {
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.latitud = latitud;
        this.longitud = longitud;
        this.idUser = idUser;
    }
    public Users(String nombre, String correo, String telefono, String latitud, String longitud, String idUser, String matricula) {
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.latitud = latitud;
        this.longitud = longitud;
        this.matricula = matricula;
        this.idUser = idUser;
    }

    public Users(String nombre, String correo, String telefono, String photo, String idUser) {
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.photo = photo;
        this.idUser = idUser;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getLatitud() {
        return latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public Lista getIdlista() {
        return idlista;
    }

    public String getPhoto() {
        return photo;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public void setIdlista(Lista idlista) {
        this.idlista = idlista;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }
}
