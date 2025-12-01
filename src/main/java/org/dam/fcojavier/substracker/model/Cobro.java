package org.dam.fcojavier.substracker.model;

import org.dam.fcojavier.substracker.model.enums.MetodoPago;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Representa un registro de pago (Cobro) realizado por el titular de una suscripción.
 *
 * Esta entidad almacena la información financiera histórica, detallando cuándo se realizó
 * el pago, qué método se utilizó y cuántos ciclos de facturación cubre dicho pago.
 *
 * @author Fco Javier García
 * @version 1.0
 */
public class Cobro {

    /**
     * Identificador único del cobro en la base de datos.
     */
    private int id_cobro;

    /**
     * Fecha en la que se realizó efectivamente el pago.
     */
    private LocalDate fecha_cobro;

    /**
     * Método de pago utilizado (Tarjeta, Efectivo, Bizum, etc.).
     */
    private MetodoPago metodo_pago;

    /**
     * Número de ciclos de la suscripción que cubre este pago.
     * Por ejemplo: 1 para un mes normal, 12 para un pago anual.
     */
    private int periodos_cubiertos;

    /**
     * Notas o detalles adicionales sobre el pago.
     */
    private String descripcion;

    /**
     * La suscripción a la que pertenece este cobro.
     */
    private Suscripcion suscripcion;

    /**
     * Constructor vacío por defecto.
     * Necesario para frameworks y operaciones de instanciación genérica.
     */
    public Cobro() {}

    /**
     * Constructor parcial.
     * Útil para crear objetos temporales o referencias rápidas.
     *
     * @param id_cobro Identificador único del cobro.
     * @param suscripcion Suscripción asociada.
     */
    public Cobro(int id_cobro, Suscripcion suscripcion) {
        this.id_cobro = id_cobro;
        this.suscripcion = suscripcion;
    }

    /**
     * Constructor completo.
     * Inicializa todos los atributos de la clase.
     *
     * @param id_cobro Identificador único.
     * @param fecha_cobro Fecha del pago.
     * @param metodo_pago Enum del método de pago.
     * @param periodos_cubiertos Cantidad de periodos pagados.
     * @param descripcion Nota opcional.
     * @param suscripcion Objeto suscripción asociado.
     */
    public Cobro(int id_cobro, LocalDate fecha_cobro, MetodoPago metodo_pago, int periodos_cubiertos,
                 String descripcion, Suscripcion suscripcion) {
        this.id_cobro = id_cobro;
        this.fecha_cobro = fecha_cobro;
        this.metodo_pago = metodo_pago;
        this.periodos_cubiertos = periodos_cubiertos;
        this.descripcion = descripcion;
        this.suscripcion = suscripcion;
    }

    /**
     * Obtiene el ID del cobro.
     * @return El identificador único (int).
     */
    public int getId_cobro() {
        return id_cobro;
    }

    /**
     * Establece el ID del cobro.
     * @param id_cobro El nuevo identificador.
     */
    public void setId_cobro(int id_cobro) {
        this.id_cobro = id_cobro;
    }

    /**
     * Obtiene la fecha del pago.
     * @return LocalDate con la fecha del cobro.
     */
    public LocalDate getFecha_cobro() {
        return fecha_cobro;
    }

    /**
     * Establece la fecha del pago.
     * @param fecha_cobro La fecha en que se realizó el cobro.
     */
    public void setFecha_cobro(LocalDate fecha_cobro) {
        this.fecha_cobro = fecha_cobro;
    }

    /**
     * Obtiene el método de pago usado.
     * @return Enum MetodoPago.
     */
    public MetodoPago getMetodo_pago() {
        return metodo_pago;
    }

    /**
     * Define el método de pago.
     * @param metodo_pago El método utilizado (ej. TARJETA, BIZUM).
     */
    public void setMetodo_pago(MetodoPago metodo_pago) {
        this.metodo_pago = metodo_pago;
    }

    /**
     * Obtiene cuántos periodos cubre este pago.
     * @return Entero con el número de periodos.
     */
    public int getPeriodos_cubiertos() {
        return periodos_cubiertos;
    }

    /**
     * Establece los periodos cubiertos.
     * @param periodos_cubiertos Cantidad de ciclos que abarca el pago.
     */
    public void setPeriodos_cubiertos(int periodos_cubiertos) {
        this.periodos_cubiertos = periodos_cubiertos;
    }

    /**
     * Obtiene la descripción o notas del cobro.
     * @return String con la descripción.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece una descripción para el cobro.
     * @param descripcion Texto libre con detalles del cobro.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene la suscripción asociada a este cobro.
     * @return Objeto Suscripcion completo o parcial.
     */
    public Suscripcion getSuscripcion() {
        return suscripcion;
    }

    /**
     * Asocia una suscripción a este cobro.
     * @param suscripcion La suscripción que genera el cobro.
     */
    public void setSuscripcion(Suscripcion suscripcion) {
        this.suscripcion = suscripcion;
    }

    /**
     * Compara si dos cobros son iguales basándose en su ID.
     *
     * @param o Objeto a comparar.
     * @return true si los IDs coinciden, false en caso contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Cobro cobro = (Cobro) o;
        return id_cobro == cobro.id_cobro;
    }

    /**
     * Genera un hash code basado en el ID del cobro.
     * @return int representando el hash.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id_cobro);
    }

    /**
     * Devuelve una representación en texto del objeto Cobro.
     * Útil para logs y depuración.
     *
     * @return String con los valores de los atributos.
     */
    @Override
    public String toString() {
        return "Cobro{" +
                "id=" + id_cobro +
                ", fecha_pagado=" + fecha_cobro +
                ", metodo_pago=" + metodo_pago +
                ", periodos_cubiertos=" + periodos_cubiertos +
                ", descripcion='" + descripcion + '\'' +
                ", suscripcion=" + suscripcion +
                '}';
    }
}