package com.example.asistenciaautomatica;

public class Lista {

    private String idAsistente, horaInicio, horaSalida, estado, idEvento, nombre;
    private int numHoras;

    public Lista() {
    }

    public Lista(String idAsistente,String nombre, String horaInicio, String horaSalida, String estado, String idEvento, int numHoras) {
        this.idAsistente = idAsistente;
        this.horaInicio = horaInicio;
        this.horaSalida = horaSalida;
        this.estado = estado;
        this.idEvento = idEvento;
        this.numHoras = numHoras;
        this.nombre = nombre;
    }

    public Lista(String idAsistente, String nombre, String horaInicio, String estado, String idEvento, int numHoras) {
        this.idAsistente = idAsistente;
        this.horaInicio = horaInicio;
        this.estado = estado;
        this.idEvento = idEvento;
        this.nombre = nombre;
        this.numHoras = numHoras;
    }

    public String getNombre() {
        return nombre;
    }

    public String getIdAsistente() {
        return idAsistente;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public String getEstado() {
        return estado;
    }

    public String getIdEvento() {
        return idEvento;
    }

    public int getNumHoras() {
        return numHoras;
    }
}
