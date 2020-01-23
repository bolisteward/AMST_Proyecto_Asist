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

}
