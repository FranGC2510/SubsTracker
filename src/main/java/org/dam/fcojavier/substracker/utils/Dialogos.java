package org.dam.fcojavier.substracker.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import javafx.stage.Stage;
import javafx.stage.Window;
import java.util.Optional;

public class Dialogos {
    public static Optional<ButtonType> mostrarConfirmacion(String titulo, String cabecera, String contenido, Window owner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        estilizarAlerta(alert, titulo, cabecera, contenido, owner);
        return alert.showAndWait();
    }

    public static void mostrarError(String titulo, String contenido, Window owner) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        estilizarAlerta(alert, titulo, null, contenido, owner);
        alert.showAndWait();
    }

    /**
     * Muestra una alerta de información (Éxito).
     */
    public static void mostrarExito(String titulo, String contenido, Window owner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        estilizarAlerta(alert, titulo, null, contenido, owner);
        alert.showAndWait();
    }

    /**
     * Muestra una alerta de advertencia (Warning).
     */
    public static void mostrarAdvertencia(String titulo, String contenido, Window owner) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        estilizarAlerta(alert, titulo, null, contenido, owner);
        alert.showAndWait();
    }

    // Método privado para aplicar el CSS y el Owner
    private static void estilizarAlerta(Alert alert, String titulo, String cabecera, String contenido, Window owner) {
        alert.setTitle(titulo);
        alert.setHeaderText(cabecera);
        alert.setContentText(contenido);

        if (owner != null) {
            alert.initOwner(owner);
        }

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Dialogos.class.getResource("/org/dam/fcojavier/substracker/view/style.css").toExternalForm());
        dialogPane.getStyleClass().add("my-dialog");
    }
}
