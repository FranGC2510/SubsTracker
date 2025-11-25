package org.dam.fcojavier.substracker.dao;

import org.dam.fcojavier.substracker.utils.connection.ConnectionDB;
import org.dam.fcojavier.substracker.interfaces.CrudDao;
import org.dam.fcojavier.substracker.model.Cobro;
import org.dam.fcojavier.substracker.model.Participa;
import org.dam.fcojavier.substracker.model.Suscripcion;
import org.dam.fcojavier.substracker.model.Usuario;
import org.dam.fcojavier.substracker.model.enums.Categoria;
import org.dam.fcojavier.substracker.model.enums.Ciclo;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la entidad {@link Suscripcion}.
 *
 * Gestiona todas las operaciones de persistencia relacionadas con las suscripciones.
 *
 * Características de Diseño:
 * Optimización SQL: Utiliza {@code INNER JOIN} para recuperar los datos del
 * usuario {@code Titular} en la misma consulta, evitando el problema de rendimiento "N+1".
 *
 * El listado general ({@code findAll}) es ligero (sin listas anidadas).
 * La consulta por ID ({@code findById}) es completa (incluye historial de cobros y participantes).
 *
 * @author Fco Javier García
 * @version 1.0
 */
public class SuscripcionDAO implements CrudDao<Suscripcion> {
    private final String create_sql="INSERT INTO suscripcion (nombre, precio, ciclo, categoria, activo, fecha_activacion, fecha_renovacion, id_titular) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private final String update_sql="UPDATE suscripcion SET nombre = ?, precio = ?, ciclo = ?, categoria = ?, activo = ?, fecha_activacion = ?, fecha_renovacion = ?, id_titular = ? WHERE id_suscripcion = ?";
    private final String delete_sql="DELETE FROM suscripcion WHERE id_suscripcion = ?";
    private final String SELECT_BASE = "SELECT s.*, u.id_usuario, u.email, u.nombre AS u_nombre, u.apellidos " +
                    "FROM suscripcion s " +
                    "INNER JOIN usuario u ON s.id_titular = u.id_usuario ";

    private final String find_all_sql = SELECT_BASE;
    private final String find_by_id_sql = SELECT_BASE + "WHERE s.id_suscripcion = ?";
    private final String find_by_categoria_sql = SELECT_BASE + "WHERE s.categoria = ?";
    private final String find_by_ciclo_sql = SELECT_BASE + "WHERE s.ciclo = ?";
    private final String find_by_titular_id_sql = SELECT_BASE + "WHERE s.id_titular = ?";

    /**
     * Registra una nueva suscripción en la base de datos.
     *
     * Se recupera la clave primaria generada (ID) y se asigna al objeto pasado por parámetro.
     *
     * @param suscripcion Objeto con los datos a guardar.
     * @return {@code true} si la operación fue exitosa.
     */
    @Override
    public boolean create(Suscripcion suscripcion) {
        try(PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(create_sql, PreparedStatement.RETURN_GENERATED_KEYS)){
            pstm.setString(1, suscripcion.getNombre());
            pstm.setDouble(2, suscripcion.getPrecio());
            pstm.setString(3, suscripcion.getCiclo().name());
            pstm.setString(4, suscripcion.getCategoria().name());
            pstm.setBoolean(5, suscripcion.isActivo());
            pstm.setDate(6, Date.valueOf(suscripcion.getFechaActivacion()));
            pstm.setDate(7, Date.valueOf(suscripcion.getFechaRenovacion()));
            pstm.setInt(8, suscripcion.getTitular().getId_usuario());

            if (pstm.executeUpdate() > 0){
                ResultSet rs = pstm.getGeneratedKeys();
                if(rs.next()){
                    suscripcion.setIdSuscripcion(rs.getInt(1));
                    return true;
                }
            }
        }catch (SQLException e){
            System.out.println("Error creando suscripcion: " + e.getMessage());
        }
        return false;
    }

