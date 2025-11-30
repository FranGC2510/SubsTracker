package org.dam.fcojavier.substracker.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.dam.fcojavier.substracker.model.Suscripcion;
import org.dam.fcojavier.substracker.model.Usuario;

import java.io.IOException;

/**
 * Controlador principal de la aplicación (Dashboard / Layout Base).
 *
 * Esta clase actúa como el contenedor y "Enrutador" de la aplicación.
 * Sus responsabilidades son:
 * Gestionar la barra lateral de navegación (Menú).
 * Mostrar la información del usuario logueado (Avatar e Iniciales).
 * Cargar dinámicamente las vistas (Suscripciones, Estadísticas, Detalles) en el área central.
 * Gestionar el cierre de sesión y el retorno al Login.
 *
 * @author Fco Javier García
 * @version 2.0
 */
public class MainController {
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblIniciales;
    @FXML private StackPane contentArea;

    private Usuario usuarioLogueado;

    /**
     * Configura la sesión del usuario al entrar al Dashboard.
     *
     * Este método es llamado desde el Login. Se encarga de:
     * 1. Guardar el usuario en memoria.
     * 2. Actualizar la interfaz (Nombre y Avatar).
     * 3. Cargar la vista por defecto (Lista de Suscripciones).
     *
     * @param usuario El usuario autenticado.
     */
    public void setUsuario(Usuario usuario) {
        this.usuarioLogueado = usuario;

        actualizarInfoUsuario(usuario);

        cargarVistaSuscripciones();
    }

    /**
     * Solo actualiza el nombre y avatar del menú lateral.
     * Útil para refrescar datos desde Configuración sin cambiar de pantalla.
     */
    public void actualizarInfoUsuario(Usuario usuario) {
        this.usuarioLogueado = usuario; // Aseguramos que tenemos el objeto actualizado
        lblNombreUsuario.setText(usuario.getNombre());
        lblIniciales.setText(calcularIniciales(usuario.getNombre()));
    }

    /**
     * Acción del botón del menú "Mis Suscripciones".
     */
    @FXML
    public void mostrarSuscripciones(ActionEvent event) {
        cargarVistaSuscripciones();
    }

    /**
     * Acción del botón del menú "Informes y Gastos".
     */
    @FXML
    public void mostarEstadisticas(ActionEvent event) {
        cargarVistaInformes();
    }

    /**
     * Cierra la sesión actual, limpia los datos y devuelve al usuario a la pantalla de Login.
     *
     * @param event Evento del botón (necesario para obtener el Stage).
     */
    @FXML
    public void cerrarSesion(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/loginView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.show();

            System.out.println("Sesión cerrada.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carga la tabla de suscripciones en el área central.
     * Inyecta este controlador ({@code this}) al hijo para permitir navegación futura.
     */
    public void cargarVistaSuscripciones() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/suscripcionesView.fxml"));
            Parent view = loader.load();

            SuscripcionesController controller = loader.getController();
            controller.initData(this.usuarioLogueado, this);

            actualizarZonaCentral(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navega a la vista de detalle de una suscripción.
     * Método público llamado desde {@link SuscripcionesController} al hacer doble clic.
     *
     * @param suscripcion La suscripción seleccionada.
     */
    public void mostrarDetalleSuscripcion(Suscripcion suscripcion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/detalleSuscripcionView.fxml"));
            Parent view = loader.load();

            DetalleSuscripcionController controller = loader.getController();
            controller.initData(suscripcion, this.usuarioLogueado, this);

            actualizarZonaCentral(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carga la vista de informes y estadísticas financieras.
     */
    @FXML
    public void cargarVistaInformes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/informesView.fxml"));
            Parent view = loader.load();

            InformesController controller = loader.getController();
            controller.initData(this.usuarioLogueado);

            actualizarZonaCentral(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error cargando la vista de estadísticas.");
        }
    }

    @FXML
    public void mostrarConfiguracion(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/configView.fxml"));
            Parent view = loader.load();

            ConfigController controller = loader.getController();
            controller.initData(this.usuarioLogueado, this);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error cargando la configuración.");
        }
    }

    //Métodos auxiliares

    /**
     * Reemplaza el contenido del área central con la nueva vista cargada.
     *
     * @param view El nodo raíz de la nueva vista.
     */
    private void actualizarZonaCentral(Parent view) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    /**
     * Genera las iniciales para el avatar visual a partir del nombre.
     *
     * Lógica: Si el nombre tiene 2 o más letras, usa las dos primeras.
     * Si es más corto, usa lo que haya.
     *
     * @param nombre El nombre del usuario.
     * @return String con las iniciales en mayúsculas.
     */
    private String calcularIniciales(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return "";
        }

        if (nombre.length() >= 2) {
            return nombre.substring(0, 2).toUpperCase();
        } else {
            return nombre.toUpperCase();
        }
    }
}
