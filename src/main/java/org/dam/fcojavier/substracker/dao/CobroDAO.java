package org.dam.fcojavier.substracker.dao;

import org.dam.fcojavier.substracker.utils.connection.ConnectionDB;
import org.dam.fcojavier.substracker.interfaces.CrudDao;
import org.dam.fcojavier.substracker.model.Cobro;
import org.dam.fcojavier.substracker.model.Suscripcion;
import org.dam.fcojavier.substracker.model.enums.Categoria;
import org.dam.fcojavier.substracker.model.enums.Ciclo;
import org.dam.fcojavier.substracker.model.enums.MetodoPago;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la entidad {@link Cobro}.
 *
 * Gestiona el historial de pagos realizados por los titulares de las suscripciones.
 *
 * Características de Diseño:
 *
 * Consultas con JOIN: Todas las lecturas unen la tabla {@code cobro} con {@code suscripcion}.
 * Esto permite filtrar cobros por datos de la suscripción (como el titular) y mostrar el nombre del servicio
 * sin necesidad de consultas adicionales.
 * Reportes Financieros: Incluye métodos específicos para filtrar por rangos de fechas,
 * esenciales para la generación de informes mensuales o anuales.
 *
 * @author Fco Javier García
 * @version 1.0
 */
public class CobroDAO implements CrudDao<Cobro> {
    private final String create_sql = "INSERT INTO cobro (id_suscripcion, fecha_cobro, metodo_pago, descripcion, periodos_cubiertos) VALUES (?, ?, ?, ?, ?)";
    private final String update_sql = "UPDATE cobro SET id_suscripcion = ?, fecha_cobro = ?, metodo_pago = ?, descripcion = ?, periodos_cubiertos = ? WHERE id_cobro = ?";
    private final String delete_sql = "DELETE FROM cobro WHERE id_cobro = ?";
    private final String SELECT_BASE =
            "SELECT c.*, " +
                    "s.id_suscripcion, s.nombre, s.precio, s.ciclo, s.categoria, s.id_titular " +
                    "FROM cobro c " +
                    "INNER JOIN suscripcion s ON c.id_suscripcion = s.id_suscripcion ";

    private final String find_all_sql = SELECT_BASE;
    private final String find_by_id_sql = SELECT_BASE + "WHERE c.id_cobro = ?";
    private final String find_by_suscripcion_id_sql = SELECT_BASE + "WHERE c.id_suscripcion = ?";
    private final String find_by_usuario_id_sql = SELECT_BASE + "WHERE s.id_titular = ?";
    private final String find_by_fechas_sql = SELECT_BASE + "WHERE c.fecha_cobro BETWEEN ? AND ?";

    /**
     * Registra un nuevo cobro en la base de datos.
     *
     * Se utiliza para confirmar que un pago de suscripción se ha realizado.
     * Actualiza el objeto {@code cobro} con el ID generado automáticamente.
     *
     * @param cobro Objeto con los detalles del pago.
     * @return {@code true} si se guardó correctamente.
     */
    @Override
    public boolean create(Cobro cobro) {
        try(PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(create_sql, PreparedStatement.RETURN_GENERATED_KEYS)){
            pstm.setInt(1, cobro.getSuscripcion().getIdSuscripcion());
            pstm.setDate(2, Date.valueOf(cobro.getFecha_cobro()));
            pstm.setString(3, cobro.getMetodo_pago().name());
            pstm.setString(4, cobro.getDescripcion());
            pstm.setInt(5, cobro.getPeriodos_cubiertos());

            if (pstm.executeUpdate() > 0){
                ResultSet rs = pstm.getGeneratedKeys();
                if(rs.next()){
                    cobro.setId_cobro(rs.getInt(1));
                }
                return true;
            }

        }catch (SQLException e){
            System.out.println("Error creando cobro: " + e.getMessage());
        }
        return false;
    }

    /**
     * Busca un cobro específico por su ID.
     *
     * @param id Identificador del cobro.
     * @return Objeto {@link Cobro} con la suscripción asociada cargada, o {@code null}.
     */
    @Override
    public Cobro findById(int id) {
        Cobro cobro = null;
        try (PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(find_by_id_sql)){
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if(rs.next()){
                cobro = obtenerCobro(rs);
            }
        }catch (SQLException e){
            System.out.println("Error buscando cobro: " + e.getMessage());
        }
        return cobro;
    }

