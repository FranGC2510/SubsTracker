package org.dam.fcojavier.substracker.model;

import org.dam.fcojavier.substracker.model.enums.MetodoPago;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Representa la participación económica de un Usuario en una Suscripción (Relación N:M).
 *
 * Esta clase modela la entidad asociativa "Participa" (o Copagador). Almacena los detalles
 * específicos del aporte que realiza un usuario secundario para ayudar a pagar una suscripción
 * titular, incluyendo el monto, la fecha y el método de pago.
 *
 * La identidad de esta clase se define por la combinación de {@link Usuario} y {@link Suscripcion}.
 *
 * @author Fco Javier García
 * @version 1.0
 */
public class Participa {

    /**
     * Cantidad monetaria que el participante aporta.
     */
    private Double cantidadApagar;

    /**
     * Fecha en la que se realizó o registró el aporte.
     */
    private LocalDate fecha_pagado;

    /**
     * Método de pago utilizado para este aporte específico (ej. BIZUM, EFECTIVO).
     */
    private MetodoPago metodo_pago;

    /**
     * Número de periodos (meses/años) que cubre este aporte concreto.
     */
    private int periodos_cubiertos;

    /**
     * Notas adicionales sobre el aporte (ej. "Pago correspondiente a Enero").
     */
    private String descripcion;

    /**
     * La suscripción a la que se está contribuyendo.
     * Parte de la clave compuesta.
     */
    private Suscripcion suscripcion;

    /**
     * El usuario que realiza el aporte (Copagador).
     * Parte de la clave compuesta.
     */
    private Usuario participante;

    /**
     * Constructor vacío por defecto.
     * Necesario para operaciones de frameworks y creación de instancias sin datos iniciales.
     */
    public Participa() {}

    /**
     * Constructor esencial.
     * Inicializa la participación con los datos mínimos obligatorios para establecer la relación.
     *
     * @param cantidadApagar Monto del aporte.
     * @param suscripcion La suscripción destino.
     * @param participante El usuario que paga.
     */
    public Participa(Double cantidadApagar, Suscripcion suscripcion, Usuario participante) {
        this.cantidadApagar = cantidadApagar;
        this.suscripcion = suscripcion;
        this.participante = participante;
    }

    /**
     * Constructor completo.
     * Inicializa todos los atributos de la participación.
     *
     * @param cantidadApagar Monto del aporte.
     * @param fecha_pagado Fecha del pago.
     * @param metodo_pago Método utilizado.
     * @param periodos_cubiertos Ciclos cubiertos.
     * @param descripcion Nota descriptiva.
     * @param suscripcion La suscripción destino.
     * @param participante El usuario que paga.
     */
    public Participa(Double cantidadApagar, LocalDate fecha_pagado, MetodoPago metodo_pago, int periodos_cubiertos,
                     String descripcion, Suscripcion suscripcion, Usuario participante) {
        this.cantidadApagar = cantidadApagar;
        this.fecha_pagado = fecha_pagado;
        this.metodo_pago = metodo_pago;
        this.periodos_cubiertos = periodos_cubiertos;
        this.descripcion = descripcion;
        this.suscripcion = suscripcion;
        this.participante = participante;
    }

    /**
     * Obtiene la cantidad a pagar.
     * @return El monto del aporte (Double).
     */
    public Double getCantidadApagar() {
        return cantidadApagar;
    }

    /**
     * Establece la cantidad a pagar.
     * @param cantidadApagar El nuevo monto del aporte.
     */
    public void setCantidadApagar(Double cantidadApagar) {
        this.cantidadApagar = cantidadApagar;
    }

    /**
     * Obtiene la fecha del pago.
     * @return LocalDate con la fecha.
     */
    public LocalDate getFecha_pagado() {
        return fecha_pagado;
    }

    /**
     * Establece la fecha del pago.
     * @param fecha_pagado La fecha en que se realizó el aporte.
     */
    public void setFecha_pagado(LocalDate fecha_pagado) {
        this.fecha_pagado = fecha_pagado;
    }

    /**
     * Obtiene el método de pago.
     * @return Enum MetodoPago.
     */
    public MetodoPago getMetodo_pago() {
        return metodo_pago;
    }

    /**
     * Establece el método de pago.
     * @param metodo_pago El medio por el cual se hizo el pago.
     */
    public void setMetodo_pago(MetodoPago metodo_pago) {
        this.metodo_pago = metodo_pago;
    }

    /**
     * Obtiene los periodos cubiertos.
     * @return Número de ciclos (int).
     */
    public int getPeriodos_cubiertos() {
        return periodos_cubiertos;
    }

    /**
     * Establece los periodos cubiertos.
     * @param periodos_cubiertos Cantidad de ciclos que cubre este aporte.
     */
    public void setPeriodos_cubiertos(int periodos_cubiertos) {
        this.periodos_cubiertos = periodos_cubiertos;
    }

    /**
     * Obtiene la descripción.
     * @return Notas sobre el aporte.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripción.
     * @param descripcion Texto libre con detalles adicionales.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene la suscripción asociada.
     * @return Objeto Suscripcion.
     */
    public Suscripcion getSuscripcion() {
        return suscripcion;
    }

    /**
     * Asocia la suscripción a este aporte.
     * Fundamental para la integridad referencial.
     * @param suscripcion La suscripción compartida.
     */
    public void setSuscripcion(Suscripcion suscripcion) {
        this.suscripcion = suscripcion;
    }

    /**
     * Obtiene el usuario participante.
     * @return Objeto Usuario (el copagador).
     */
    public Usuario getParticipante() {
        return participante;
    }

    /**
     * Asocia el usuario participante a este aporte.
     * Fundamental para la integridad referencial.
     * @param participante El usuario que realiza el pago.
     */
    public void setParticipante(Usuario participante) {
        this.participante = participante;
    }

    /**
     * Compara dos objetos Participa para determinar si son iguales.
     * La igualdad se basa en la <strong>Clave Primaria Compuesta</strong>:
     * la combinación de {@code suscripcion} y {@code participante}.
     *
     * @param o Objeto a comparar.
     * @return true si ambos objetos refieren a la misma suscripción y usuario.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Participa participa = (Participa) o;
        return Objects.equals(suscripcion, participa.suscripcion) &&
                Objects.equals(participante, participa.participante);
    }

    /**
     * Genera un hash code basado en la clave compuesta.
     * @return int hash basado en suscripcion y participante.
     */
    @Override
    public int hashCode() {
        return Objects.hash(suscripcion, participante);
    }

    /**
     * Devuelve una representación en texto del aporte.
     *
     * @return String con los detalles del aporte y las entidades relacionadas.
     */
    @Override
    public String toString() {
        return "Participa{" +
                "aporte=" + cantidadApagar +
                ", fecha_pagado=" + fecha_pagado +
                ", metodo_pago=" + metodo_pago +
                ", periodos_cubiertos=" + periodos_cubiertos +
                ", descripcion='" + descripcion + '\'' +
                ", suscripcion=" + suscripcion +
                ", participante=" + participante +
                '}';
    }
}
