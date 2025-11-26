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

public class MainController {
    @FXML private Label lblNombreUsuario;
    @FXML private StackPane contentArea;

    private Usuario usuarioLogueado;

    // Este método lo llamaremos desde el LoginController para pasarle los datos
    public void setUsuario(Usuario usuario) {
        this.usuarioLogueado = usuario;
        lblNombreUsuario.setText(usuario.getNombre());
    }

    @FXML
    public void mostrarSuscripciones(ActionEvent event) {
        cargarVistaSuscripciones();
    }

    @FXML
    public void mostrarEstadisticas(ActionEvent event) {
        System.out.println("Navegando a Estadísticas...");
        // cargarVista("statsView.fxml");
    }

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

    // Método auxiliar público para volver desde el detalle
    public void cargarVistaSuscripciones() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/suscripcionesView.fxml"));
            Parent view = loader.load();

            SuscripcionesController controller = loader.getController();
            controller.initData(this.usuarioLogueado, this);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Método para ir al detalle
    public void mostrarDetalleSuscripcion(Suscripcion suscripcion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/detalleSuscripcionView.fxml"));
            Parent view = loader.load();

            DetalleSuscripcionController controller = loader.getController();
            controller.initData(suscripcion, this.usuarioLogueado, this);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
