package org.dam.fcojavier.substracker.interfaces;

import java.util.List;

/**
 * Interfaz genérica para operaciones CRUD.
 * @param <T> Tipo de la entidad
 */
public interface CrudDao<T> {
    boolean create(T entity);

    T findById(int id);

    List<T> findAll();

    boolean update(T entity);

    boolean delete(int id);
}