    /**
     * Recupera el historial completo de todos los cobros del sistema.
     *
     * @return Lista de cobros.
     */
    @Override
    public List<Cobro> findAll() {
        List<Cobro> cobrosList = new ArrayList<>();
        try (PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(find_all_sql)) {
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                cobrosList.add(obtenerCobro(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error ejecutando query de cobros: " + e.getMessage());
        }
        return cobrosList;
    }

    /**
     * Actualiza los detalles de un cobro existente.
     *
     * @param cobro Objeto con los datos modificados.
     * @return {@code true} si la actualización fue exitosa.
     */
    @Override
    public boolean update(Cobro cobro) {
        try(PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(update_sql)){
            pstm.setInt(1, cobro.getSuscripcion().getIdSuscripcion());
            pstm.setDate(2, Date.valueOf(cobro.getFecha_cobro()));
            pstm.setString(3, cobro.getMetodo_pago().name());
            pstm.setString(4, cobro.getDescripcion());
            pstm.setInt(5, cobro.getPeriodos_cubiertos());
            pstm.setInt(6, cobro.getId_cobro());

            return pstm.executeUpdate() > 0;
        }catch (SQLException e){
            System.out.println("Error actualizando cobro: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un registro de cobro.
     *
     * @param id Identificador del cobro a borrar.
     * @return {@code true} si se eliminó correctamente.
     */
    @Override
    public boolean delete(int id) {
        try(PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(delete_sql)){
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        }catch (SQLException e){
            System.out.println("Error eliminando cobro: " + e.getMessage());
            return false;
        }
    }

    // Métodos específicos de Cobro

    /**
     * Obtiene el historial de pagos de una suscripción específica.
     *
     * @param suscripcionId ID de la suscripción.
     * @return Lista de cobros asociados a esa suscripción.
     */
    public List<Cobro> findBySuscripcionId(int suscripcionId) {
        List<Cobro> cobrosSuscripcion = new ArrayList<>();
        try (PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(find_by_suscripcion_id_sql)) {
            pstm.setInt(1, suscripcionId);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                cobrosSuscripcion.add(obtenerCobro(rs));
            }
        }catch (SQLException e){
            System.out.println("Error buscando cobros por suscripción: " + e.getMessage());
        }
        return cobrosSuscripcion;
    }

    /**
     * Obtiene todos los pagos realizados por un usuario (Titular) en cualquiera de sus suscripciones.
     *
     * Utiliza un JOIN implícito en la consulta SQL para filtrar por el {@code id_titular}
     * de la tabla de suscripciones.
     *
     * @param usuarioId ID del usuario titular.
     * @return Lista de cobros del usuario.
     */
    public List<Cobro> findByUsuarioId(int usuarioId) {
        List<Cobro> cobrosSuscripcion = new ArrayList<>();
        try (PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(find_by_usuario_id_sql)) {
            pstm.setInt(1, usuarioId);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                cobrosSuscripcion.add(obtenerCobro(rs));
            }
        }catch (SQLException e){
            System.out.println("Error buscando cobros por usuario: " + e.getMessage());
        }
        return cobrosSuscripcion;
    }

    /**
     * Filtra los cobros realizados en un rango de fechas.
     *
     * Fundamental para la generación de informes financieros (ej. "Gasto total de Enero").
     *
     * @param desde Fecha inicial (inclusiva).
     * @param hasta Fecha final (inclusiva).
     * @return Lista de cobros dentro del periodo.
     */
    public List<Cobro> findByFechas(LocalDate desde, LocalDate hasta) {
        List<Cobro> cobrosSuscripcion = new ArrayList<>();
        try (PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(find_by_fechas_sql)) {
            pstm.setDate(1, Date.valueOf(desde));
            pstm.setDate(2, Date.valueOf(hasta));
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                cobrosSuscripcion.add(obtenerCobro(rs));
            }
        }catch (SQLException e){
            System.out.println("Error buscando cobros entre fechas: " + e.getMessage());
        }
        return cobrosSuscripcion;
    }

    // Métodos privados auxiliares

    /**
     * Mapea un ResultSet a un objeto Cobro.
     * Reconstruye también el objeto Suscripcion básico.
     */
    private Cobro obtenerCobro(ResultSet rs) throws SQLException{
        Cobro cobro = new Cobro();
        cobro.setId_cobro(rs.getInt("id_cobro"));

        cobro.setFecha_cobro(parsearFechaSegura(rs.getString("fecha_cobro")));

        cobro.setMetodo_pago(MetodoPago.valueOf(rs.getString("metodo_pago")));
        cobro.setDescripcion(rs.getString("descripcion"));
        cobro.setPeriodos_cubiertos(rs.getInt("periodos_cubiertos"));

        Suscripcion s = new Suscripcion();
        s.setIdSuscripcion(rs.getInt("id_suscripcion"));
        s.setNombre(rs.getString("nombre"));
        s.setPrecio(rs.getDouble("precio"));
        s.setCiclo(Ciclo.valueOf(rs.getString("ciclo")));
        s.setCategoria(Categoria.valueOf(rs.getString("categoria")));
        cobro.setSuscripcion(s);

        return cobro;
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
