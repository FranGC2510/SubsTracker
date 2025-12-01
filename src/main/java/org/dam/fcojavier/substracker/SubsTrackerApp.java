package org.dam.fcojavier.substracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.dam.fcojavier.substracker.utils.connection.ConnectionDB;

import java.awt.*;
import java.io.IOException;

public class SubsTrackerApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/loginView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 750, 650);

        try {
            Image icon = new Image(getClass().getResourceAsStream("/ic_app.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono de la ventana.");
        }

        try {
            // Detectamos si es Mac
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("mac")) {
                java.net.URL iconURL = getClass().getResource("/ic_app.png");

                if (iconURL != null) {
                    java.awt.Image awtImage = Toolkit.getDefaultToolkit().getImage(iconURL);

                    // Verificamos si el sistema soporta cambiar el icono
                    if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
                        Taskbar.getTaskbar().setIconImage(awtImage);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("No se pudo cambiar el icono del Dock.");
        }

        stage.setTitle("SubTracker - Iniciar Sesión");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Método del Ciclo de Vida de JavaFX.
     * Se ejecuta AUTOMÁTICAMENTE justo antes de que el proceso muera.
     */
    @Override
    public void stop() throws Exception {
        System.out.println("PARANDO APLICACIÓN...");

        // AQUÍ CERRAMOS LA CONEXIÓN
        try {
            ConnectionDB.closeConnection();
            System.out.println("Conexión a Base de Datos cerrada con éxito.");
        } catch (Exception e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }

        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}
