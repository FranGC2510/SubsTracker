package org.dam.fcojavier.substracker.model;

import org.dam.fcojavier.substracker.utils.enums.Categoria;
import org.dam.fcojavier.substracker.utils.enums.Ciclo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Suscripcion {
    private int idSuscripcion;
    private String nombre;
    private Double precio;
    private Ciclo ciclo;
    private Categoria categoria;
    private boolean activo;
    private LocalDate fechaActivacion;
    private LocalDate fechaRenovacion;
    private Usuario titular;
    private List<Cobro> cobros = new ArrayList<>();
    private List<Participa> participantes = new ArrayList<>();

    public Suscripcion() {}

    public Suscripcion(int idSuscripcion, String nombre, Double precio, Ciclo ciclo, Categoria categoria,
                       LocalDate fechaActivacion, LocalDate fechaRenovacion, Usuario Titular) {
        this.idSuscripcion = idSuscripcion;
        this.nombre = nombre;
        this.precio = precio;
        this.ciclo = ciclo;
        this.categoria = categoria;
        this.activo = true;
        this.fechaActivacion = fechaActivacion;
        this.fechaRenovacion = fechaRenovacion;
        this.titular = Titular;
    }

    public Suscripcion(int idSuscripcion, String nombre, Double precio, Ciclo ciclo, Categoria categoria,
                       LocalDate fechaActivacion, LocalDate fechaRenovacion, Usuario titular,
                       List<Cobro> cobros, List<Participa> participantes) {
        this.idSuscripcion = idSuscripcion;
        this.nombre = nombre;
        this.precio = precio;
        this.ciclo = ciclo;
        this.categoria = categoria;
        this.activo = true;
        this.fechaActivacion = fechaActivacion;
        this.fechaRenovacion = fechaRenovacion;
        this.titular = titular;
        this.cobros = cobros;
        this.participantes = participantes;
    }

    public int getIdSuscripcion() {
        return idSuscripcion;
    }

    public void setIdSuscripcion(int idSuscripcion) {
        this.idSuscripcion = idSuscripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Ciclo getCiclo() {
        return ciclo;
    }

    public void setCiclo(Ciclo ciclo) {
        this.ciclo = ciclo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDate getFechaActivacion() {
        return fechaActivacion;
    }

    public void setFechaActivacion(LocalDate fechaActivacion) {
        this.fechaActivacion = fechaActivacion;
    }

    public LocalDate getFechaRenovacion() {
        return fechaRenovacion;
    }

    public void setFechaRenovacion(LocalDate fechaRenovacion) {
        this.fechaRenovacion = fechaRenovacion;
    }

    public Usuario getTitular() {
        return titular;
    }

    public void setTitular(Usuario titular) {
        this.titular = titular;
    }

    public List<Cobro> getCobros() {
        return cobros;
    }

    public void setCobros(List<Cobro> cobros) {
        this.cobros = cobros;
    }

    public List<Participa> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(List<Participa> participantes) {
        this.participantes = participantes;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Suscripcion that = (Suscripcion) o;
        return idSuscripcion == that.idSuscripcion;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idSuscripcion);
    }

    @Override
    public String toString() {
        return "Suscripcion{" +
                "idSuscripcion=" + idSuscripcion +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", ciclo=" + ciclo +
                ", categoria=" + categoria +
                ", activo=" + activo +
                ", fechaActivacion=" + fechaActivacion +
                ", fechaRenovacion=" + fechaRenovacion +
                ", titular=" + titular +
                ", cobros=" + cobros +
                ", participantes=" + participantes +
                '}';
    }
}
