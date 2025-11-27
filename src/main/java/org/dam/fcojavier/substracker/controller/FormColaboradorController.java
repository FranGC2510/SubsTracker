package org.dam.fcojavier.substracker.controller;

import javafx.scene.layout.HBox;
import org.dam.fcojavier.substracker.dao.ParticipaDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.dam.fcojavier.substracker.model.enums.MetodoPago;
import org.dam.fcojavier.substracker.model.Participa;
import org.dam.fcojavier.substracker.model.Suscripcion;
import org.dam.fcojavier.substracker.utils.Validaciones;

import java.time.LocalDate;

/**
 * Controlador para la ventana modal de gestión de colaboradores (Participa).
 *
 * Esta clase maneja tanto la creación de nuevos colaboradores (invitados)
 * como la edición de los existentes.
 *
 * Características principales:
 * Gestión de estado "Pagado" vs "Pendiente" mediante CheckBox.
 * Configuración de periodos cubiertos por el pago (adelantos).
 * Validación de importes y campos obligatorios.
 * Comunicación con {@link ParticipaDAO} para persistir los cambios.
 *
 * @author Fco Javier García
 * @version 1.0
 */
public class FormColaboradorController {
    @FXML private TextField txtNombre;
    @FXML private TextField txtImporte;
    @FXML private ComboBox<MetodoPago> comboMetodo;
    @FXML private DatePicker dpFechaPago;
    @FXML private Label lblError;
    @FXML private TextField txtDescripcion;
    @FXML private CheckBox chkPagado;
    @FXML private HBox boxFecha;
    @FXML private Button btnEliminar;
    @FXML private Button btnGuardar;
    @FXML private Spinner<Integer> spinnerPeriodos;

    private Suscripcion suscripcionActual;
    private ParticipaDAO participaDAO;

    private boolean guardado = false;

    private Participa participaEditando;

    /**
     * Inicializa el controlador y configura los componentes de la interfaz.
     *
     * Se ejecuta automáticamente tras cargar el FXML. Configura:
     * El DAO.
     * Los valores del ComboBox de métodos de pago.
     * El Spinner de periodos (Mínimo 1, Máximo 24).
     * Los Bindings: Deshabilita la fecha y el spinner si el checkbox "Pagado" no está marcado.
     * Listeners visuales para la opacidad.
     */
    public void initialize() {
        participaDAO = new ParticipaDAO();
        comboMetodo.getItems().setAll(MetodoPago.values());
        comboMetodo.getSelectionModel().select(MetodoPago.BIZUM); // Defecto
        dpFechaPago.setValue(LocalDate.now());

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 24, 1);
        spinnerPeriodos.setValueFactory(valueFactory);

        dpFechaPago.disableProperty().bind(chkPagado.selectedProperty().not());
        spinnerPeriodos.disableProperty().bind(chkPagado.selectedProperty().not()); // Deshabilitar si no paga

        chkPagado.selectedProperty().addListener((obs, oldVal, newVal) -> {
            boxFecha.setOpacity(newVal ? 1.0 : 0.5);
        });
    }

    /**
     * Establece la suscripción a la que se va a añadir el colaborador.
     * Necesario solo en el modo "Crear".
     *
     * @param sub Objeto {@link Suscripcion} padre.
     */
    public void setSuscripcion(Suscripcion sub) {
        this.suscripcionActual = sub;
    }

    /**
     * Configura el formulario en Modo Edición.
     *
     * Rellena todos los campos con los datos del colaborador existente y habilita
     * el botón de eliminar.
     *
     * @param p El objeto {@link Participa} que se desea editar.
     */
    public void setParticipaEditar(Participa p) {
        this.participaEditando = p;

        btnGuardar.setText("Guardar Cambios");
        btnEliminar.setVisible(true);

        if (p.getParticipante() != null) {
            txtNombre.setText(p.getParticipante().getNombre());
            txtNombre.setDisable(true);
        } else {
            txtNombre.setText(p.getNombreInvitado());
        }

        txtImporte.setText(String.valueOf(p.getCantidadApagar()));
        txtDescripcion.setText(p.getDescripcion());
        comboMetodo.setValue(p.getMetodo_pago());

        if (p.getPeriodos_cubiertos() > 0) {
            spinnerPeriodos.getValueFactory().setValue(p.getPeriodos_cubiertos());
        }

        if (p.getFecha_pagado() != null) {
            chkPagado.setSelected(true);
            dpFechaPago.setValue(p.getFecha_pagado());
        } else {
            chkPagado.setSelected(false);
            dpFechaPago.setValue(LocalDate.now());
        }
    }

    /**
     * Acción del botón Guardar.
     *
     * Valida los datos de entrada y decide si llamar a {@code create} o {@code update}
     * en el DAO dependiendo del estado del formulario.
     *
     */
    @FXML
    private void guardar() {
        String nombre = txtNombre.getText();
        String descripcion = txtDescripcion.getText();
        String importeStr = txtImporte.getText();

        if (!Validaciones.esTextoValido(nombre)) { mostrarError("Falta nombre"); return; }

        double importe = 0;
        try {
            importe = Double.parseDouble(importeStr.replace(",", "."));
        } catch (Exception e) {
            mostrarError("Importe mal");
            return;
        }

        LocalDate fechaFinal = chkPagado.isSelected() ? dpFechaPago.getValue() : null;
        int periodos = chkPagado.isSelected() ? spinnerPeriodos.getValue() : 1;

        if (participaEditando == null) {
            Participa p = new Participa(nombre, importe, suscripcionActual, comboMetodo.getValue(), fechaFinal);
            p.setDescripcion(descripcion);
            p.setPeriodos_cubiertos(periodos);

            if (participaDAO.create(p)) exito();
            else mostrarError("Error al crear.");

        } else {
            if (participaEditando.getParticipante() == null) {
                participaEditando.setNombreInvitado(nombre);
            }

            participaEditando.setCantidadApagar(importe);
            participaEditando.setDescripcion(descripcion);
            participaEditando.setMetodo_pago(comboMetodo.getValue());
            participaEditando.setFecha_pagado(fechaFinal);
            participaEditando.setPeriodos_cubiertos(periodos);

            if (participaDAO.update(participaEditando)) exito();
            else mostrarError("Error al actualizar.");
        }
    }

    /**
     * Cierra la ventana modal actual sin realizar cambios (Botón Cancelar).
     */
    @FXML private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    /**
     * Acción del botón Eliminar.
     * Solo visible en modo edición. Borra el registro de la base de datos.
     */
    @FXML
    private void eliminar() {
        if (participaEditando == null) return;

        if (participaDAO.delete(participaEditando.getIdParticipa())) {
            exito();
        } else {
            mostrarError("No se pudo eliminar.");
        }
    }

    // MÉTODOS PRIVADOS

    /**
     * Marca la operación como exitosa y cierra la ventana.
     * Esto permite a la ventana padre saber que debe refrescar la tabla.
     */
    private void exito() {
        guardado = true;
        cerrar();
    }

    private void mostrarError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }

    /**
     * Permite a la vista padre consultar si se realizaron cambios.
     * @return true si se creó, editó o eliminó un registro.
     */
    public boolean isGuardado() { return guardado; }
}

