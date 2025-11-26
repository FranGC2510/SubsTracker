package org.dam.fcojavier.substracker.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.dam.fcojavier.substracker.dao.ParticipaDAO;
import org.dam.fcojavier.substracker.dao.SuscripcionDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.dam.fcojavier.substracker.model.Participa;
import org.dam.fcojavier.substracker.model.Usuario;
import org.dam.fcojavier.substracker.model.enums.Categoria;
import org.dam.fcojavier.substracker.model.enums.Ciclo;
import org.dam.fcojavier.substracker.model.Suscripcion;
import org.dam.fcojavier.substracker.utils.Validaciones;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
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
    @FXML private ScrollPane scrollParticipantes;
    @FXML private VBox containerParticipantes;
    @FXML private VBox panelNoColaboradores;
    @FXML private Label lblGastoNeto;
    @FXML private Label lblGastoBruto;

    private MainController mainController; // Referencia para volver
    private Usuario usuarioLogueado;

    private Suscripcion suscripcionActual;
    private SuscripcionDAO suscripcionDAO;
    private ParticipaDAO participaDAO;
    private boolean huboCambios = false; // Para avisar a la tabla padre
    private boolean modoEdicion = false; // Controla el estado

    public DetalleSuscripcionController() {
        this.suscripcionDAO = new SuscripcionDAO();
        this.participaDAO = new ParticipaDAO();
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

        txtNombre.setText(suscripcion.getNombre());
        txtPrecio.setText(String.valueOf(suscripcion.getPrecio()));
        comboCiclo.setValue(suscripcion.getCiclo());
        comboCategoria.setValue(suscripcion.getCategoria());
        dpFechaActivacion.setValue(suscripcion.getFechaActivacion());
        dpFechaRenovacion.setValue(suscripcion.getFechaRenovacion());

        actualizarEstadisticas();
        cargarParticipantes();
    }

    private void cargarParticipantes() {
        if (suscripcionActual != null) {
            List<Participa> lista = participaDAO.findBySuscripcionId(suscripcionActual.getIdSuscripcion());

            if (lista.isEmpty()) {
                scrollParticipantes.setVisible(false);
                panelNoColaboradores.setVisible(true);
            } else {
                scrollParticipantes.setVisible(true);
                panelNoColaboradores.setVisible(false);

                // Limpiar lista anterior
                containerParticipantes.getChildren().clear();

                for (Participa p : lista) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/itemColaborador.fxml"));
                        HBox tarjeta = loader.load();

                        ItemColaboradorController itemController = loader.getController();

                        itemController.setDatos(p, suscripcionActual.getCiclo(), () -> {
                            abrirModalEditarColaborador(p);
                        });

                        containerParticipantes.getChildren().add(tarjeta);

                    } catch (IOException e) { e.printStackTrace(); }
                }
            }
        }
        actualizarEstadisticas();
    }

    private void abrirModalEditarColaborador(Participa participaAEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/formColaboradorView.fxml"));
            Parent root = loader.load();

            FormColaboradorController controller = loader.getController();
            controller.setSuscripcion(suscripcionActual);

            controller.setParticipaEditar(participaAEditar);

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(scrollParticipantes.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.setMinWidth(500);
            stage.setMinHeight(350);
            stage.showAndWait();

            if (controller.isGuardado()) {
                cargarParticipantes();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actualizarEstadisticas() {
        if (suscripcionActual != null) {

            List<Participa> colaboradores = participaDAO.findBySuscripcionId(suscripcionActual.getIdSuscripcion());
            double totalAportacionesPorCiclo = 0;

            for (Participa p : colaboradores) {
                totalAportacionesPorCiclo += p.getCantidadApagar();
            }

            double precioTotalCiclo = suscripcionActual.getPrecio();
            double miCosteRealCiclo = precioTotalCiclo - totalAportacionesPorCiclo;

            long numPagos = suscripcionActual.calcularNumeroDePagos(LocalDate.now());

            double historicoBruto = precioTotalCiclo * numPagos;
            double historicoNeto = miCosteRealCiclo * numPagos;

            lblGastoNeto.setText(String.format("%.2f €", historicoNeto));
            lblGastoBruto.setText(String.format("%.2f €", historicoBruto));

            if (historicoNeto < 0) {
                lblGastoNeto.setStyle("-fx-font-size: 38; -fx-font-weight: bold; -fx-text-fill: #2ecc71;"); // Verde (Beneficio)
            } else {
                lblGastoNeto.setStyle("-fx-font-size: 38; -fx-font-weight: bold; -fx-text-fill: -fx-accent-color;"); // Turquesa (Gasto)
            }

            lblFechaInicioEstadistica.setText("Calculado sobre " + numPagos + " ciclos (" + suscripcionActual.getFechaActivacion() + ")");
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

    @FXML
    private void abrirModalColaborador(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/formColaboradorView.fxml"));
            Parent root = loader.load();

            FormColaboradorController controller = loader.getController();
            controller.setSuscripcion(suscripcionActual);

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(txtNombre.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isGuardado()) {
                cargarParticipantes(); // Refrescar la tabla
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

