package org.dam.fcojavier.substracker.controller;

import org.dam.fcojavier.substracker.dao.SuscripcionDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.dam.fcojavier.substracker.model.Usuario;
import org.dam.fcojavier.substracker.model.enums.Categoria;
import org.dam.fcojavier.substracker.model.enums.Ciclo;
import org.dam.fcojavier.substracker.model.Suscripcion;
import org.dam.fcojavier.substracker.utils.Validaciones;

import java.time.LocalDate;
import java.util.Optional;

public class DetalleSuscripcionController {
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<Ciclo> comboCiclo;
    @FXML private ComboBox<Categoria> comboCategoria;
    @FXML private DatePicker dpFechaActivacion;
    @FXML private DatePicker dpFechaRenovacion;
    @FXML private Label lblError;
    @FXML private Button btnEditarGuardar;
    @FXML private Label lblTituloDetalle;
    @FXML private Label lblTotalGastado;
    @FXML private Label lblFechaInicioEstadistica;

    private MainController mainController; // Referencia para volver
    private Usuario usuarioLogueado;

    private Suscripcion suscripcionActual;
    private SuscripcionDAO suscripcionDAO;
    private boolean huboCambios = false; // Para avisar a la tabla padre
    private boolean modoEdicion = false; // Controla el estado

    public DetalleSuscripcionController() {
        this.suscripcionDAO = new SuscripcionDAO();
    }

    @FXML
    public void initialize() {
        comboCiclo.getItems().setAll(Ciclo.values());
        comboCategoria.getItems().setAll(Categoria.values());
    }

    // Método clave: Recibe los datos al abrir la ventana
    public void initData(Suscripcion suscripcion, Usuario usuario, MainController main) {
        this.suscripcionActual = suscripcion;
        this.usuarioLogueado = usuario;
        this.mainController = main;

        // Rellenar campos
        txtNombre.setText(suscripcion.getNombre());
        txtPrecio.setText(String.valueOf(suscripcion.getPrecio()));
        comboCiclo.setValue(suscripcion.getCiclo());
        comboCategoria.setValue(suscripcion.getCategoria());
        dpFechaActivacion.setValue(suscripcion.getFechaActivacion());
        dpFechaRenovacion.setValue(suscripcion.getFechaRenovacion());

        actualizarEstadisticas();
    }

    private void actualizarEstadisticas() {
        if (suscripcionActual != null) {
            // 1. Calculamos el total usando el método del modelo
            double total = suscripcionActual.calcularGastoTotal(LocalDate.now());

            // 2. Formateamos a 2 decimales con símbolo €
            lblTotalGastado.setText(String.format("%.2f €", total));

            // 3. Mostramos la fecha de inicio formateada
            lblFechaInicioEstadistica.setText("Desde el " + suscripcionActual.getFechaActivacion().toString());
        }
    }

    @FXML
    private void volverAtras(ActionEvent event) {
        if (mainController != null) {
            mainController.cargarVistaSuscripciones();
        }
    }

    @FXML
    private void toggleEdicion(ActionEvent event) {
        if (!modoEdicion) {
            habilitarCampos(true);
            btnEditarGuardar.setText("Guardar Cambios");
            btnEditarGuardar.setStyle("-fx-background-color: #2ecc71;"); // Cambiar a verde
            modoEdicion = true;
        } else {
            guardarCambios();
        }
    }

    private void guardarCambios() {
        String nombre = txtNombre.getText();
        String precioStr = txtPrecio.getText();

        if (!Validaciones.esTextoValido(nombre) || !Validaciones.esTextoValido(precioStr)) {
            mostrarError("Revisa los campos vacíos.");
            return;
        }

        double precio = Double.parseDouble(precioStr.replace(",", "."));

        suscripcionActual.setNombre(nombre);
        suscripcionActual.setPrecio(precio);
        suscripcionActual.setCiclo(comboCiclo.getValue());
        suscripcionActual.setCategoria(comboCategoria.getValue());
        suscripcionActual.setFechaActivacion(dpFechaActivacion.getValue());
        suscripcionActual.setFechaRenovacion(dpFechaRenovacion.getValue());

        if (suscripcionDAO.update(suscripcionActual)) {
            System.out.println("Suscripción actualizada.");
            huboCambios = true;

            habilitarCampos(false);
            btnEditarGuardar.setText("Editar");
            btnEditarGuardar.setStyle("");
            modoEdicion = false;
            lblError.setVisible(false);
        } else {
            mostrarError("Error al actualizar en la base de datos.");
        }
    }

    @FXML
    private void eliminarSuscripcion(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar Suscripción");
        alert.setHeaderText("¿Eliminar " + suscripcionActual.getNombre() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (suscripcionDAO.delete(suscripcionActual.getIdSuscripcion())) {
                volverAtras(event);
            } else {
                mostrarError("No se pudo eliminar.");
            }
        }
    }

    private void habilitarCampos(boolean habilitar) {
        // disable = !habilitar
        boolean estado = !habilitar;
        txtNombre.setDisable(estado);
        txtPrecio.setDisable(estado);
        comboCiclo.setDisable(estado);
        comboCategoria.setDisable(estado);
        dpFechaActivacion.setDisable(estado);
        dpFechaRenovacion.setDisable(estado);
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

    public boolean huboCambios() {
        return huboCambios;
    }
}

