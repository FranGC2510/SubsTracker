package org.dam.fcojavier.substracker.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import javafx.stage.Stage;
import javafx.stage.Window;
import java.util.Optional;

/**
 * Clase de utilidad para la gestión centralizada de ventanas emergentes (Dialogs).
 *
 * Proporciona métodos estáticos para mostrar alertas preconfiguradas (Error, Información,
 * Confirmación, Advertencia) asegurando una consistencia visual en toda la aplicación.
 *
 * Características:
 * Inyección automática de la hoja de estilos CSS para mantener el "Dark Mode".
 * Configuración del "Owner" (ventana padre) para centrar la alerta y bloquear la interacción trasera.
 *
 * @author Fco Javier García
 * @version 2.0
 */
public class Dialogos {
    /**
     * Muestra una alerta de confirmación con botones Aceptar/Cancelar.
     *
     * Utilizada para operaciones críticas irreversibles, como eliminar registros.
     *
     * @param titulo Título de la ventana.
     * @param cabecera Texto destacado en la parte superior.
     * @param contenido Texto detallado de la pregunta.
     * @param owner La ventana desde la que se lanza la alerta (para centrado y modalidad).
     * @return Un {@link Optional} que contiene el tipo de botón pulsado (ej. ButtonType.OK).
     */
    public static Optional<ButtonType> mostrarConfirmacion(String titulo, String cabecera, String contenido, Window owner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        estilizarAlerta(alert, titulo, cabecera, contenido, owner);
        return alert.showAndWait();
    }

    /**
     * Muestra una alerta de error bloqueante.
     *
     * Utilizada cuando ocurre una excepción o un fallo en la validación que impide continuar.
     *
     * @param titulo Título de la ventana (ej. "Error de Conexión").
     * @param contenido Descripción del error.
     * @param owner La ventana padre.
     */
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
     * Muestra una alerta de advertencia.
     *
     * Utilizada para avisar al usuario de situaciones anómalas que no impiden
     * el funcionamiento (ej. "No hay datos para exportar").
     *
     * @param titulo Título de la ventana.
     * @param contenido Mensaje de advertencia.
     * @param owner La ventana padre.
     */
    public static void mostrarAdvertencia(String titulo, String contenido, Window owner) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        estilizarAlerta(alert, titulo, null, contenido, owner);
        alert.showAndWait();
    }

    /**
     * Aplica la configuración común a todas las alertas: Textos, Dueño y Estilos CSS.
     *
     * @param alert La instancia de la alerta a configurar.
     * @param titulo Título de la ventana.
     * @param cabecera Texto de cabecera (puede ser null).
     * @param contenido Texto del cuerpo.
     * @param owner Ventana propietaria.
     */
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

    /**
     * Muestra una alerta informativa o de éxito.
     *
     * Utilizada para feedback positivo (ej. "Usuario registrado correctamente") o
     * información general.
     *
     * @param titulo Título de la ventana.
     * @param contenido Mensaje informativo.
     * @param owner La ventana padre.
     */
    public static void mostrarInformacion(String titulo, String contenido, Window owner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        estilizarAlerta(alert, titulo, null, contenido, owner);
        alert.showAndWait();
    }
}
