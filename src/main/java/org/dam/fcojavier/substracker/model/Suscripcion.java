package org.dam.fcojavier.substracker.model;

import org.dam.fcojavier.substracker.model.enums.Categoria;
import org.dam.fcojavier.substracker.model.enums.Ciclo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa una suscripción a un servicio recurrente (ej. Netflix, Spotify, Gimnasio).
 *
 * Es la entidad central del sistema. Una suscripción pertenece obligatoriamente a un
 * usuario titular (quien realiza el pago principal) y puede tener múltiples copagadores
 * (participantes) y un historial de cobros realizados.
 *
 * @author Fco Javier García
 * @version 1.0
 */
public class Suscripcion {

    /**
     * Identificador único de la suscripción en la base de datos.
     */
    private int idSuscripcion;

    /**
     * Nombre del servicio (ej. "Netflix 4K", "HBO Max").
     */
    private String nombre;

    /**
     * Precio total de la suscripción (antes de dividir gastos).
     */
    private Double precio;

    /**
     * Frecuencia con la que se realiza el cobro (MENSUAL, ANUAL, etc.).
     */
    private Ciclo ciclo;

    /**
     * Categoría a la que pertenece el servicio (OCIO, HOGAR, TRABAJO, etc.).
     */
    private Categoria categoria;

    /**
     * Indica si la suscripción está vigente (true) o cancelada/pausada (false).
     */
    private boolean activo;

    /**
     * Fecha en la que se contrató o inició el servicio.
     */
    private LocalDate fechaActivacion;

    /**
     * Fecha del próximo cobro previsto.
     */
    private LocalDate fechaRenovacion;

    /**
     * Usuario propietario de la suscripción.
     * Es el responsable legal del pago ante el proveedor del servicio.
     */
    private Usuario titular;

    /**
     * Historial de pagos realizados asociados a esta suscripción.
     * Relación 1:N (Una suscripción tiene muchos cobros).
     */
    private List<Cobro> cobros = new ArrayList<>();

    /**
     * Lista de usuarios que comparten el gasto de esta suscripción.
     * Relación N:M gestionada a través de la entidad {@link Participa}.
     */
    private List<Participa> participantes = new ArrayList<>();

    /**
     * Constructor vacío por defecto.
     * Inicializa las listas de cobros y participantes para evitar NullPointerException.
     */
    public Suscripcion() {}

    /**
     * Constructor estándar para crear una nueva suscripción.
     *
     * Por defecto, establece el estado {@code activo} en {@code true}.
     *
     * @param idSuscripcion Identificador (normalmente 0 si es nuevo).
     * @param nombre Nombre del servicio.
     * @param precio Coste total.
     * @param ciclo Frecuencia de pago.
     * @param categoria Tipo de servicio.
     * @param fechaActivacion Fecha de inicio.
     * @param fechaRenovacion Fecha del próximo pago.
     * @param Titular Usuario dueño de la suscripción.
     */
    public Suscripcion(int idSuscripcion, String nombre, Double precio, Ciclo ciclo, Categoria categoria,
                       LocalDate fechaActivacion, LocalDate fechaRenovacion, Usuario Titular) {
        this.idSuscripcion = idSuscripcion;
        this.nombre = nombre;
        this.precio = precio;
        this.ciclo = ciclo;
        this.categoria = categoria;
        this.activo = true; // Se activa por defecto al crearla
        this.fechaActivacion = fechaActivacion;
        this.fechaRenovacion = fechaRenovacion;
        this.titular = Titular;
    }

    /**
     * Constructor completo.
     * Útil cuando se recupera la información completa desde la base de datos,
     * incluyendo historiales y participantes.
     *
     * @param idSuscripcion Identificador único.
     * @param nombre Nombre del servicio.
     * @param precio Coste total.
     * @param ciclo Frecuencia de pago.
     * @param categoria Categoría del servicio.
     * @param fechaActivacion Fecha de inicio.
     * @param fechaRenovacion Fecha del próximo pago.
     * @param titular Usuario dueño.
     * @param cobros Historial de pagos ya cargado.
     * @param participantes Lista de copagadores ya cargada.
     */
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

    /**
     * Obtiene el ID de la suscripción.
     * @return Identificador entero.
     */
    public int getIdSuscripcion() {
        return idSuscripcion;
    }

    /**
     * Establece el ID de la suscripción.
     * @param idSuscripcion Nuevo identificador.
     */
    public void setIdSuscripcion(int idSuscripcion) {
        this.idSuscripcion = idSuscripcion;
    }

    /**
     * Obtiene el nombre del servicio.
     * @return String con el nombre.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del servicio.
     * @param nombre Nuevo nombre.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el precio total.
     * @return Precio en formato Double.
     */
    public Double getPrecio() {
        return precio;
    }

