package org.dam.fcojavier.substracker.controller;

import org.dam.fcojavier.substracker.dao.UsuarioDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.dam.fcojavier.substracker.model.Usuario;
import org.dam.fcojavier.substracker.utils.Dialogos;
import org.dam.fcojavier.substracker.utils.PasswordUtilidades;
import org.dam.fcojavier.substracker.utils.Validaciones;

import java.util.Optional;

/**
 * Controlador para la pantalla de Configuración / Perfil de Usuario.
 *
 * Gestiona la modificación de datos personales del usuario logueado.
 * Permite:
 * Actualizar nombre, apellidos y email.
 * Cambiar la contraseña de forma segura (hashing).
 * Eliminar la cuenta permanentemente (Zona de Peligro).
 *
 * @author Fco Javier García
 * @version 2.0
 */
public class ConfigController {
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtEmail;

    @FXML private PasswordField txtNuevaPass;
    @FXML private PasswordField txtConfirmPass;

    private UsuarioDAO usuarioDAO;
    private Usuario usuarioLogueado;
    private MainController mainController;

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

                mainController.cerrarSesion(new ActionEvent(txtNombre, null));
            } else {
                Dialogos.mostrarError("Error", "No se pudo eliminar la cuenta.", stage);
            }
        }
    }
}
