package org.dam.fcojavier.substracker.dao;

import org.dam.fcojavier.substracker.interfaces.CrudDao;
import org.dam.fcojavier.substracker.model.Suscripcion;
import org.dam.fcojavier.substracker.utils.enums.Ciclo;

import java.util.List;

public class SuscripcionDAO implements CrudDao<Suscripcion> {

    @Override
    public void create(Suscripcion suscripcion) {
        // implementación pendiente
    }

    @Override
    public Suscripcion findById(int id) {
        return null; // implementación pendiente
    }

    @Override
    public List<Suscripcion> findAll() {
        return null; // implementación pendiente
    }

    @Override
    public void update(Suscripcion suscripcion) {
        // implementación pendiente
    }

    @Override
    public void delete(int id) {
        // implementación pendiente
    }

    // Métodos específicos de Suscripcion

    public List<Suscripcion> findByCategoria(String categoria) {
        return null; // implementación pendiente
    }

    public List<Suscripcion> findByCiclo(Ciclo ciclo) {
        return null; // implementación pendiente
    }

    public List<Suscripcion> findByTitularId(int usuarioId) {
        return null; // implementación pendiente
    }
}
