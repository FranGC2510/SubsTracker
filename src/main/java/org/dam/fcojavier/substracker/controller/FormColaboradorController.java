package org.dam.fcojavier.substracker.controller;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.dam.fcojavier.substracker.dao.ParticipaDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.dam.fcojavier.substracker.model.enums.MetodoPago;
import org.dam.fcojavier.substracker.model.Participa;
import org.dam.fcojavier.substracker.model.Suscripcion;
import org.dam.fcojavier.substracker.utils.Validaciones;

import java.time.LocalDate;

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

    public void setSuscripcion(Suscripcion sub) {
        this.suscripcionActual = sub;
    }

    @FXML
    private void guardar() {
        String nombre = txtNombre.getText();
        String descripcion = txtDescripcion.getText();
        String importeStr = txtImporte.getText();

        if (!Validaciones.esTextoValido(nombre)) { mostrarError("Falta nombre"); return; }

        double importe = 0;
        try { importe = Double.parseDouble(importeStr.replace(",", ".")); }
        catch (Exception e) { mostrarError("Importe mal"); return; }

        LocalDate fechaFinal = chkPagado.isSelected() ? dpFechaPago.getValue() : null;
        int periodos = chkPagado.isSelected() ? spinnerPeriodos.getValue() : 1;

        if (participaEditando == null) {
            Participa p = new Participa(nombre, importe, suscripcionActual, comboMetodo.getValue(), fechaFinal);
            p.setDescripcion(descripcion);
            p.setPeriodos_cubiertos(periodos);

            if (participaDAO.create(p)) exito();
            else mostrarError("Error al crear.");

        } else {
            participaEditando.setFecha_pagado(fechaFinal);
            participaEditando.setPeriodos_cubiertos(periodos);

            if (participaDAO.update(participaEditando)) exito();
            else mostrarError("Error al actualizar.");
        }
    }

    @FXML private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    @FXML
    private void eliminar() {
        if (participaEditando == null) return;

        if (participaDAO.delete(participaEditando.getIdParticipa())) {
            exito();
        } else {
            mostrarError("No se pudo eliminar.");
        }
    }

    private void exito() {
        guardado = true;
        cerrar();
    }

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

    private void mostrarError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }

    public boolean isGuardado() { return guardado; }
}

