package org.dam.fcojavier.substracker.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.dam.fcojavier.substracker.dao.UsuarioDAO;
import javafx.fxml.FXML;
import org.dam.fcojavier.substracker.model.Usuario;
import org.dam.fcojavier.substracker.utils.Dialogos;
import org.dam.fcojavier.substracker.utils.PasswordUtilidades;
import org.dam.fcojavier.substracker.utils.Validaciones;

import javafx.event.ActionEvent;
import org.dam.fcojavier.substracker.utils.connection.ConnectionDB;

import java.io.IOException;

/**
 * Controlador de la pantalla de Inicio de Sesión (Login).
 *
 * Esta clase gestiona el punto de entrada a la aplicación. Sus responsabilidades son:
 *
 * Autenticar a los usuarios verificando email y contraseña (Hash).
 * Gestionar la selección del entorno de persistencia (MySQL vs SQLite).
 * Dirigir al usuario al Dashboard principal o al formulario de registro.
 *
 * @author Fco Javier García
 * @version 2.0
 */
public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;
    @FXML private ComboBox<String> comboDB;

    private final UsuarioDAO usuarioDAO;

    /**
     * Constructor por defecto.
     * Inicializa el DAO de usuarios para realizar las consultas.
     */
    public LoginController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Inicialización del controlador tras la carga del FXML.
     *
     * Configura el estado inicial de la vista:
     * Oculta los mensajes de error.
     * Puebla el selector de base de datos con las opciones disponibles.
     */
    @FXML
    public void initialize() {
        lblError.setVisible(false);
        comboDB.getItems().addAll("Nube (MySQL)", "Local (SQLite)");
        comboDB.getSelectionModel().selectFirst();
    }

    /**
     * Procesa el intento de inicio de sesión al pulsar el botón "ENTRAR".
     *
     * Flujo de ejecución:
     *
     * Configura la conexión a la BD seleccionada.
     * Verifica que la conexión esté operativa.
     * Realiza validaciones de campos vacíos y formato de email.
     * Delega la autenticación al método {@link #intentarLogin(String, String)}.
     *
     * @param event Evento del botón.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        configurarConexionSeleccionada();

        if (ConnectionDB.getConnection() == null) {
            mostrarMensajeError("No se pudo conectar a la base de datos seleccionada.");
            return;
        }

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
     * Realiza la búsqueda del usuario y la verificación de credenciales.
     *
     * @param email Email introducido.
     * @param password Contraseña en texto plano.
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
     * Configura la ventana principal con el tamaño adecuado y pasa el usuario logueado.
     *
     * @param usuario El usuario autenticado.
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
     * Navega a la vista de registro de nuevo usuario.
     * Se asegura de configurar la base de datos seleccionada antes de cambiar de pantalla.
     */
    @FXML
    private void irARegistro(ActionEvent event) {
        configurarConexionSeleccionada();

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

    /**
     * Lee el ComboBox y establece el tipo de base de datos en el Singleton de conexión.
     */
    private void configurarConexionSeleccionada() {
        if (comboDB.getSelectionModel().getSelectedIndex() == 0) {
            ConnectionDB.setTipo(ConnectionDB.DBType.MYSQL);
        } else {
            ConnectionDB.setTipo(ConnectionDB.DBType.SQLITE);
        }
    }

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
