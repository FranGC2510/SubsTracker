package org.dam.fcojavier.substracker.model.enums;

/**
 * Define los diferentes medios de pago admitidos en el sistema.
 * Se utiliza en dos contextos diferentes:
 * Cobros: Cómo paga el titular la suscripción al proveedor (ej. Tarjeta).
 * Participaciones: Cómo pagan los copagadores su parte al titular (ej. Bizum, Efectivo).
 *
 * @author Fco Javier García
 * @version 1.0
 */
public enum MetodoPago {
    TARJETA, TRANSFERENCIA, EFECTIVO, BIZUM, OTRO
}
