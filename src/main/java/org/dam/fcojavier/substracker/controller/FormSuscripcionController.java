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

    public FormSuscripcionController() {
        this.suscripcionDAO = new SuscripcionDAO();
    }

    @FXML
    public void initialize() {
        comboCiclo.getItems().setAll(Ciclo.values());
        comboCategoria.getItems().setAll(Categoria.values());

        comboCiclo.getSelectionModel().select(Ciclo.MENSUAL);
        comboCategoria.getSelectionModel().select(Categoria.OCIO);
        dpFechaActivacion.setValue(LocalDate.now());
        dpFechaInicio.setValue(LocalDate.now());
    }

    // Método para recibir el usuario desde la ventana anterior
    public void setUsuario(Usuario usuario) {
        this.usuarioTitular = usuario;
    }

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

        LocalDate fechaRenovacion = calcularProximaFecha(fechaPrimerPago, ciclo);

        Suscripcion nueva = new Suscripcion(0, nombre, precio, ciclo, categoria, fechaActivacion, fechaPrimerPago, usuarioTitular);

        if (suscripcionDAO.create(nueva)) {
            System.out.println("Suscripción guardada: " + nueva.getNombre());
            guardadoExitoso = true;
            cerrarVentana();
        } else {
            mostrarError("Error al guardar en base de datos.");
        }
    }

    private LocalDate calcularProximaFecha(LocalDate inicio, Ciclo ciclo) {
        switch (ciclo) {
            case MENSUAL: return inicio.plusMonths(1);
            case TRIMESTRAL: return inicio.plusMonths(3);
            case ANUAL: return inicio.plusYears(1);
            default: return inicio.plusMonths(1);
        }
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }

    // Método para que la ventana padre sepa si tiene que recargar la tabla
    public boolean isGuardadoExitoso() {
        return guardadoExitoso;
    }
}
