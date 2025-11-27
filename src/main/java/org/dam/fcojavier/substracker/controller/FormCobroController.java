package org.dam.fcojavier.substracker.controller;

import org.dam.fcojavier.substracker.dao.CobroDAO;
import org.dam.fcojavier.substracker.dao.SuscripcionDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.dam.fcojavier.substracker.model.Cobro;
import org.dam.fcojavier.substracker.model.enums.MetodoPago;
import org.dam.fcojavier.substracker.model.Suscripcion;

import java.time.LocalDate;

public class FormCobroController {
    @FXML private Label lblNombreSuscripcion;
    @FXML private DatePicker dpFecha;
    @FXML private Spinner<Integer> spinnerPeriodos;
    @FXML private ComboBox<MetodoPago> comboMetodo;
    @FXML private TextField txtDescripcion;
    @FXML private Label lblError;

    private Suscripcion suscripcionActual;
    private CobroDAO cobroDAO;
    private SuscripcionDAO suscripcionDAO;
    private boolean guardadoExitoso = false;

    public void initialize() {
        cobroDAO = new CobroDAO();
        suscripcionDAO = new SuscripcionDAO();

        comboMetodo.getItems().setAll(MetodoPago.values());
        comboMetodo.getSelectionModel().select(MetodoPago.TARJETA); // Defecto
        dpFecha.setValue(LocalDate.now());

        spinnerPeriodos.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 24, 1));
    }

    public void setSuscripcion(Suscripcion sub) {
        this.suscripcionActual = sub;
        lblNombreSuscripcion.setText(sub.getNombre() + " (" + sub.getPrecio() + " €)");
    }

    @FXML
    private void guardar() {
        if (dpFecha.getValue() == null) {
            mostrarError("Selecciona una fecha.");
            return;
        }

        Cobro nuevoCobro = new Cobro();
        nuevoCobro.setSuscripcion(suscripcionActual);
        nuevoCobro.setFecha_cobro(dpFecha.getValue());
        nuevoCobro.setMetodo_pago(comboMetodo.getValue());
        nuevoCobro.setPeriodos_cubiertos(spinnerPeriodos.getValue());
        nuevoCobro.setDescripcion(txtDescripcion.getText());

        if (cobroDAO.create(nuevoCobro)) {

            actualizarFechaRenovacionSuscripcion();

            guardadoExitoso = true;
            cerrar();
        } else {
            mostrarError("Error al registrar el pago.");
        }
    }

    /**
     * Avanza la fecha de renovación de la suscripción según los periodos pagados.
     */
    private void actualizarFechaRenovacionSuscripcion() {
        int periodos = spinnerPeriodos.getValue();
        LocalDate viejaRenovacion = suscripcionActual.getFechaRenovacion();
        LocalDate nuevaRenovacion = viejaRenovacion; // Inicializar

        switch (suscripcionActual.getCiclo()) {
            case MENSUAL: nuevaRenovacion = viejaRenovacion.plusMonths(periodos); break;
            case TRIMESTRAL: nuevaRenovacion = viejaRenovacion.plusMonths(periodos * 3L); break;
            case ANUAL: nuevaRenovacion = viejaRenovacion.plusYears(periodos); break;
        }

        suscripcionActual.setFechaRenovacion(nuevaRenovacion);
        suscripcionDAO.update(suscripcionActual);
        System.out.println("Suscripción renovada hasta: " + nuevaRenovacion);
    }

    @FXML private void cerrar() {
        ((Stage) lblNombreSuscripcion.getScene().getWindow()).close();
    }

    private void mostrarError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }

    public boolean isGuardadoExitoso() { return guardadoExitoso; }
}

