package org.dam.fcojavier.substracker.dao;

import org.dam.fcojavier.substracker.utils.connection.ConnectionDB;
import org.dam.fcojavier.substracker.interfaces.CrudDao;
import org.dam.fcojavier.substracker.model.Participa;
import org.dam.fcojavier.substracker.model.Suscripcion;
import org.dam.fcojavier.substracker.model.Usuario;
import org.dam.fcojavier.substracker.model.enums.Categoria;
import org.dam.fcojavier.substracker.model.enums.Ciclo;
import org.dam.fcojavier.substracker.model.enums.MetodoPago;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la entidad asociativa {@link Participa}.
 *
 * Gestiona la relación N:M (Muchos a Muchos) entre {@link Usuario} y {@link Suscripcion}.
 * Esta tabla almacena a los "Copagadores" (usuarios que pagan una parte de una suscripción ajena).
 *
 * Particularidades:
 * Clave Primaria Compuesta: La identidad se define por la pareja ({@code id_usuario}, {@code id_suscripcion}).
 * Por este motivo, los métodos estándar {@code findById(int)} y {@code delete(int)} no son soportados.
 * Optimización SQL: Las consultas de lectura utilizan {@code INNER JOIN} para reconstruir
 * los objetos {@code Usuario} y {@code Suscripcion} en una sola consulta a la base de datos.
 *
 * @author Fco Javier García
 * @version 1.0
 */
public class ParticipaDAO implements CrudDao<Participa> {
    private final String create_sql="INSERT INTO participa (id_usuario, id_suscripcion, cantidadApagar, fecha_pagado, metodo_pago, descripcion, periodos_cubiertos) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private final String update_sql="UPDATE participa SET cantidadApagar = ?, fecha_pagado = ?, metodo_pago = ?, descripcion = ?, periodos_cubiertos = ? WHERE id_usuario=? AND id_suscripcion=?";
    private final String delete_sql="DELETE FROM participa WHERE id_usuario=? AND id_suscripcion=?";
    private final String SELECT_BASE =
            "SELECT p.*, " +
                    "u.id_usuario, u.email, u.nombre AS u_nombre, u.apellidos, " +
                    "s.id_suscripcion, s.nombre AS s_nombre, s.precio, s.ciclo, s.categoria " +
                    "FROM participa p " +
                    "INNER JOIN usuario u ON p.id_usuario = u.id_usuario " +
                    "INNER JOIN suscripcion s ON p.id_suscripcion = s.id_suscripcion ";

    private final String find_all_sql = SELECT_BASE;
    private final String find_by_id_suscripcion_sql = SELECT_BASE + "WHERE p.id_suscripcion = ?";
    private final String find_by_id_usuario_sql     = SELECT_BASE + "WHERE p.id_usuario = ?";

    /**
     * Registra una nueva participación (un usuario copagando una suscripción).
     *
     * @param participa Objeto con los datos de la relación (usuario, suscripción, monto, etc.).
     * @return {@code true} si se registró correctamente, {@code false} si hubo error (ej. duplicado).
     */
    @Override
    public boolean create(Participa participa) {
        try(PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(create_sql)){
            pstm.setInt(1, participa.getParticipante().getId_usuario());
            pstm.setInt(2, participa.getSuscripcion().getIdSuscripcion());
            pstm.setDouble(3, participa.getCantidadApagar());
            pstm.setDate(4, Date.valueOf(participa.getFecha_pagado()));
            pstm.setString(5, participa.getMetodo_pago().name());
            pstm.setString(6, participa.getDescripcion());
            pstm.setInt(7, participa.getPeriodos_cubiertos());

            return pstm.executeUpdate() > 0;
        }catch (SQLException e){
            System.out.println("Error creando participa: " + e.getMessage());
            return false;
        }
    }

    /**
     * Operación NO SOPORTADA.
     *
     * La entidad {@code Participa} tiene una clave primaria compuesta, por lo que no se puede
     * buscar por un único entero ID.
     *
     * @param id ID simple (ignorado).
     * @throws UnsupportedOperationException Siempre, indicando el uso incorrecto.
     */
    @Override
    public Participa findById(int id) {
        // Como PARTICIPA tiene PK compuesta, este método no se suele usar.
        // Podemos crear un findByUsuarioYSuscripcion
        throw new UnsupportedOperationException("Usar búsqueda compuesta");
    }

    /**
     * Recupera todas las participaciones registradas en el sistema.
     *
     * Útil para informes globales de administración.
     *
     * @return Lista de todas las relaciones de copago.
     */
    @Override
    public List<Participa> findAll() {
        List<Participa> participaList = new ArrayList<>();
        try(PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(find_all_sql)){
            ResultSet rs = pstm.executeQuery();
            while(rs.next()){
                Participa participa = obtenerParticipa(rs);
                participaList.add(participa);
            }
        }catch (SQLException e){
            System.out.println("Error obteniendo participa: " + e.getMessage());
        }
        return participaList;
    }

