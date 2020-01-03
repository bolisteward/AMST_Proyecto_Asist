package com.example.asistenciaautomatica;

public class Lista {

    private String idAsistente, horaInicio, horaSalida, estado, idEvento;
    private int numHoras;

    public Lista() {
    }

    public Lista(String idAsistente, String horaInicio, String horaSalida, String estado, String idEvento, int numHoras) {
        this.idAsistente = idAsistente;
        this.horaInicio = horaInicio;
        this.horaSalida = horaSalida;
        this.estado = estado;
        this.idEvento = idEvento;
        this.numHoras = numHoras;
    }

    public Lista(String idAsistente, String horaInicio, String estado, String idEvento) {
        this.idAsistente = idAsistente;
        this.horaInicio = horaInicio;
        this.estado = estado;
        this.idEvento = idEvento;
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
