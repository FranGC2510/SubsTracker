package org.dam.fcojavier.substracker.model;

import org.dam.fcojavier.substracker.utils.enums.MetodoPago;

import java.time.LocalDate;
import java.util.Objects;

public class Participa {
    private Double aporte;
    private LocalDate fecha_pagado;
    private MetodoPago metodo_pago;
    private int periodos_cubiertos;
    private String descripcion;
    private Suscripcion suscripcion;
    private Usuario participante;

    public Participa() {}

    public Participa(Double aporte, Suscripcion suscripcion, Usuario participante) {
        this.aporte = aporte;
        this.suscripcion = suscripcion;
        this.participante = participante;
    }

    public Participa(Double aporte, LocalDate fecha_pagado, MetodoPago metodo_pago, int periodos_cubiertos,
                     String descripcion, Suscripcion suscripcion, Usuario participante) {
        this.aporte = aporte;
        this.fecha_pagado = fecha_pagado;
        this.metodo_pago = metodo_pago;
        this.periodos_cubiertos = periodos_cubiertos;
        this.descripcion = descripcion;
        this.suscripcion = suscripcion;
        this.participante = participante;
    }

    public Double getAporte() {
        return aporte;
    }

    public void setAporte(Double aporte) {
        this.aporte = aporte;
    }

    public LocalDate getFecha_pagado() {
        return fecha_pagado;
    }

    public void setFecha_pagado(LocalDate fecha_pagado) {
        this.fecha_pagado = fecha_pagado;
    }

    public MetodoPago getMetodo_pago() {
        return metodo_pago;
    }

    public void setMetodo_pago(MetodoPago metodo_pago) {
        this.metodo_pago = metodo_pago;
    }

    public int getPeriodos_cubiertos() {
        return periodos_cubiertos;
    }

    public void setPeriodos_cubiertos(int periodos_cubiertos) {
        this.periodos_cubiertos = periodos_cubiertos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Suscripcion getSuscripcion() {
        return suscripcion;
    }

    public void setSuscripcion(Suscripcion suscripcion) {
        this.suscripcion = suscripcion;
    }

    public Usuario getParticipante() {
        return participante;
    }

    public void setParticipante(Usuario participante) {
        this.participante = participante;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Participa participa = (Participa) o;
        return Objects.equals(suscripcion, participa.suscripcion) && Objects.equals(participante, participa.participante);
    }

    @Override
    public int hashCode() {
        return Objects.hash(suscripcion, participante);
    }

    @Override
    public String toString() {
        return "Participa{" +
                "aporte=" + aporte +
                ", fecha_pagado=" + fecha_pagado +
                ", metodo_pago=" + metodo_pago +
                ", periodos_cubiertos=" + periodos_cubiertos +
                ", descripcion='" + descripcion + '\'' +
                ", suscripcion=" + suscripcion +
                ", participante=" + participante +
                '}';
    }
}