    /**
     * Actualiza los datos de un copago existente.
     *
     * Permite modificar el monto, fecha o descripción. La identificación de la fila
     * se hace mediante {@code id_usuario} y {@code id_suscripcion} contenidos en el objeto.
     *
     * @param participa Objeto con los datos actualizados.
     * @return {@code true} si la actualización fue exitosa.
     */
    @Override
    public boolean update(Participa participa) {
        try(PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(update_sql)){
            pstm.setDouble(1, participa.getCantidadApagar());
            pstm.setDate(2, Date.valueOf(participa.getFecha_pagado()));
            pstm.setString(3, participa.getMetodo_pago().name());
            pstm.setString(4, participa.getDescripcion());
            pstm.setInt(5, participa.getPeriodos_cubiertos());
            pstm.setInt(6, participa.getParticipante().getId_usuario());
            pstm.setInt(7, participa.getSuscripcion().getIdSuscripcion());

            return pstm.executeUpdate() > 0;
        }catch (SQLException e){
            System.out.println("Error actualizando participa: " + e.getMessage());
            return false;
        }
    }

    /**
     * Operación NO SOPORTADA.
     *
     * Para eliminar una participación se requiere identificar al usuario y a la suscripción.
     * Use {@link #delete(int, int)} en su lugar.
     *
     * @param id ID simple.
     * @throws UnsupportedOperationException Siempre.
     */
    @Override
    public boolean delete(int id) {
        throw new UnsupportedOperationException("Usar delete(idUsuario, idSuscripcion)");
    }

    /**
     * Elimina una participación específica (Dejar de ser copagador).
     *
     * @param idUsuario ID del usuario que deja de pagar.
     * @param idSuscripcion ID de la suscripción afectada.
     * @return {@code true} si se eliminó el registro correctamente.
     */
    public boolean delete(int idUsuario, int idSuscripcion){
        try(PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(delete_sql)){
            pstm.setInt(1, idUsuario);
            pstm.setInt(2, idSuscripcion);
            return pstm.executeUpdate() > 0;
        }catch (SQLException e){
            System.out.println("Error eliminando participa: " + e.getMessage());
            return false;
        }
    }

    // Métodos específicos de Participa

    /**
     * Obtiene la lista de todos los copagadores de una suscripción concreta.
     *
     * @param suscripcionId ID de la suscripción.
     * @return Lista de copagadores con sus aportes detallados.
     */
    public List<Participa> findBySuscripcionId(int suscripcionId) {
        List<Participa> participaList = new ArrayList<>();
        try(PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(find_by_id_suscripcion_sql)){
            pstm.setInt(1, suscripcionId);
            ResultSet rs = pstm.executeQuery();
            while(rs.next()) {
                Participa participa = obtenerParticipa(rs);
                participaList.add(participa);
            }
        }catch (SQLException e){
            System.out.println("Error obteniendo participa: " + e.getMessage());
        }
        return participaList;
    }

    /**
     * Obtiene la lista de suscripciones ajenas en las que participa un usuario.
     *
     * @param usuarioId ID del usuario copagador.
     * @return Lista de participaciones.
     */
    public List<Participa> findByUsuarioId(int usuarioId) {
        List<Participa> participaList = new ArrayList<>();
        try(PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(find_by_id_usuario_sql)){
            pstm.setInt(1, usuarioId);
            ResultSet rs = pstm.executeQuery();
            while(rs.next()) {
                Participa participa = obtenerParticipa(rs);
                participaList.add(participa);
            }
        }catch (SQLException e){
            System.out.println("Error obteniendo participa: " + e.getMessage());
        }
        return participaList;
    }

    //Métodos privados auxiliares

    /**
     * Mapea un ResultSet a un objeto Participa, construyendo también el Usuario y la Suscripción.
     *
     * @param rs ResultSet posicionado.
     * @return Objeto Participa completo.
     * @throws SQLException Si ocurre un error de lectura.
     */
    private Participa obtenerParticipa(ResultSet rs) throws SQLException{
        Participa p = new Participa();
        p.setCantidadApagar(rs.getDouble("cantidadApagar"));
        p.setFecha_pagado(rs.getDate("fecha_pagado").toLocalDate());
        p.setMetodo_pago(MetodoPago.valueOf(rs.getString("metodo_pago")));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPeriodos_cubiertos(rs.getInt("periodos_cubiertos"));

        // Cargar usuario y suscripción completos
        Usuario u = new Usuario();
        u.setId_usuario(rs.getInt("id_usuario"));
        u.setEmail(rs.getString("email"));
        u.setNombre(rs.getString("u_nombre"));   // ¡Ojo al alias!
        u.setApellidos(rs.getString("apellidos"));
        // No traemos el password por seguridad y eficiencia
        p.setParticipante(u);

        Suscripcion s = new Suscripcion();
        s.setIdSuscripcion(rs.getInt("id_suscripcion"));
        s.setNombre(rs.getString("s_nombre"));   // ¡Ojo al alias!
        s.setPrecio(rs.getDouble("precio"));
        s.setCiclo(Ciclo.valueOf(rs.getString("ciclo")));
        s.setCategoria(Categoria.valueOf(rs.getString("categoria")));

        p.setSuscripcion(s);

        return p;
    }
}
