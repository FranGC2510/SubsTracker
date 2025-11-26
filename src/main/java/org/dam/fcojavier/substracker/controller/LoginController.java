package org.dam.fcojavier.substracker.controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.dam.fcojavier.substracker.dao.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.dam.fcojavier.substracker.model.Usuario;
import org.dam.fcojavier.substracker.utils.PasswordUtilidades;
import org.dam.fcojavier.substracker.utils.Validaciones;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;

    private final UsuarioDAO usuarioDAO;

    public LoginController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    // El método initialize se ejecuta DESPUÉS de cargar el FXML (útil para setup inicial)
    @FXML
    public void initialize() {
        lblError.setVisible(false);
    }

    /**
     * Método que se ejecuta al pulsar el botón "ENTRAR"
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        limpiarEstilos();

        boolean hayErrorValidacion = false;

        if (!Validaciones.esTextoValido(email)) {
            marcarCampoError(txtEmail);
            hayErrorValidacion = true;
        }

        if (!Validaciones.esTextoValido(password)) {
            marcarCampoError(txtPassword);
            hayErrorValidacion = true;
        }

        if (hayErrorValidacion) {
            mostrarMensajeError("Por favor, rellena los campos marcados.");
            return;
        }

        // 3. Validar formato de email
        if (!Validaciones.esEmailValido(email)) {
            marcarCampoError(txtEmail);
            mostrarMensajeError("El formato del email no es correcto.");
            return;
        }

        Usuario usuarioEncontrado = usuarioDAO.findByEmail(email);

        if (usuarioEncontrado != null) {
            if (PasswordUtilidades.checkPassword(password, usuarioEncontrado.getPassword())) {
                loginExitoso(usuarioEncontrado);
            } else {
                marcarCampoError(txtPassword);
                mostrarMensajeError("Contraseña incorrecta.");
                txtPassword.clear();
            }
        } else {
            marcarCampoError(txtEmail);
            mostrarMensajeError("No existe cuenta con ese email.");
        }
    }

    @FXML
    private void irARegistro(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/registroView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error cargando la vista de registro.");
        }
    }

    // Métodos auxiliares

    private void limpiarEstilos() {
        lblError.setVisible(false);
        txtEmail.getStyleClass().remove("error");
        txtPassword.getStyleClass().remove("error");
    }

    private void marcarCampoError(TextField campo) {
        if (!campo.getStyleClass().contains("error")) {
            campo.getStyleClass().add("error");
        }
    }

    private void mostrarMensajeError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }

    private void loginExitoso(Usuario usuario) {
        lblError.setVisible(false);
        System.out.println("Login correcto para: " + usuario.getNombre());

        mostrarAlerta("Bienvenido", "Hola de nuevo, " + usuario.getNombre());

        // TODO: AQUI HAREMOS EL CAMBIO DE PANTALLA (SPRINT 3 - PASO 2)
        // cerrarVentanaLogin();
        // abrirVentanaPrincipal(usuario);
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
