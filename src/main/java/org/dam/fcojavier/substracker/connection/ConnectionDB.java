package org.dam.fcojavier.substracker.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionDB {
    private static ConnectionDB _instance;
    private static Connection con;

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

    public static Connection getConnection() {
        if (_instance == null) {
            _instance = new ConnectionDB();
        }
        return con;
    }

    public static void closeConnection() {
        try {
            if (con != null) con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
