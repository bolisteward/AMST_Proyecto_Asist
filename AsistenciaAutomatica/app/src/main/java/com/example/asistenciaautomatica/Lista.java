package com.example.asistenciaautomatica;

public class Lista {

    private String idAsistente, horaInicio, horaSalida, estado, idEvento, nombre;
    private int numHoras;

    public Lista() {
    }

    public Lista(String idAsistente, String nombre, String horaInicio, String estado, String idEvento, int numHoras) {
        this.idAsistente = idAsistente;
        this.horaInicio = horaInicio;
        this.estado = estado;
        this.idEvento = idEvento;
        this.nombre = nombre;
        this.numHoras = numHoras;
    }

}
