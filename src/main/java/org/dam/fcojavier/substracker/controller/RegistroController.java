package org.dam.fcojavier.substracker.controller;

import javafx.scene.control.TextInputControl;
import org.dam.fcojavier.substracker.dao.UsuarioDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;
import org.dam.fcojavier.substracker.model.Usuario;
import org.dam.fcojavier.substracker.utils.Validaciones;

import java.io.IOException;

public class RegistroController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblError;

    private final UsuarioDAO usuarioDAO;

    public RegistroController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    @FXML
    private void handleRegistro(ActionEvent event) {
        limpiarEstilos();

        String nombre = txtNombre.getText();
        String apellidos = txtApellidos.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        boolean hayErrorCamposVacios = false;

        if (!Validaciones.esTextoValido(nombre)) {
            marcarCampoError(txtNombre);
            hayErrorCamposVacios = true;
        }
        if (!Validaciones.esTextoValido(apellidos)) {
            marcarCampoError(txtApellidos);
            hayErrorCamposVacios = true;
        }
        if (!Validaciones.esTextoValido(email)) {
            marcarCampoError(txtEmail);
            hayErrorCamposVacios = true;
        }
        if (!Validaciones.esTextoValido(password)) {
            marcarCampoError(txtPassword);
            hayErrorCamposVacios = true;
        }
        if (!Validaciones.esTextoValido(confirmPassword)) {
            marcarCampoError(txtConfirmPassword);
            hayErrorCamposVacios = true;
        }

        if (hayErrorCamposVacios) {
            mostrarError("Por favor, rellena los campos marcados.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            marcarCampoError(txtPassword);
            marcarCampoError(txtConfirmPassword);
            mostrarError("Las contraseñas no coinciden.");
            txtConfirmPassword.clear();
            return;
        }

        if (!Validaciones.esEmailValido(email)) {
            marcarCampoError(txtEmail);
            mostrarError("El formato del email no es válido.");
            return;
        }

        if (!Validaciones.esPasswordValida(password)) {
            marcarCampoError(txtPassword);
            marcarCampoError(txtConfirmPassword);
            mostrarError("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        if (usuarioDAO.findByEmail(email) != null) {
            marcarCampoError(txtEmail);
            mostrarError("Ese email ya está registrado en el sistema.");
            return;
        }

        Usuario nuevoUsuario = new Usuario(0, nombre, apellidos, email, password);

        if (usuarioDAO.create(nuevoUsuario)) {
            System.out.println("Usuario registrado: " + nuevoUsuario.getNombre());

            mostrarLogin(event);
        } else {
            mostrarError("Error al guardar en la base de datos.");
        }
    }

    @FXML
    private void volverLogin(ActionEvent event) {
        mostrarLogin(event);
    }

    // Método auxiliar para cambiar de escena

    private void mostrarLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/loginView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root, 800, 600);

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void limpiarEstilos() {
        lblError.setVisible(false);

        quitarEstiloError(txtNombre);
        quitarEstiloError(txtApellidos);
        quitarEstiloError(txtEmail);
        quitarEstiloError(txtPassword);
        quitarEstiloError(txtConfirmPassword);
    }

    private void quitarEstiloError(TextInputControl campo) {
        campo.getStyleClass().remove("error");
    }

    private void marcarCampoError(TextInputControl campo) {
        if (!campo.getStyleClass().contains("error")) {
            campo.getStyleClass().add("error");
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }
}
