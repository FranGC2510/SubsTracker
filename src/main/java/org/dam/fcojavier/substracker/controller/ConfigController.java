package org.dam.fcojavier.substracker.controller;

import javafx.scene.control.*;
import org.dam.fcojavier.substracker.dao.UsuarioDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import org.dam.fcojavier.substracker.model.Usuario;
import org.dam.fcojavier.substracker.utils.Dialogos;
import org.dam.fcojavier.substracker.utils.PasswordUtilidades;
import org.dam.fcojavier.substracker.utils.Validaciones;
import org.dam.fcojavier.substracker.utils.connection.ConnectionDB;

import java.util.Optional;

/**
 * Controlador para la pantalla de Configuración / Perfil de Usuario.
 *
 * Gestiona la modificación de datos personales y preferencias.
 * Características:
 *
 * Edición de perfil (Nombre, Email) con detección de cambios en tiempo real.
 * Cambio de contraseña seguro.
 * Visualización del estado de conexión (Nube vs Local).
 * Eliminación de cuenta (Zona de Peligro).
 *
 * @author Fco Javier García
 * @version 2.0
 */
public class ConfigController {
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtEmail;
    @FXML private Button btnGuardarDatos;

    @FXML private PasswordField txtNuevaPass;
    @FXML private PasswordField txtConfirmPass;
    @FXML private Button btnActualizarPass;

    @FXML private Label lblIconoBD;
    @FXML private Label lblNombreBD;

    private UsuarioDAO usuarioDAO;
    private Usuario usuarioLogueado;
    private MainController mainController;

    private String nombreOriginal;
    private String apellidoOriginal;
    private String emailOriginal;

    /**
     * Constructor por defecto. Inicializa el DAO de usuarios.
     */
    public ConfigController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Inicializa los datos del controlador con la información del usuario actual.
     *
     * @param usuario El usuario que ha iniciado sesión.
     * @param main Referencia al controlador principal para actualizar la UI (nombre/avatar) tras los cambios.
     */
    public void initData(Usuario usuario, MainController main) {
        this.usuarioLogueado = usuario;
        this.mainController = main;

        txtNombre.setText(usuario.getNombre());
        txtApellidos.setText(usuario.getApellidos());
        txtEmail.setText(usuario.getEmail());

        this.nombreOriginal = usuario.getNombre();
        this.apellidoOriginal = usuario.getApellidos();
        this.emailOriginal = usuario.getEmail();

        configurarInfoBD();
        iniciarDetectoresDeCambio();
    }

    /**
     * Guarda los cambios de los datos personales (Nombre, Apellidos, Email).
     *
     * Nota: Este método NO actualiza la contraseña. El objeto usuario mantiene
     * su hash de contraseña actual para no perderlo al actualizar el resto de campos.
     */
    @FXML
    private void guardarDatos() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        String nombre = txtNombre.getText();
        String apellidos = txtApellidos.getText();
        String email = txtEmail.getText();

        if (!Validaciones.esTextoValido(nombre) || !Validaciones.esTextoValido(apellidos) || !Validaciones.esTextoValido(email)) {
            Dialogos.mostrarError("Error", "No puedes dejar campos vacíos.", null);
            return;
        }

        if (!Validaciones.esEmailValido(email)) {
            Dialogos.mostrarError("Error", "Formato de email inválido.", null);
            return;
        }

        usuarioLogueado.setNombre(nombre);
        usuarioLogueado.setApellidos(apellidos);
        usuarioLogueado.setEmail(email);