    /**
     * Busca una suscripción por su ID y realiza una carga completa de datos (Eager Loading).
     *
     * Además de los datos básicos y el titular, este método invoca a {@link CobroDAO} y
     * {@link ParticipaDAO} para llenar las listas de historial de pagos y copagadores.
     * Use este método para ver el Detalle de una suscripción.
     *
     * @param id Identificador de la suscripción.
     * @return Objeto {@link Suscripcion} completo o {@code null} si no existe.
     */
    @Override
    public Suscripcion findById(int id) {
        Suscripcion suscripcion=null;
        try(PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(find_by_id_sql)){
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if(rs.next()){
                suscripcion = obtenerSuscripcion(rs);
                // SOLO cargamos las listas completas si buscamos por ID específico (Detalle)
                suscripcion.setCobros(findCobrosBySuscripcion(suscripcion.getIdSuscripcion()));
                suscripcion.setParticipantes(findParticipantesBySuscripcion(suscripcion.getIdSuscripcion()));
            }
        }catch (SQLException e){
            System.out.println("Error buscando suscripcion: " + e.getMessage());
        }
        return suscripcion;
    }

    /**
     * Recupera todas las suscripciones del sistema.
     *
     * Realiza una carga ligera. No incluye las listas
     * de cobros ni participantes para optimizar la velocidad del listado general.
     *
     * @return Lista de suscripciones.
     */
    @Override
    public List<Suscripcion> findAll() {
        List<Suscripcion> suscripciones =new ArrayList<>();
        try(PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(find_all_sql)){
            ResultSet rs= pstm.executeQuery();
            while (rs.next()){
                Suscripcion suscripcion = obtenerSuscripcion(rs);
                // En el listado general NO cargamos cobros ni participantes para no saturar la BD
                suscripciones.add(suscripcion);
            }
        }catch (SQLException e){
            System.out.println("Error obteniendo suscripciones: " + e.getMessage());
        }
        return suscripciones;
    }

