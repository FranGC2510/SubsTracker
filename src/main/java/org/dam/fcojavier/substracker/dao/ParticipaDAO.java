package org.dam.fcojavier.substracker.dao;

import org.dam.fcojavier.substracker.interfaces.CrudDao;
import org.dam.fcojavier.substracker.model.Participa;

import java.util.List;

public class ParticipaDAO implements CrudDao<Participa> {

    @Override
    public void create(Participa participa) {
        // implementación pendiente
    }

    @Override
    public Participa findById(int id) {
        return null; // implementación pendiente
    }

    @Override
    public List<Participa> findAll() {
        return null; // implementación pendiente
    }

    @Override
    public void update(Participa participa) {
        // implementación pendiente
    }

    @Override
    public void delete(int id) {
        // implementación pendiente
    }

    // Métodos específicos de Participa

    public List<Participa> findBySuscripcionId(int suscripcionId) {
        return null; // implementación pendiente
    }

    public List<Participa> findByUsuarioId(int usuarioId) {
        return null; // implementación pendiente
    }
}