        if (usuarioDAO.update(usuarioLogueado)) {
            Dialogos.mostrarInformacion("Guardado", "Datos de perfil actualizados.", stage);

            if (mainController != null) {
                mainController.actualizarInfoUsuario(usuarioLogueado);
            }
            nombreOriginal = nombre;
            apellidoOriginal = apellidos;
            emailOriginal = email;
            comprobarCambiosDatos();
        } else {
            Dialogos.mostrarError("Error", "No se pudo actualizar. Puede que el email ya exista.", stage);
        }
    }

    /**
     * Gestiona el cambio de contraseña.
     *
     * Valida que las contraseñas coincidan y cumplan los requisitos de seguridad.
     * Genera un nuevo hash BCrypt antes de enviarlo al DAO.
     */
    @FXML
    private void cambiarPassword() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        String nueva = txtNuevaPass.getText();
        String confirm = txtConfirmPass.getText();

        if (!Validaciones.esTextoValido(nueva)) {
            Dialogos.mostrarError("Error", "Escribe una contraseña.", stage);
            return;
        }

        if (!nueva.equals(confirm)) {
            Dialogos.mostrarError("Error", "Las contraseñas no coinciden.", stage);
            return;
        }

        if (!Validaciones.esPasswordValida(nueva)) {
            Dialogos.mostrarError("Error", "La contraseña debe tener al menos 6 caracteres.", stage);
            return;
        }

        String passEncriptada = PasswordUtilidades.hashPassword(nueva);
        usuarioLogueado.setPassword(passEncriptada);

        if (usuarioDAO.update(usuarioLogueado)) {
            Dialogos.mostrarInformacion("Éxito", "Contraseña actualizada correctamente.", stage);
            txtNuevaPass.clear();
            txtConfirmPass.clear();
        } else {
            Dialogos.mostrarError("Error", "Error al cambiar la contraseña.", stage);
        }
    }

    /**
     * Elimina la cuenta del usuario actual tras una confirmación.
     *
     * Esta acción es irreversible y elimina en cascada todos los datos asociados
     * (suscripciones, cobros, etc.). Si tiene éxito, cierra la sesión.
     */
    @FXML
    private void eliminarCuenta() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();

        Optional<ButtonType> result = Dialogos.mostrarConfirmacion(
                "ELIMINAR CUENTA",
                "¿Estás seguro de que quieres borrar tu cuenta?",
                "Se perderán todos tus datos, suscripciones e historiales. Esta acción es irreversible.",
                stage);

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (usuarioDAO.delete(usuarioLogueado.getId_usuario())) {
                Dialogos.mostrarInformacion("Adiós", "Tu cuenta ha sido eliminada.", stage);

                if (mainController != null) {
                    mainController.cerrarSesion(new ActionEvent(txtNombre, null));
                }
            } else {
                Dialogos.mostrarError("Error", "No se pudo eliminar la cuenta.", stage);
            }
        }
    }

    /**
     * Configura listeners en los campos de texto para detectar cambios en tiempo real.
     */
    private void iniciarDetectoresDeCambio() {
        txtNombre.textProperty().addListener((obs, oldVal, newVal) -> comprobarCambiosDatos());
        txtApellidos.textProperty().addListener((obs, oldVal, newVal) -> comprobarCambiosDatos());
        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> comprobarCambiosDatos());

        txtNuevaPass.textProperty().addListener((obs, oldVal, newVal) -> comprobarCambiosPass());
        txtConfirmPass.textProperty().addListener((obs, oldVal, newVal) -> comprobarCambiosPass());
    }

    /**
     * Comprueba si los datos actuales difieren de los originales.
     * Activa o desactiva el botón de "Guardar Cambios".
     */
    private void comprobarCambiosDatos() {
        boolean hayCambios = !txtNombre.getText().equals(nombreOriginal) ||
                !txtApellidos.getText().equals(apellidoOriginal) ||
                !txtEmail.getText().equals(emailOriginal);

        activarBoton(btnGuardarDatos, hayCambios);
    }

    /**
     * Comprueba si se ha escrito algo en los campos de nueva contraseña.
     * Activa o desactiva el botón de "Actualizar Contraseña".
     */
    private void comprobarCambiosPass() {
        boolean hayTexto = !txtNuevaPass.getText().isEmpty() || !txtConfirmPass.getText().isEmpty();

        activarBoton(btnActualizarPass, hayTexto);
    }

    /**
     * Cambia el estilo visual del botón entre "Activo" (Primary) e "Inactivo" (Secondary).
     *
     * @param boton El botón a modificar.
     * @param activo true para activarlo (verde), false para desactivarlo (gris).
     */
    private void activarBoton(Button boton, boolean activo) {
        boton.setDisable(!activo);

        if (activo) {
            if (!boton.getStyleClass().contains("button-primary")) {
                boton.getStyleClass().removeAll("button-secondary");
                boton.getStyleClass().add("button-primary");
            }
        } else {
            if (!boton.getStyleClass().contains("button-secondary")) {
                boton.getStyleClass().removeAll("button-primary");
                boton.getStyleClass().add("button-secondary");
            }
        }
    }

    /**
     * Muestra visualmente qué tipo de base de datos se está utilizando.
     */
    private void configurarInfoBD() {
        ConnectionDB.DBType tipo = ConnectionDB.getTipoSeleccionado();

        if (tipo == ConnectionDB.DBType.MYSQL) {
            lblIconoBD.setText("☁️"); // Nube
            lblNombreBD.setText("Nube (MySQL)");
        } else {
            lblIconoBD.setText("📂");
            lblNombreBD.setText("Local (SQLite)");
        }
    }
}
