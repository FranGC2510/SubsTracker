package org.dam.fcojavier.substracker.utils;

import java.time.LocalDate;

public class Validaciones {
    private static final String EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    /**
     * Verifica que un texto no sea nulo ni esté vacío.
     * @param texto El String a validar.
     * @return true si contiene texto válido.
     */
    public static boolean esTextoValido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    /**
     * Verifica el formato de un correo electrónico.
     * @param email El email a validar.
     * @return true si cumple con el patrón estándar.
     */
    public static boolean esEmailValido(String email) {
        if (!esTextoValido(email)) return false;
        return email.matches(EMAIL);
    }

    /**
     * Verifica que un número sea positivo (mayor que 0).
     * Útil para precios y cantidades.
     * @param numero El número a validar.
     * @return true si es mayor que 0.
     */
    public static boolean esPositivo(double numero) {
        return numero > 0;
    }

    /**
     * Verifica que una contraseña cumpla los requisitos mínimos.
     * Ej: Mínimo 6 caracteres.
     * @param password La contraseña.
     * @return true si es segura/válida.
     */
    public static boolean esPasswordValida(String password) {
        return esTextoValido(password) && password.length() >= 6;
    }

    /**
     * Verifica que la fecha de fin sea posterior a la de inicio.
     * @param inicio Fecha de activación.
     * @param fin Fecha de renovación.
     * @return true si las fechas son lógicas.
     */
    public static boolean sonFechasValidas(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) return false;
        return fin.isAfter(inicio) || fin.isEqual(inicio);
    }

    /**
     * Verifica que el número de periodos cubiertos sea lógico (ej. mayor o igual a 1).
     */
    public static boolean esPeriodoValido(int periodos) {
        return periodos >= 1;
    }
}
