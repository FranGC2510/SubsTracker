package org.dam.fcojavier.substracker.utils.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestiona la conexión a la base de datos MySQL implementando el patrón de diseño Singleton.
 *
 * Esta clase asegura que solo exista una única instancia de la conexión a la base de datos
 * durante el ciclo de vida de la aplicación, optimizando recursos y centralizando la configuración.
 * Lee las credenciales de conexión (URL, usuario, contraseña) desde un archivo externo
 * llamado {@code database_mysql.properties} ubicado en el classpath.
 *
 * @author Fco Javier García
 * @version 1.0
 */
public class ConnectionDB {

    /**
     * Instancia única de la clase (Patrón Singleton).
     */
    private static ConnectionDB _instance;

    /**
     * Objeto de conexión JDBC activo.
     */
    private static Connection con;

    /**
     * Constructor privado para prevenir la instanciación directa desde otras clases.
     *
     * Se encarga de cargar el driver, leer el archivo de propiedades {@code database_mysql.properties}
     * y establecer la conexión inicial. Si ocurre un error, la conexión quedará como {@code null}.
     */
    private ConnectionDB() {
        try {
            Properties props = new Properties();
            props.load(getClass().getClassLoader().getResourceAsStream("database_mysql.properties"));

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String pass = props.getProperty("db.password");

            con = DriverManager.getConnection(url, user, pass);

        } catch (Exception e) {
            e.printStackTrace();
            con = null;
        }
    }

    /**
     * Punto de acceso global a la conexión de la base de datos.
     *
     * Implementa "Lazy Initialization": solo crea la instancia
     * la primera vez que se solicita. En las siguientes llamadas, devuelve la existente.
     *
     * @return El objeto {@link Connection} activo a la base de datos, o {@code null} si hubo un fallo.
     */
    public static Connection getConnection() {
        if (_instance == null) {
            _instance = new ConnectionDB();
        }
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
            if (con != null) con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
