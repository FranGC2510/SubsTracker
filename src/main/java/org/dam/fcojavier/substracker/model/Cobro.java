package org.dam.fcojavier.substracker.model;

import org.dam.fcojavier.substracker.utils.enums.MetodoPago;

import java.time.LocalDate;
import java.util.Objects;

public class Cobro {
    private int id;
    private LocalDate fecha_pagado;
    private MetodoPago metodo_pago;
    private int periodos_cubiertos;
    private String descripcion;
    private Suscripcion suscripcion;

    public Cobro() {}

    public Cobro(int id, Suscripcion suscripcion) {
        this.id = id;
        this.suscripcion = suscripcion;
    }

    public Cobro(int id, LocalDate fecha_pagado, MetodoPago metodo_pago, int periodos_cubiertos,
                 String descripcion, Suscripcion suscripcion) {
        this.id = id;
        this.fecha_pagado = fecha_pagado;
        this.metodo_pago = metodo_pago;
        this.periodos_cubiertos = periodos_cubiertos;
        this.descripcion = descripcion;
        this.suscripcion = suscripcion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Cobro cobro = (Cobro) o;
        return id == cobro.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Cobro{" +
                "id=" + id +
                ", fecha_pagado=" + fecha_pagado +
                ", metodo_pago=" + metodo_pago +
                ", periodos_cubiertos=" + periodos_cubiertos +
                ", descripcion='" + descripcion + '\'' +
                ", suscripcion=" + suscripcion +
                '}';
    }
}
