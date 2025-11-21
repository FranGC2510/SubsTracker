package org.dam.fcojavier.substracker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Usuario {
    private int id;
    private String nombre;
    private String apellidos;
    private String email;
    private String password;
    private List<Suscripcion> misSuscripciones = new ArrayList<>();
    private List<Participa> misParticipantes = new ArrayList<>();

    public Usuario() {}

    public Usuario(int id, String nombre, String apellidos, String email, String password) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Suscripcion> getMisSuscripciones() {
        return misSuscripciones;
    }

    public void setMisSuscripciones(List<Suscripcion> misSuscripciones) {
        this.misSuscripciones = misSuscripciones;
    }

    public List<Participa> getMisParticipantes() {
        return misParticipantes;
    }

    public void setMisParticipantes(List<Participa> misParticipantes) {
        this.misParticipantes = misParticipantes;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id == usuario.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", misSuscripciones=" + misSuscripciones +
                ", misParticipantes=" + misParticipantes +
                '}';
    }
}
