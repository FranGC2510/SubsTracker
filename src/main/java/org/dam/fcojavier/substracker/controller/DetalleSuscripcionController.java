package org.dam.fcojavier.substracker.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import org.dam.fcojavier.substracker.utils.Dialogos;
import org.dam.fcojavier.substracker.utils.Validaciones;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de la vista detallada de una suscripción.
 *
 * Gestiona la pantalla "Dashboard" individual de un servicio, permitiendo:
 * Visualizar y editar los datos principales (nombre, precio, fechas).
 * Ver estadísticas financieras en tiempo real (Gasto Bruto vs Neto).
 * Gestionar la lista de colaboradores mediante un sistema de tarjetas dinámicas.
 * Eliminar la suscripción completa.
 *
 * @author Fco Javier García
 * @version 2.0 (Con estadísticas financieras)
 */
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
    @FXML private Label lblFechaInicioEstadistica;
    @FXML private ScrollPane scrollParticipantes;
    @FXML private VBox containerParticipantes;
    @FXML private VBox panelNoColaboradores;
    @FXML private Label lblGastoNeto;
    @FXML private Label lblGastoBruto;
    @FXML private CheckBox chkActivo;

    private MainController mainController;
    private Usuario usuarioLogueado;

    private Suscripcion suscripcionActual;
    private SuscripcionDAO suscripcionDAO;
    private ParticipaDAO participaDAO;
    private boolean huboCambios = false;
    private boolean modoEdicion = false;

    /**
     * Constructor por defecto. Inicializa los DAOs.
     */
    public DetalleSuscripcionController() {
        this.suscripcionDAO = new SuscripcionDAO();
        this.participaDAO = new ParticipaDAO();
    }

    /**
     * Configuración inicial de los componentes visuales.
     * Carga los valores de los enumerados en los ComboBoxes.
     */
    @FXML
    public void initialize() {
        comboCiclo.getItems().setAll(Ciclo.values());
        comboCategoria.getItems().setAll(Categoria.values());
        chkActivo.selectedProperty().addListener((obs, estabaActivo, ahoraEstaActivo) -> {
            actualizarEstiloEstado(ahoraEstaActivo);
        });
    }

    /**
     * Método principal de carga de datos (Inyección de Dependencias).
     *
     * Se llama desde el {@link MainController} cuando el usuario hace doble clic en la tabla.
     *
     * @param suscripcion El objeto {@link Suscripcion} seleccionado.
     * @param usuario El usuario logueado actualmente.
     * @param main Referencia al controlador principal para poder navegar "Atrás".
     */
    public void initData(Suscripcion suscripcion, Usuario usuario, MainController main) {
        this.suscripcionActual = suscripcion;
        this.usuarioLogueado = usuario;
        this.mainController = main;

        lblTituloDetalle.setText(suscripcion.getNombre());

        txtNombre.setText(suscripcion.getNombre());
        txtPrecio.setText(String.valueOf(suscripcion.getPrecio()));
        comboCiclo.setValue(suscripcion.getCiclo());
        comboCategoria.setValue(suscripcion.getCategoria());
        dpFechaActivacion.setValue(suscripcion.getFechaActivacion());
        dpFechaRenovacion.setValue(suscripcion.getFechaRenovacion());
        chkActivo.setSelected(suscripcion.isActivo());

        actualizarEstiloEstado(suscripcion.isActivo());
        actualizarEstadisticas();
        cargarParticipantes();
    }

    private void actualizarEstiloEstado(boolean activo) {
        if (activo) {
            chkActivo.setText("SUSCRIPCIÓN ACTIVA");
            chkActivo.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px; -fx-opacity: 1.0;");
        } else {
            chkActivo.setText("SUSCRIPCIÓN PAUSADA");
            chkActivo.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14px; -fx-opacity: 1.0;");
        }
    }

    /**
     * Carga la lista de colaboradores desde la base de datos y genera las tarjetas visuales.
     *
     * Utiliza un {@link VBox} dinámico para insertar una tarjeta {@code itemColaborador.fxml}
     * por cada registro encontrado.
     */
    private void cargarParticipantes() {
        if (suscripcionActual != null) {
            List<Participa> lista = participaDAO.findBySuscripcionId(suscripcionActual.getIdSuscripcion());

            if (lista.isEmpty()) {
                scrollParticipantes.setVisible(false);
                panelNoColaboradores.setVisible(true);
            } else {
                scrollParticipantes.setVisible(true);
                panelNoColaboradores.setVisible(false);

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

    /**
     * Abre la ventana modal para editar un colaborador existente.
     */
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

    /**
     * Calcula y muestra los datos financieros en la tarjeta KPI.
     *
     * Algoritmo:
     * Calcula el coste total histórico del servicio (Bruto).
     * Suma todas las aportaciones REALES recibidas de colaboradores (teniendo en cuenta periodos pagados).
     * Resta las aportaciones al bruto para obtener el Gasto Neto del usuario.
     */
    private void actualizarEstadisticas() {
        if (suscripcionActual != null) {

            long numPagosTranscurridos = suscripcionActual.calcularNumeroDePagos(LocalDate.now());
            double precioTotalCiclo = suscripcionActual.getPrecio();
            double historicoBruto = precioTotalCiclo * numPagosTranscurridos;

            List<Participa> colaboradores = participaDAO.findBySuscripcionId(suscripcionActual.getIdSuscripcion());
            double totalDineroRecibido = 0;

            for (Participa p : colaboradores) {
                if (p.getFecha_pagado() != null) {
                    double aportePersona = p.getCantidadApagar() * p.getPeriodos_cubiertos();
                    totalDineroRecibido += aportePersona;
                }
            }

            double historicoNeto = historicoBruto - totalDineroRecibido;

            lblGastoNeto.setText(String.format("%.2f €", historicoNeto));
            lblGastoBruto.setText(String.format("%.2f €", historicoBruto));

            if (historicoNeto < 0) {
                lblGastoNeto.setStyle("-fx-font-size: 38; -fx-font-weight: bold; -fx-text-fill: #2ecc71;");
            } else {
                lblGastoNeto.setStyle("-fx-font-size: 38; -fx-font-weight: bold; -fx-text-fill: -fx-accent-color;");
            }

            lblFechaInicioEstadistica.setText("Calculado sobre " + numPagosTranscurridos + " ciclos (" + suscripcionActual.getFechaActivacion() + ")");
        }
    }

    /**
     * Navega de vuelta a la lista principal de suscripciones.
     */
    @FXML
    private void volverAtras(ActionEvent event) {
        if (mainController != null) {
            mainController.cargarVistaSuscripciones();
        }
    }

    /**
     * Alterna entre el modo "Solo Lectura" y "Edición".
     * Si ya está en edición, guarda los cambios.
     */
    @FXML
    private void toggleEdicion(ActionEvent event) {
        if (!modoEdicion) {
            habilitarCampos(true);
            btnEditarGuardar.setText("Guardar Cambios");
            btnEditarGuardar.setStyle("-fx-background-color: #2ecc71;");
            modoEdicion = true;
        } else {
            guardarCambios();
        }
    }

    /**
     * Recoge los datos del formulario, valida y actualiza la suscripción en la BD.
     */
    private void guardarCambios() {
        String nombre = txtNombre.getText();
        String precioStr = txtPrecio.getText();

        if (!Validaciones.esTextoValido(nombre) || !Validaciones.esTextoValido(precioStr)) {
            mostrarError("Revisa los campos vacíos.");
            return;
        }

        suscripcionActual.setActivo(chkActivo.isSelected());
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

    /**
     * Elimina la suscripción actual tras confirmación.
     * Si tiene éxito, vuelve a la lista automáticamente.
     */
    @FXML
    private void eliminarSuscripcion(ActionEvent event) {
        Stage ventanaActual = (Stage) btnEditarGuardar.getScene().getWindow();

        Optional<ButtonType> result = Dialogos.mostrarConfirmacion(
                "Eliminar Suscripción",
                "¿Estás seguro de eliminar " + suscripcionActual.getNombre() + "?",
                "Esta acción borrará también el historial de pagos y colaboradores asociados.",
                ventanaActual
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (suscripcionDAO.delete(suscripcionActual.getIdSuscripcion())) {
                volverAtras(event);
            } else {
                Dialogos.mostrarError("Error", "No se pudo eliminar la suscripción.", ventanaActual);
            }
        }
    }

    private void habilitarCampos(boolean habilitar) {
        boolean estado = !habilitar;
        txtNombre.setDisable(estado);
        txtPrecio.setDisable(estado);
        comboCiclo.setDisable(estado);
        comboCategoria.setDisable(estado);
        dpFechaActivacion.setDisable(estado);
        dpFechaRenovacion.setDisable(estado);
        chkActivo.setDisable(estado);
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

    /**
     * Abre el modal para añadir un nuevo colaborador.
     */
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

