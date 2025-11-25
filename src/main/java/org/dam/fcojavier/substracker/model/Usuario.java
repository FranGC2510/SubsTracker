package org.dam.fcojavier.substracker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa a un usuario registrado en la aplicación SubTracker.
 * Un usuario puede actuar dos roles simultáneamente:
 *
 * Titular: Dueño de suscripciones (gestionadas en {@code misSuscripciones}).
 * Copagador: Participante en suscripciones de otros (gestionadas en {@code misParticipantes}).
 *
 * @author Fco Javier García
 * @version 1.0
 */
public class Usuario {

    /**
     * Identificador único del usuario en la base de datos (Clave Primaria).
     */
    private int id_usuario;

    /**
     * Nombre de pila del usuario.
     */
    private String nombre;

    /**
     * Apellidos del usuario.
     */
    private String apellidos;

    /**
     * Correo electrónico. Se utiliza como identificador de acceso (Login) y debe ser único.
     */
    private String email;

    /**
     * Contraseña de acceso.
     */
    private String password;

    /**
     * Lista de suscripciones donde este usuario es el TITULAR (Dueño).
     * Relación 1:N.
     */
    private List<Suscripcion> misSuscripciones = new ArrayList<>();

    /**
     * Lista de participaciones donde este usuario es COPAGADOR.
     * Representa las suscripciones ajenas en las que este usuario colabora económicamente.
     */
    private List<Participa> misParticipaciones = new ArrayList<>();

    /**
     * Constructor vacío por defecto.
     * Inicializa las listas de relaciones como vacías para evitar {@code NullPointerException}.
     */
    public Usuario() {}

    /**
     * Constructor con datos básicos.
     * Utilizado para crear nuevos usuarios o recuperar datos planos de la BD.
     *
     * @param id_usuario Identificador único.
     * @param nombre Nombre.
     * @param apellidos Apellidos.
     * @param email Correo electrónico.
     * @param password Contraseña.
     */
    public Usuario(int id_usuario, String nombre, String apellidos, String email, String password) {
        this.id_usuario = id_usuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.password = password;
    }

    /**
     * Obtiene el ID del usuario.
     * @return El identificador entero.
     */
    public int getId_usuario() {
        return id_usuario;
    }

    /**
     * Establece el ID del usuario.
     * @param id_usuario Nuevo identificador.
     */
    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    /**
     * Obtiene el nombre.
     * @return String con el nombre.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre.
     * @param nombre Nuevo nombre.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene los apellidos.
     * @return String con los apellidos.
     */
    public String getApellidos() {
        return apellidos;
    }

    /**
     * Establece los apellidos.
     * @param apellidos Nuevos apellidos.
     */
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    /**
     * Obtiene el email.
     * @return String con el correo.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el email.
     * @param email Nuevo correo electrónico.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene la contraseña.
     * @return String con la contraseña.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contraseña.
     * @param password Nueva contraseña.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Obtiene las suscripciones propias del usuario.
     * @return Lista de objetos Suscripcion.
     */
    public List<Suscripcion> getMisSuscripciones() {
        return misSuscripciones;
    }

    /**
     * Asigna la lista de suscripciones propias.
     * @param misSuscripciones Lista de suscripciones donde es titular.
     */
    public void setMisSuscripciones(List<Suscripcion> misSuscripciones) {
        this.misSuscripciones = misSuscripciones;
    }

    /**
     * Obtiene las participaciones en suscripciones ajenas.
     * @return Lista de objetos Participa.
     */
    public List<Participa> getMisParticipaciones() {
        return misParticipaciones;
    }

    /**
     * Asigna la lista de participaciones.
     * @param misParticipaciones Lista de participaciones como copagador.
     */
    public void setMisParticipaciones(List<Participa> misParticipaciones) {
        this.misParticipaciones = misParticipaciones;
    }

    /**
     * Compara dos usuarios basándose en su ID único.
     *
     * @param o Objeto a comparar.
     * @return true si los IDs coinciden.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id_usuario == usuario.id_usuario;
    }

    /**
     * Genera un hash code basado en el ID del usuario.
     * @return int hash.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id_usuario);
    }

    /**
     * Representación en cadena del usuario y sus relaciones cargadas.
     * @return String con los detalles.
     */
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id_usuario +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", misSuscripciones=" + misSuscripciones +
                ", misParticipantes=" + misParticipaciones +
                '}';
    }
}
