package org.dam.fcojavier.substracker.interfaces;

import java.util.List;

/**
 * Interfaz genérica para operaciones CRUD.
 * @param <T> Tipo de la entidad
 */
public interface CrudDao<T> {
    void create(T entity);

    T findById(int id);

    List<T> findAll();

    void update(T entity);

    void delete(int id);
}