    /**
     * Establece el precio total.
     * @param precio Nuevo coste.
     */
    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    /**
     * Obtiene el ciclo de facturación.
     * @return Enum Ciclo.
     */
    public Ciclo getCiclo() {
        return ciclo;
    }

    /**
     * Establece el ciclo de facturación.
     * @param ciclo Nuevo ciclo (MENSUAL, ANUAL, etc.).
     */
    public void setCiclo(Ciclo ciclo) {
        this.ciclo = ciclo;
    }

    /**
     * Obtiene la categoría del servicio.
     * @return Enum Categoria.
     */
    public Categoria getCategoria() {
        return categoria;
    }

    /**
     * Establece la categoría.
     * @param categoria Nueva categoría.
     */
    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    /**
     * Verifica si la suscripción está activa.
     * @return true si está activa, false si está cancelada.
     */
    public boolean isActivo() {
        return activo;
    }

    /**
     * Cambia el estado de actividad de la suscripción.
     * @param activo true para activar, false para desactivar.
     */
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /**
     * Obtiene la fecha de inicio del contrato.
     * @return LocalDate de activación.
     */
    public LocalDate getFechaActivacion() {
        return fechaActivacion;
    }

    /**
     * Establece la fecha de inicio.
     * @param fechaActivacion Nueva fecha de activación.
     */
    public void setFechaActivacion(LocalDate fechaActivacion) {
        this.fechaActivacion = fechaActivacion;
    }

    /**
     * Obtiene la próxima fecha de cobro.
     * @return LocalDate de renovación.
     */
    public LocalDate getFechaRenovacion() {
        return fechaRenovacion;
    }

    /**
     * Establece la próxima fecha de cobro.
     * @param fechaRenovacion Nueva fecha de renovación.
     */
    public void setFechaRenovacion(LocalDate fechaRenovacion) {
        this.fechaRenovacion = fechaRenovacion;
    }

    /**
     * Obtiene el usuario titular.
     * @return Objeto Usuario propietario.
     */
    public Usuario getTitular() {
        return titular;
    }

    /**
     * Asigna un titular a la suscripción.
     * @param titular Usuario responsable del pago.
     */
    public void setTitular(Usuario titular) {
        this.titular = titular;
    }

    /**
     * Obtiene la lista histórica de cobros.
     * @return Lista de objetos Cobro.
     */
    public List<Cobro> getCobros() {
        return cobros;
    }

    /**
     * Asigna una lista completa de cobros.
     * @param cobros Lista de cobros.
     */
    public void setCobros(List<Cobro> cobros) {
        this.cobros = cobros;
    }

    /**
     * Obtiene la lista de participantes (copagadores).
     * @return Lista de objetos Participa.
     */
    public List<Participa> getParticipantes() {
        return participantes;
    }

    /**
     * Asigna la lista de participantes.
     * @param participantes Lista de objetos Participa.
     */
    public void setParticipantes(List<Participa> participantes) {
        this.participantes = participantes;
    }

    /**
     * Calcula el dinero total gastado teóricamente desde la fecha de activación hasta hoy.
     * Asume que los pagos se realizan por adelantado al inicio de cada ciclo.
     */
    public double calcularGastoTotal(LocalDate fechaHasta) {
        LocalDate hoy = LocalDate.now();

        // Validación: Si no hay fecha, no podemos calcular
        if (fechaHasta == null || fechaActivacion == null) return 0.0;

        if (fechaActivacion.isAfter(fechaHasta)) {
            return 0.0;
        }

        long periodosTranscurridos = 0;

        switch (ciclo) {
            case MENSUAL:
                periodosTranscurridos = ChronoUnit.MONTHS.between(fechaActivacion, fechaHasta);
                break;
            case TRIMESTRAL:
                periodosTranscurridos = ChronoUnit.MONTHS.between(fechaActivacion, fechaHasta) / 3;
                break;
            case ANUAL:
                periodosTranscurridos = ChronoUnit.YEARS.between(fechaActivacion, fechaHasta);
                break;
        }

        // Sumamos 1 porque el primer pago se hace el día de la activación (pago por adelantado)
        long totalPagos = periodosTranscurridos + 1;

        return totalPagos * precio;
    }

    /**
     * Compara suscripciones basándose en su ID.
     * @param o Objeto a comparar.
     * @return true si los IDs coinciden.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Suscripcion that = (Suscripcion) o;
        return idSuscripcion == that.idSuscripcion;
    }

    /**
     * Genera hash code basado en el ID.
     * @return Hash entero.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(idSuscripcion);
    }

    /**
     * Representación en cadena de la suscripción.
     * Muestra información básica y las listas asociadas.
     * @return String con los detalles.
     */
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
