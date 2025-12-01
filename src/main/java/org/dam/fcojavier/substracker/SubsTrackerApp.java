package org.dam.fcojavier.substracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;

public class SubsTrackerApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/loginView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 750, 600);

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

    public static void main(String[] args) {
        launch();
    }
}
