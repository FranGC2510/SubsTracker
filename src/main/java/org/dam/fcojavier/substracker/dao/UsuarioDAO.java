package org.dam.fcojavier.substracker.dao;

import org.dam.fcojavier.substracker.interfaces.CrudDao;
import org.dam.fcojavier.substracker.model.Usuario;

import java.util.List;

public class UsuarioDAO implements CrudDao<Usuario> {

    @Override
    public void create(Usuario usuario) {
        // implementación pendiente
    }

    @Override
    public Usuario findById(int id) {
        return null; // implementación pendiente
    }

    @Override
    public List<Usuario> findAll() {
        return null; // implementación pendiente
    }

    @Override
    public void update(Usuario usuario) {
        // implementación pendiente
    }

    @Override
    public void delete(int id) {
        // implementación pendiente
    }

    // Métodos específicos de Usuario

    public Usuario findByEmail(String email) {
        return null; // implementación pendiente
    }
}
