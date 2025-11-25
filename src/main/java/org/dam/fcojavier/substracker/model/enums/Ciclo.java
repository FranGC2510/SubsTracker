package org.dam.fcojavier.substracker.model.enums;

/**
 * Define la periodicidad o frecuencia de facturación de una suscripción.
 *
 * Este enum es fundamental para la lógica de negocio, ya que determina:
 * Cuándo se debe calcular la próxima fecha de renovación ({@code fechaRenovacion}).
 * Cómo se proyectan los gastos en los informes financieros (amortización del coste).
 *
 * @author Fco Javier García
 * @version 1.0
 */
public enum Ciclo {
    MENSUAL, TRIMESTRAL, ANUAL
}
