package org.dam.fcojavier.substracker.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.dam.fcojavier.substracker.dao.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.dam.fcojavier.substracker.model.Usuario;
import org.dam.fcojavier.substracker.utils.Dialogos;
import org.dam.fcojavier.substracker.utils.PasswordUtilidades;
import org.dam.fcojavier.substracker.utils.Validaciones;

import javafx.event.ActionEvent;

import java.io.IOException;

/**
 * Controlador de la pantalla de Inicio de Sesión (Login).
 *
 * Gestiona la autenticación de usuarios contra la base de datos.
 * Utiliza algoritmos de hashing (BCrypt) para verificar la contraseña de forma segura.
 *
 * @author Fco Javier García
 * @version 2.0
 */
public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;

    private final UsuarioDAO usuarioDAO;

    /**
     * Constructor por defecto.
     * Inicializa el DAO de usuarios para realizar las consultas.
     */
    public LoginController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Inicialización del controlador.
     * Se asegura de que los mensajes de error estén ocultos al arrancar.
     */
    @FXML
    public void initialize() {
        lblError.setVisible(false);
    }

    /**
     * Maneja el evento de clic en el botón "ENTRAR".
     *
     * Flujo de ejecución:
     * Limpia estilos de error previos.
     * Valida que los campos no estén vacíos.
     * Valida el formato del email.
     * Consulta la BD para obtener el usuario (y su hash).
     * Verifica la contraseña usando BCrypt.
     * Si es correcto, navega al Dashboard.
     *
     * @param event Evento del botón.
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

        if (!Validaciones.esEmailValido(email)) {
            marcarCampoError(txtEmail);
            mostrarMensajeError("El formato del email no es correcto.");
            return;
        }

        intentarLogin(email, password);
    }

    /**
     * Realiza la comprobación de credenciales contra la base de datos.
     */
    private void intentarLogin(String email, String password) {
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
            mostrarMensajeError("No existe ninguna cuenta con ese email.");
        }
    }

    /**
     * Transición a la pantalla principal (Dashboard) tras un login exitoso.
     *
     * @param usuario El usuario autenticado que se pasará al MainController.
     */
    private void loginExitoso(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/mainView.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.setUsuario(usuario);

            Stage stage = (Stage) txtEmail.getScene().getWindow();

            stage.setResizable(true);
            stage.setMinWidth(1000);
            stage.setMinHeight(700);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("SubTracker - Dashboard de " + usuario.getNombre());

            stage.centerOnScreen();
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Dialogos.mostrarError("Error Crítico", "No se pudo cargar el Dashboard.", null);
        }
    }

    /**
     * Navega a la vista de registro.
     */
    @FXML
    private void irARegistro(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/registroView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 750, 700);

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
}
