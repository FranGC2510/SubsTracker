package org.dam.fcojavier.substracker.dao;

import org.dam.fcojavier.substracker.utils.connection.ConnectionDB;
import org.dam.fcojavier.substracker.interfaces.CrudDao;
import org.dam.fcojavier.substracker.model.Participa;
import org.dam.fcojavier.substracker.model.Suscripcion;
import org.dam.fcojavier.substracker.model.Usuario;
import org.dam.fcojavier.substracker.model.enums.Categoria;
import org.dam.fcojavier.substracker.model.enums.Ciclo;
import org.dam.fcojavier.substracker.model.enums.MetodoPago;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la entidad asociativa {@link Participa}.
 *
 * Gestiona los aportes económicos ("copagos") realizados a una suscripción.
 * Esta entidad ha evolucionado para soportar dos tipos de colaboradores:
 * Usuarios Registrados: Tienen cuenta en la app (relación con {@code id_usuario}).
 * Invitados: Personas externas (solo nombre) añadidas manualmente por el titular.
 *
 * Características Técnicas:
 * Uso de LEFT JOIN en consultas SQL para recuperar datos incluso si el usuario es NULL (invitado).
 * Gestión de campos nulos ({@code setNull}) para fechas y claves foráneas opcionales.
 * Identificación mediante clave primaria simple {@code id_participa}.
 *
 * @author Fco Javier García
 * @version 2.0 (Soporte para Invitados)
 */
public class ParticipaDAO implements CrudDao<Participa> {
    private final String create_sql="INSERT INTO participa (id_suscripcion, id_usuario, nombre_invitado, cantidadApagar, fecha_pagado, metodo_pago, descripcion, periodos_cubiertos) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private final String update_sql = "UPDATE participa SET nombre_invitado=?, cantidadApagar=?, fecha_pagado=?, metodo_pago=?, descripcion=?, periodos_cubiertos=? WHERE id_participa=?";
    private final String delete_sql = "DELETE FROM participa WHERE id_participa=?";
    private final String SELECT_BASE =
            "SELECT p.*, " +
                    "u.id_usuario, u.email, u.nombre AS u_nombre, u.apellidos, " +
                    "s.id_suscripcion, s.nombre AS s_nombre, s.precio, s.ciclo, s.categoria " +
                    "FROM participa p " +
                    "LEFT JOIN usuario u ON p.id_usuario = u.id_usuario " +
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
            pstm.setInt(1, participa.getSuscripcion().getIdSuscripcion());

            if (participa.getParticipante() != null) {
                pstm.setInt(2, participa.getParticipante().getId_usuario());
                pstm.setNull(3, Types.VARCHAR);
            } else {
                pstm.setNull(2, Types.INTEGER);
                pstm.setString(3, participa.getNombreInvitado());
            }

            pstm.setDouble(4, participa.getCantidadApagar());

            if (participa.getFecha_pagado() != null)
                pstm.setDate(5, Date.valueOf(participa.getFecha_pagado()));
            else
                pstm.setNull(5, Types.DATE);

            pstm.setString(6, participa.getMetodo_pago().name());
            pstm.setString(7, participa.getDescripcion());
            pstm.setInt(8, participa.getPeriodos_cubiertos());

            return pstm.executeUpdate() > 0;
        }catch (SQLException e){
            System.out.println("Error creando participa: " + e.getMessage());
            return false;
        }
    }

    /**
     * Método no implementado.
     *
     * La búsqueda por ID individual no suele utilizarse en la lógica de negocio actual,
     * ya que los accesos se realizan principalmente por suscripción.
     *
     * @param id ID del registro.
     * @throws UnsupportedOperationException Siempre.
     */
    @Override
    public Participa findById(int id) {
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
     * Actualiza los datos de un colaborador existente.
     *
     * Permite modificar el nombre (si es invitado), el importe, el estado del pago (fecha)
     * y el número de periodos cubiertos. Utiliza la clave primaria {@code id_participa}.
     *
     * @param participa Objeto con los datos modificados.
     * @return {@code true} si la actualización fue exitosa.
     */
    @Override
    public boolean update(Participa participa) {
        try (PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(update_sql)) {

            pstm.setString(1, participa.getNombreInvitado());
            pstm.setDouble(2, participa.getCantidadApagar());

            if (participa.getFecha_pagado() != null)
                pstm.setDate(3, Date.valueOf(participa.getFecha_pagado()));
            else
                pstm.setNull(3, Types.DATE);

            pstm.setString(4, participa.getMetodo_pago().name());
            pstm.setString(5, participa.getDescripcion());
            pstm.setInt(6, participa.getPeriodos_cubiertos());
            pstm.setInt(7, participa.getIdParticipa());

            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un registro de participación por su ID único.
     *
     * @param idParticipa Identificador único del registro a borrar.
     * @return {@code true} si se eliminó correctamente.
     */
    @Override
    public boolean delete(int idParticipa) {
        try (PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(delete_sql)) {
            pstm.setInt(1, idParticipa);
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
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
        p.setIdParticipa(rs.getInt("id_participa")); // Nuevo ID
        p.setCantidadApagar(rs.getDouble("cantidadApagar"));

        p.setFecha_pagado(parsearFechaSegura(rs.getString("fecha_pagado")));

        p.setMetodo_pago(MetodoPago.valueOf(rs.getString("metodo_pago")));
        p.setPeriodos_cubiertos(rs.getInt("periodos_cubiertos"));
        p.setNombreInvitado(rs.getString("nombre_invitado"));

        // Cargar usuario y suscripción completos
        int idUsuario = rs.getInt("id_usuario");
        if (!rs.wasNull() && idUsuario > 0) {
            Usuario u = new Usuario();
            u.setId_usuario(idUsuario);
            u.setNombre(rs.getString("u_nombre"));
            u.setEmail(rs.getString("email"));
            p.setParticipante(u);
        }

        Suscripcion s = new Suscripcion();
        s.setIdSuscripcion(rs.getInt("id_suscripcion"));
        s.setNombre(rs.getString("s_nombre"));   // ¡Ojo al alias!
        s.setPrecio(rs.getDouble("precio"));
        s.setCiclo(Ciclo.valueOf(rs.getString("ciclo")));
        s.setCategoria(Categoria.valueOf(rs.getString("categoria")));

        p.setSuscripcion(s);

        return p;
    }

    /**
     * Método auxiliar para convertir fechas de SQLite/MySQL de forma robusta.
     * Soporta tanto formato ISO (yyyy-MM-dd) como Timestamp (milisegundos).
     */
    private LocalDate parsearFechaSegura(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty()) return null;

        try {
            //MySQL
            return LocalDate.parse(fechaStr);
        } catch (Exception e) {
            try {
                // SQLite JDBC
                long millis = Long.parseLong(fechaStr);
                return java.time.Instant.ofEpochMilli(millis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
            } catch (Exception ex) {
                System.err.println("Error fecha irrecuperable: " + fechaStr);
                return null;
            }
        }
    }
}
