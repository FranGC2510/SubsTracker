package org.dam.fcojavier.substracker.dao;

import org.dam.fcojavier.substracker.utils.PasswordUtilidades;
import org.dam.fcojavier.substracker.utils.connection.ConnectionDB;
import org.dam.fcojavier.substracker.interfaces.CrudDao;
import org.dam.fcojavier.substracker.model.Participa;
import org.dam.fcojavier.substracker.model.Suscripcion;
import org.dam.fcojavier.substracker.model.Usuario;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la entidad {@link Usuario}.
 *
 * Implementa las operaciones CRUD básicas (Crear, Leer, Actualizar, Borrar) contra la tabla 'usuario'
 * de la base de datos.
 *
 * Características especiales:
 * Gestiona la recuperación de claves autogeneradas (ID) al crear usuarios.
 * Implementa una estrategia de carga mixta:
 *
 * {@code findAll()}: Carga ligera (solo datos del usuario).
 * {@code findById()} y {@code findByEmail()}: Carga completa (incluye listas de suscripciones y participaciones).
 *
 * @author Fco Javier García
 * @version 1.0
 */
public class UsuarioDAO implements CrudDao<Usuario> {

    private final String create_sql = "INSERT INTO usuario (nombre, apellidos, email, password) VALUES (?, ?, ?, ?)";
    private final String update_sql = "UPDATE usuario SET nombre = ?, apellidos = ?, email = ?, password = ? WHERE id_usuario = ?";
    private final String delete_sql = "DELETE FROM usuario WHERE id_usuario = ?";
    private final String find_all_sql = "SELECT * FROM usuario";
    private final String find_by_id_sql = "SELECT * FROM usuario WHERE id_usuario = ?";
    private final String find_by_email_sql = "SELECT * FROM usuario WHERE email = ?";

    /**
     * Inserta un nuevo usuario en la base de datos.
     *
     * Si la inserción es exitosa, actualiza el objeto {@code usuario} pasado por parámetro
     * asignándole el ID generado automáticamente por la base de datos.
     *
     * @param usuario Objeto con los datos del nuevo usuario (nombre, email, pass, etc.).
     * @return {@code true} si se guardó correctamente, {@code false} si hubo error (ej. email duplicado).
     */
    @Override
    public boolean create(Usuario usuario) {
        try (PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(create_sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstm.setString(1, usuario.getNombre());
            pstm.setString(2, usuario.getApellidos());
            pstm.setString(3, usuario.getEmail());
            pstm.setString(4, usuario.getPassword());

            if (pstm.executeUpdate() > 0) {
                ResultSet rs = pstm.getGeneratedKeys();
                if (rs.next()) {
                    usuario.setId_usuario(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error creando usuario: " + e.getMessage());
        }
        return false;
    }

    /**
     * Busca un usuario por su Clave Primaria (ID) y carga TODA su información relacionada.
     *
     * Este método realiza una carga completa: además de los datos del perfil,
     * recupera las listas de {@code misSuscripciones} y {@code misParticipantes}.
     *
     * @param id Identificador único del usuario.
     * @return El objeto {@link Usuario} completo, o {@code null} si no existe.
     */
    @Override
    public Usuario findById(int id) {
        Usuario usuario = null;

        try (PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(find_by_id_sql)) {
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                usuario = obtenerUsuario(rs);

                usuario.setMisSuscripciones(findSuscripcionesDeTitular(usuario.getId_usuario()));
                usuario.setMisParticipaciones(findParticipacionesDeUsuario(usuario.getId_usuario()));
            }
        } catch (SQLException e) {
            System.out.println("Error buscando usuario: " + e.getMessage());
        }
        return usuario;
    }

    /**
     * Recupera todos los usuarios registrados en el sistema.
     *
     * Nota de rendimiento: Este método realiza una carga ligera.
     * Devuelve los usuarios con sus datos básicos (nombre, email...) pero
     * NO carga las listas de suscripciones ni participaciones para evitar saturar la memoria.
     *
     * @return Lista de usuarios (puede estar vacía).
     */
    @Override
    public List<Usuario> findAll() {
        List<Usuario> usuarios = new ArrayList<>();
        try (PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(find_all_sql)) {
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                usuarios.add(obtenerUsuario(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo usuarios: " + e.getMessage());
        }
        return usuarios;
    }

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param usuario Objeto con los datos modificados. Debe tener un {@code id_usuario} válido.
     * @return {@code true} si se actualizó correctamente.
     */
    @Override
    public boolean update(Usuario usuario) {
        try (PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(update_sql)) {
            pstm.setString(1, usuario.getNombre());
            pstm.setString(2, usuario.getApellidos());
            pstm.setString(3, usuario.getEmail());
            pstm.setString(4, usuario.getPassword());
            pstm.setInt(5, usuario.getId_usuario());

            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error actualizando usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un usuario de la base de datos.
     *
     * Restricción: Si el usuario tiene suscripciones activas o participaciones,
     * la base de datos podría impedir el borrado por integridad referencial (Foreign Keys),
     * lanzando una excepción.
     *
     * @param id ID del usuario a eliminar.
     * @return {@code true} si se eliminó, {@code false} si hubo error o restricción.
     */
    @Override
    public boolean delete(int id) {
        try (PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(delete_sql)) {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error eliminando usuario: " + e.getMessage());
            return false;
        }
    }

    // Métodos específicos de Usuario

    /**
     * Busca un usuario por su correo electrónico.
     * Método esencial para el proceso de Login.
     * Al igual que {@code findById}, realiza una carga completa de las relaciones del usuario.
     *
     * @param email Correo a buscar (debe ser coincidencia exacta).
     * @return Objeto {@link Usuario} completo o {@code null} si no existe.
     */
    public Usuario findByEmail(String email) {
        Usuario usuario = null;
        try (PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(find_by_email_sql)) {
            pstm.setString(1, email);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                usuario = obtenerUsuario(rs);

                usuario.setMisSuscripciones(findSuscripcionesDeTitular(usuario.getId_usuario()));
                usuario.setMisParticipaciones(findParticipacionesDeUsuario(usuario.getId_usuario()));
            }
        } catch (SQLException e) {
            System.out.println("Error buscando usuario por email: " + e.getMessage());
        }
        return usuario;
    }

    // Métodos privados auxiliares

    /**
     * Convierte una fila del ResultSet en un objeto Usuario básico.
     *
     * @param rs ResultSet posicionado en la fila actual.
     * @return Usuario con datos básicos (sin listas cargadas).
     * @throws SQLException Si hay error al leer columnas.
     */
    private Usuario obtenerUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId_usuario(rs.getInt("id_usuario"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellidos(rs.getString("apellidos"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPassword(rs.getString("password"));

        // Inicializamos listas vacías para evitar NullPointerException
        usuario.setMisSuscripciones(new ArrayList<>());
        usuario.setMisParticipaciones(new ArrayList<>());

        return usuario;
    }

    /**
     * Delega en SuscripcionDAO la búsqueda de suscripciones donde el usuario es titular.
     */
    private List<Suscripcion> findSuscripcionesDeTitular(int idUsuario) {
        return new SuscripcionDAO().findByTitularId(idUsuario);
    }

    /**
     * Delega en ParticipaDAO la búsqueda de participaciones donde el usuario es copagador.
     */
    private List<Participa> findParticipacionesDeUsuario(int idUsuario) {
        return new ParticipaDAO().findByUsuarioId(idUsuario);
    }
}
