package org.dam.fcojavier.substracker.dao;

import org.dam.fcojavier.substracker.interfaces.CrudDao;
import org.dam.fcojavier.substracker.model.Cobro;

import java.time.LocalDate;
import java.util.List;

public class CobroDAO implements CrudDao<Cobro> {

    @Override
    public void create(Cobro cobro) {
        // implementación pendiente
    }

    @Override
    public Cobro findById(int id) {
        return null; // implementación pendiente
    }

    @Override
    public List<Cobro> findAll() {
        return null; // implementación pendiente
    }

    @Override
    public void update(Cobro cobro) {
        // implementación pendiente
    }

    @Override
    public void delete(int id) {
        // implementación pendiente
    }

    // Métodos específicos de Cobro

    public List<Cobro> findBySuscripcionId(int suscripcionId) {
        return null; // implementación pendiente
    }

    public List<Cobro> findByUsuarioId(int usuarioId) {
        return null; // implementación pendiente
    }

    public List<Cobro> findByFechas(LocalDate desde, LocalDate hasta) {
        return null; // implementación pendiente
    }
}