    /**
     * Actualiza los datos de una suscripción existente.
     *
     * @param suscripcion Objeto con los datos modificados.
     * @return {@code true} si se actualizó correctamente.
     */
    @Override
    public boolean update(Suscripcion suscripcion) {
        try(PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(update_sql)){
            pstm.setString(1, suscripcion.getNombre());
            pstm.setDouble(2, suscripcion.getPrecio());
            pstm.setString(3, suscripcion.getCiclo().name());
            pstm.setString(4, suscripcion.getCategoria().name());
            pstm.setBoolean(5, suscripcion.isActivo());
            pstm.setDate(6, Date.valueOf(suscripcion.getFechaActivacion()));
            pstm.setDate(7, Date.valueOf(suscripcion.getFechaRenovacion()));
            pstm.setInt(8, suscripcion.getTitular().getId_usuario());
            pstm.setInt(9, suscripcion.getIdSuscripcion());

            return pstm.executeUpdate() > 0;
        }catch (SQLException e){
            System.out.println("Error actualizando suscripcion: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina una suscripción de la base de datos.
     *
     * Dependiendo de la configuración de la BD, esto podría eliminar
     * también los registros asociados en las tablas 'cobro' y 'participa'.
     *
     * @param id Identificador de la suscripción.
     * @return {@code true} si se eliminó correctamente.
     */
    @Override
    public boolean delete(int id) {
        try(PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(delete_sql)){
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        }catch (SQLException e){
            System.out.println("Error eliminando suscripcion: " + e.getMessage());
            return false;
        }
    }

    // Métodos específicos de Suscripcion

    /**
     * Filtra suscripciones por categoría (OCIO, HOGAR, etc.).
     * @param categoria Nombre de la categoría.
     * @return Lista de suscripciones (carga ligera).
     */
    public List<Suscripcion> findByCategoria(String categoria) {
        return findByStringField(find_by_categoria_sql, categoria);
    }

    /**
     * Filtra suscripciones por ciclo de facturación (MENSUAL, ANUAL, etc.).
     * @param ciclo Objeto Enum Ciclo.
     * @return Lista de suscripciones (carga ligera).
     */
    public List<Suscripcion> findByCiclo(Ciclo ciclo) {
        return findByStringField(find_by_ciclo_sql, ciclo.name());
    }

    /**
     * Busca todas las suscripciones pertenecientes a un usuario titular específico.
     *
     * Importante: Este método realiza una carga completa
     * (incluye cobros y participantes), ya que suele usarse para mostrar el perfil completo del usuario.
     *
     * @param usuarioId ID del titular.
     * @return Lista completa de suscripciones del usuario.
     */
    public List<Suscripcion> findByTitularId(int usuarioId) {
        List<Suscripcion> suscripcionesTitular=new ArrayList<>();
        try(PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(find_by_titular_id_sql)){
            pstm.setInt(1, usuarioId);
            ResultSet rs= pstm.executeQuery();
            while (rs.next()){
                Suscripcion suscripcion = obtenerSuscripcion(rs);
                suscripcion.setCobros(findCobrosBySuscripcion(suscripcion.getIdSuscripcion()));
                suscripcion.setParticipantes(findParticipantesBySuscripcion(suscripcion.getIdSuscripcion()));
                suscripcionesTitular.add(suscripcion);
            }
        }catch (SQLException e){
            System.out.println("Error obteniendo suscripciones por titular: " + e.getMessage());
        }
        return suscripcionesTitular;
    }

    //Metodos auxiliares privados

    private List<Cobro> findCobrosBySuscripcion(int idSuscripcion){
        CobroDAO cobroDAO = new CobroDAO();
        return cobroDAO.findBySuscripcionId(idSuscripcion);
    }

    private List<Participa> findParticipantesBySuscripcion(int idSuscripcion){
        ParticipaDAO participaDAO = new ParticipaDAO();
        return participaDAO.findBySuscripcionId(idSuscripcion);
    }

    /**
     * Mapea un ResultSet a un objeto Suscripcion.
     * Incluye la construcción del objeto Usuario (Titular) gracias al JOIN.
     */
    private Suscripcion obtenerSuscripcion(ResultSet rs) throws SQLException{
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setIdSuscripcion(rs.getInt("id_suscripcion"));
        suscripcion.setNombre(rs.getString("nombre"));
        suscripcion.setPrecio(rs.getDouble("precio"));
        suscripcion.setCiclo(Ciclo.valueOf(rs.getString("ciclo")));
        suscripcion.setCategoria(Categoria.valueOf(rs.getString("categoria")));
        suscripcion.setActivo(rs.getBoolean("activo"));
        suscripcion.setFechaActivacion(rs.getDate("fecha_activacion").toLocalDate());
        suscripcion.setFechaRenovacion(rs.getDate("fecha_renovacion").toLocalDate());

        Usuario u = new Usuario();
        u.setId_usuario(rs.getInt("id_titular")); // O usar rs.getInt("u.id_usuario")
        u.setEmail(rs.getString("email"));
        u.setNombre(rs.getString("u_nombre"));
        u.setApellidos(rs.getString("apellidos"));
        // No cargamos password por seguridad

        suscripcion.setTitular(u);

        // Inicializamos listas vacías para evitar NullPointerException si se usan
        suscripcion.setCobros(new ArrayList<>());
        suscripcion.setParticipantes(new ArrayList<>());

        return suscripcion;
    }

    private List<Suscripcion> findByStringField(String sql, String campo){
        List<Suscripcion> suscripciones = new ArrayList<>();
        try(PreparedStatement pstm = ConnectionDB.getConnection().prepareStatement(sql)){
            pstm.setString(1, campo);
            ResultSet rs = pstm.executeQuery();
            while(rs.next()){
                suscripciones.add(obtenerSuscripcion(rs));
            }
        }catch (SQLException e){
            System.out.println("Error obteniendo suscripciones: " + e.getMessage());
        }
        return suscripciones;
    }
}
