package org.dam.fcojavier.substracker.utils.connection;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Gestor de conexiones a base de datos utilizando el patrón Singleton.
 *
 * Esta clase centraliza la configuración y acceso a la persistencia de datos,
 * permitiendo alternar dinámicamente entre una base de datos remota (MySQL)
 * y una local basada en ficheros (SQLite).
 *
 * @author Fco Javier García
 * @version 2.0
 */
public class ConnectionDB {
    /** Enum con los tipos de bases de datos soportados. */
    public enum DBType { MYSQL, SQLITE }

    /**
     * Instancia única de la clase (Patrón Singleton).
     */
    private static ConnectionDB _instance;

    /**
     * Objeto de conexión JDBC activo.
     */
    private static Connection con;

    /** Configuración seleccionada por el usuario (Por defecto MySQL). */
    private static DBType tipoSeleccionado = DBType.MYSQL;

    /**
     * Constructor privado.
     *
     * Ejecuta la lógica de conexión:
     * Determina el archivo de propiedades a leer (.properties).
     * Carga las credenciales.
     * Si es SQLite, asegura la existencia del directorio 'data'.
     * Establece la conexión JDBC.
     * Si es SQLite y está vacía, ejecuta el script de creación de tablas.
     */
    private ConnectionDB() {
        try {
            Properties props = new Properties();

            String fileName = (tipoSeleccionado == DBType.MYSQL)
                    ? "database_mysql.properties"
                    : "database_sqlite.properties";
            String path = "/configDB/" + fileName;

            try(InputStream is = getClass().getResourceAsStream(path)){
                if (is == null) {
                    throw new RuntimeException("No se encuentra el archivo de configuración en: " + path);
                }

                props.load(is);
            }

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String pass = props.getProperty("db.password");

            if (tipoSeleccionado == DBType.SQLITE) {
                File dir = new File("data");
                if (!dir.exists()) dir.mkdirs();

                con = DriverManager.getConnection(url);
                inicializarTablasSQLite();
            } else {
                con = DriverManager.getConnection(url, user, pass);
            }
            System.out.println("Conectado a " + tipoSeleccionado + " usando configuración de: " + path);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error crítico conectando a: " + tipoSeleccionado);
        }
    }

    /**
     * Cambia el tipo de base de datos a utilizar.
     *
     * Cierra la conexión actual y resetea la instancia Singleton para forzar
     * una reconexión con el nuevo tipo en la próxima llamada.
     *
     * @param tipo El nuevo tipo de base de datos (MYSQL o SQLITE).
     */
    public static void setTipo(DBType tipo) {
        tipoSeleccionado = tipo;
        closeConnection();
        _instance = null;
        con = null;
    }

    /**
     * Punto de acceso global a la conexión de la base de datos.
     *
     * Implementa "Lazy Initialization": crea la conexión solo cuando se necesita.
     *      * También verifica si la conexión se ha cerrado inesperadamente y la reabre.
     *
     * @return El objeto {@link Connection} activo a la base de datos, o {@code null} si hubo un fallo.
     */
    public static Connection getConnection() {
        if (_instance == null || con == null) {
            _instance = new ConnectionDB();
        }
        try {
            if (con != null && con.isClosed()) {
                _instance = new ConnectionDB();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return con;
    }

    /**
     * Cierra la conexión activa con la base de datos de forma segura.
     *
     * Se debe llamar a este método al finalizar la ejecución de la aplicación para
     * liberar los recursos del servidor de base de datos.
     */
    public static void closeConnection() {
        try {
            if (con != null && !con.isClosed()) con.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Devuelve el tipo de base de datos que está activa actualmente.
     */
    public static DBType getTipoSeleccionado() {
        return tipoSeleccionado;
    }

    // Métodos privados

    /**
     * Verifica si la base de datos SQLite tiene tablas.
     * Si no existen, ejecuta el script SQL de creación.
     */
    private void inicializarTablasSQLite() {
        try {
            try (Statement st = con.createStatement()) {
                st.executeQuery("SELECT 1 FROM usuario LIMIT 1");
            }
        } catch (SQLException e) {
            System.out.println("Base de datos SQLite nueva. Creando tablas...");
            ejecutarScriptSQL("/configDB/script_sqlite.sql");
        }
    }

    /**
     * Lee y ejecuta un archivo .sql desde los recursos.
     *
     * @param resourcePath Ruta absoluta del archivo en resources.
     */
    private void ejecutarScriptSQL(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)){
            if (is == null) {
                System.err.println("ERROR CRÍTICO: No se encuentra el script SQL: " + resourcePath);
                return;
            }

            String sql = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));

            try (Statement st = con.createStatement()) {
                st.executeUpdate(sql);
                System.out.println("Tablas SQLite creadas.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error al ejecutar el script SQL inicial.");
        }
    }
}
