package org.dam.fcojavier.substracker;

import org.dam.fcojavier.substracker.connection.ConnectionDB;

import java.sql.Connection;

public class Tests {
    public static void main(String[] args) {

        //Obtener conexion a base de datos MySQL
        Connection con = ConnectionDB.getConnection();

        if(con !=null){
            System.out.println("¡Conexión a la base de datos establecida!");
        }else{
            System.out.println("¡Error en la conexión a la base de datos!");
        }

        //Cerrar conexion a base de datos
        ConnectionDB.closeConnection();
    }
}
