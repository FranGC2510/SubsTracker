package org.dam.fcojavier.substracker.controller;

import org.dam.fcojavier.substracker.dao.SuscripcionDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.dam.fcojavier.substracker.model.enums.Categoria;
import org.dam.fcojavier.substracker.model.enums.Ciclo;
import org.dam.fcojavier.substracker.model.Suscripcion;
import org.dam.fcojavier.substracker.model.Usuario;
import org.dam.fcojavier.substracker.utils.Validaciones;

import java.time.LocalDate;

/**
 * Controlador para la ventana modal de creación de una nueva suscripción.
 *
 * Gestiona el formulario donde el usuario introduce los datos de un nuevo servicio.
 * Se encarga de validar la entrada, calcular fechas y persistir la nueva suscripción
 * en la base de datos a través del DAO.
 *
 * @author Fco Javier García
 * @version 1.0
 */
public class FormSuscripcionController {
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<Ciclo> comboCiclo;
    @FXML private ComboBox<Categoria> comboCategoria;
    @FXML private DatePicker dpFechaActivacion;
    @FXML private DatePicker dpFechaInicio;
    @FXML private Label lblError;

    private Usuario usuarioTitular;
    private SuscripcionDAO suscripcionDAO;

    private boolean guardadoExitoso = false;

    /**
     * Constructor de la clase. Inicializa el DAO de suscripciones.
     */
    public FormSuscripcionController() {
        this.suscripcionDAO = new SuscripcionDAO();
    }

    /**
     * Inicializa los componentes de la interfaz tras la carga del FXML.
     *
     * Configura:
     * Los valores de los ComboBox (Ciclos y Categorías).
     * Las selecciones por defecto (Mensual, Ocio).
     * Las fechas por defecto (día actual).
     */
    @FXML
    public void initialize() {
        comboCiclo.getItems().setAll(Ciclo.values());
        comboCategoria.getItems().setAll(Categoria.values());

        comboCiclo.getSelectionModel().select(Ciclo.MENSUAL);
        comboCategoria.getSelectionModel().select(Categoria.OCIO);
        dpFechaActivacion.setValue(LocalDate.now());
        dpFechaInicio.setValue(LocalDate.now());
    }

    /**
     * Recibe el usuario que está creando la suscripción.
     *
     * Es necesario llamar a este método antes de mostrar la ventana para
     * asignar correctamente el titular.
     *
     * @param usuario El usuario logueado.
     */
    public void setUsuario(Usuario usuario) {
        this.usuarioTitular = usuario;
    }

    /**
     * Acción del botón "Guardar".
     *
     * Realiza el flujo completo de creación:
     * Recoge los datos del formulario.
     * Valida campos obligatorios y formatos numéricos.
     * Valida la lógica de fechas (el pago no puede ser anterior a la activación).
     * Calcula la fecha de renovación.
     * Instancia el objeto {@link Suscripcion} y llama al DAO.
     *
     * @param event Evento del botón.
     */
    @FXML
    private void guardarSuscripcion(ActionEvent event) {
        String nombre = txtNombre.getText();
        String precioStr = txtPrecio.getText();
        Ciclo ciclo = comboCiclo.getValue();
        Categoria categoria = comboCategoria.getValue();
        LocalDate fechaActivacion = dpFechaActivacion.getValue();
        LocalDate fechaPrimerPago = dpFechaInicio.getValue();

        if (!Validaciones.esTextoValido(nombre)) {
            mostrarError("El nombre del servicio es obligatorio.");
            return;
        }

        if (!Validaciones.esTextoValido(precioStr)) {
            mostrarError("Debes indicar un precio.");
            return;
        }

        double precio = 0;
        try {
            precio = Double.parseDouble(precioStr.replace(",", "."));
            if (!Validaciones.esPositivo(precio)) {
                mostrarError("El precio debe ser mayor que 0.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarError("El precio debe ser un número válido.");
            return;
        }

        if (fechaPrimerPago == null) {
            mostrarError("Selecciona una fecha de inicio.");
            return;
        }

        if (fechaPrimerPago.isBefore(fechaActivacion)) {
            mostrarError("El pago no puede ser anterior a la fecha de activación.");
            // Efecto visual: marcamos el DatePicker de pago
            dpFechaInicio.setStyle("-fx-border-color: #e74c3c;");
            return;
        } else {
            dpFechaInicio.setStyle(""); // Limpiar estilo si pasa
        }

        LocalDate fechaRenovacion = fechaPrimerPago;
        LocalDate hoy = LocalDate.now();

        while (fechaRenovacion.isBefore(hoy)) {
            fechaRenovacion = calcularProximaFecha(fechaRenovacion, ciclo);
        }

        Suscripcion nueva = new Suscripcion(0, nombre, precio, ciclo, categoria, fechaActivacion, fechaRenovacion, usuarioTitular);

        if (suscripcionDAO.create(nueva)) {
            System.out.println("Suscripción guardada: " + nueva.getNombre());
            guardadoExitoso = true;
            cerrarVentana();
        } else {
            mostrarError("Error al guardar en base de datos.");
        }
    }

    /**
     * Calcula la siguiente fecha de cobro basada en una fecha inicial y un ciclo.
     *
     * @param inicio Fecha de referencia.
     * @param ciclo Ciclo de facturación (MENSUAL, ANUAL, etc.).
     * @return La fecha calculada sumando el periodo correspondiente.
     */
    private LocalDate calcularProximaFecha(LocalDate inicio, Ciclo ciclo) {
        switch (ciclo) {
            case MENSUAL: return inicio.plusMonths(1);
            case TRIMESTRAL: return inicio.plusMonths(3);
            case ANUAL: return inicio.plusYears(1);
            default: return inicio.plusMonths(1);
        }
    }

    /**
     * Cierra la ventana modal actual.
     */
    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }

    /**
     * Muestra un mensaje de error en la interfaz.
     * @param msg Texto del error.
     */
    private void mostrarError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }

    /**
     * Permite a la ventana principal saber si se creó una suscripción.
     * Útil para decidir si refrescar la tabla al cerrar el modal.
     *
     * @return true si la operación de guardado fue exitosa.
     */
    public boolean isGuardadoExitoso() {
        return guardadoExitoso;
    }
}
