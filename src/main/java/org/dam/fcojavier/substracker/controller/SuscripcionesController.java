package org.dam.fcojavier.substracker.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.dam.fcojavier.substracker.dao.SuscripcionDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;
import org.dam.fcojavier.substracker.model.Suscripcion;
import org.dam.fcojavier.substracker.model.Usuario;
import org.dam.fcojavier.substracker.model.enums.Categoria;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class SuscripcionesController {
    @FXML private TableView<Suscripcion> tablaSuscripciones;
    @FXML private TableColumn<Suscripcion, String> colNombre;
    @FXML private TableColumn<Suscripcion, Double> colPrecio;
    @FXML private TableColumn<Suscripcion, String> colCiclo;
    @FXML private TableColumn<Suscripcion, String> colCategoria;
    @FXML private TableColumn<Suscripcion, LocalDate> colProximoPago;
    @FXML private TableColumn<Suscripcion, Boolean> colActivo;
    @FXML private TableColumn<Suscripcion, String> colColaboradores;
    @FXML private TextField txtBuscar;
    @FXML private TableColumn<Suscripcion, Void> colAcciones;
    @FXML private ComboBox<String> filterCategoria;
    @FXML private ComboBox<String> filterEstado;

    private Usuario usuarioLogueado;
    private final SuscripcionDAO suscripcionDAO;
    private MainController mainController;

    private ObservableList<Suscripcion> masterData = FXCollections.observableArrayList();
    private FilteredList<Suscripcion> filteredData;
    private Map<Categoria, Image> iconosCategoria = new HashMap<>();

    public SuscripcionesController() {
        this.suscripcionDAO = new SuscripcionDAO();
    }

    @FXML
    public void initialize() {
        cargarIconos();

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        colNombre.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(24);
                imageView.setFitWidth(24);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String nombreServicio, boolean empty) {
                super.updateItem(nombreServicio, empty);

                if (empty || nombreServicio == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Suscripcion s = getTableView().getItems().get(getIndex());

                    if (s.getCategoria() != null) {
                        Image icon = iconosCategoria.get(s.getCategoria());
                        imageView.setImage(icon);
                        setGraphic(imageView);
                    } else {
                        setGraphic(null);
                    }

                    String textoFinal = "  " + nombreServicio;

                    if (!s.isActivo()) {
                        textoFinal += " (PAUSADA)";
                    }

                    setText(textoFinal);

                    if (!s.isActivo()) {
                        setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #95a5a6; -fx-alignment: CENTER-LEFT;");
                    } else {
                        setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: white; -fx-alignment: CENTER-LEFT;");
                    }
                }
            }
        });
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        // Formato Moneda para el precio
        colPrecio.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("%.2f €", item));
            }
        });

        colProximoPago.setCellValueFactory(new PropertyValueFactory<>("fechaRenovacion"));

        colProximoPago.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate fechaRenovacion, boolean empty) {
                super.updateItem(fechaRenovacion, empty);

                if (empty || fechaRenovacion == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Suscripcion s = getTableView().getItems().get(getIndex());
                    LocalDate fechaActivacion = s.getFechaActivacion();
                    LocalDate hoy = LocalDate.now();

                    boolean esPrimerPagoPendiente = fechaRenovacion.isEqual(fechaActivacion) && !fechaRenovacion.isAfter(hoy);

                    if (esPrimerPagoPendiente) {
                        setText("PENDIENTE (1º PAGO)");
                        // Rojo llamativo para incitar a pagar
                        setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    }
                    else {
                        // --- LÓGICA ESTÁNDAR (Días restantes) ---
                        long diasRestantes = ChronoUnit.DAYS.between(hoy, fechaRenovacion);
                        String textoBase = fechaRenovacion.toString();

                        if (diasRestantes < 0) {
                            setText(textoBase + " (VENCIDO)");
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: CENTER;");
                        }
                        else if (diasRestantes == 0) {
                            setText("¡SE PAGA HOY!");
                            setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold; -fx-font-size: 13px; -fx-alignment: CENTER;");
                        }
                        else if (diasRestantes <= 7) {
                            setText(textoBase + " (" + diasRestantes + " días)");
                            setStyle("-fx-text-fill: #f39c12; -fx-alignment: CENTER;");
                        }
                        else {
                            setText(textoBase);
                            setStyle("-fx-text-fill: -fx-text-light; -fx-alignment: CENTER;");
                        }
                    }
                }
            }
        });

        colColaboradores.setCellValueFactory(cellData -> {
            Suscripcion s = cellData.getValue();
            boolean tieneGente = !s.getParticipantes().isEmpty();
            return new SimpleStringProperty(tieneGente ? "SÍ" : "NO");
        });

        colAcciones.setCellFactory(param -> new TableCell<>() {
            // Creamos el botón
            private final Button btnPagar = new Button("Pagar");

            {
                // ASIGNAMOS LA CLASE CSS (En lugar de setStyle)
                btnPagar.getStyleClass().add("button-pay");

                // Acción del botón
                btnPagar.setOnAction(event -> {
                    Suscripcion suscripcion = getTableView().getItems().get(getIndex());
                    abrirModalPago(suscripcion);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Centramos el botón en la celda
                    setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(btnPagar);
                }
            }
        });

        tablaSuscripciones.setRowFactory(tv -> {
            TableRow<Suscripcion> row = new TableRow<>() {
                @Override
                protected void updateItem(Suscripcion item, boolean empty) {
                    super.updateItem(item, empty);

                    getStyleClass().remove("row-inactive");

                    if (empty || item == null) {
                        return;
                    }

                    if (!item.isActivo()) {
                        getStyleClass().add("row-inactive");
                    }
                }
            };

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty()) ) {
                    Suscripcion rowData = row.getItem();
                    if (mainController != null) {
                        mainController.mostrarDetalleSuscripcion(rowData);
                    }
                }
            });

            return row;
        });

        filterCategoria.getItems().add("TODAS");
        for (Categoria c : Categoria.values()) {
            filterCategoria.getItems().add(c.name());
        }
        filterCategoria.getSelectionModel().selectFirst();

        filterEstado.getItems().addAll("TODOS", "ACTIVAS", "PAUSADAS");
        filterEstado.getSelectionModel().selectFirst();

        filteredData = new FilteredList<>(masterData, p -> true);

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        filterCategoria.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        filterEstado.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        SortedList<Suscripcion> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaSuscripciones.comparatorProperty());
        tablaSuscripciones.setItems(sortedData);
    }

    // Este método lo llamará el MainController
    public void initData(Usuario usuario, MainController mainController) {
        this.usuarioLogueado = usuario;
        this.mainController = mainController; // Guardamos la referencia
        cargarSuscripciones();
    }

    /**
     * Método central que combina todos los criterios de búsqueda.
     * Funciona con lógica AND (debe cumplir todo).
     */
    private void aplicarFiltros() {
        String textoBusqueda = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase() : "";
        String catSeleccionada = filterCategoria.getValue();
        String estadoSeleccionado = filterEstado.getValue();

        filteredData.setPredicate(suscripcion -> {
            if (!textoBusqueda.isEmpty()) {
                if (!suscripcion.getNombre().toLowerCase().contains(textoBusqueda)) {
                    return false;
                }
            }

            if (catSeleccionada != null && !catSeleccionada.equals("TODAS")) {
                if (!suscripcion.getCategoria().name().equals(catSeleccionada)) {
                    return false;
                }
            }

            if (estadoSeleccionado != null && !estadoSeleccionado.equals("TODOS")) {
                boolean buscarActivas = estadoSeleccionado.equals("ACTIVAS");
                if (suscripcion.isActivo() != buscarActivas) {
                    return false;
                }
            }
            return true;
        });
    }

    private void cargarIconos() {
        try {
            iconosCategoria.put(Categoria.OCIO,      new Image(getClass().getResourceAsStream("/images/ic_ocio.png")));
            iconosCategoria.put(Categoria.HOGAR,     new Image(getClass().getResourceAsStream("/images/ic_home.png")));
            iconosCategoria.put(Categoria.TRABAJO,   new Image(getClass().getResourceAsStream("/images/ic_work.png")));
            iconosCategoria.put(Categoria.SALUD,     new Image(getClass().getResourceAsStream("/images/ic_health.png")));
            iconosCategoria.put(Categoria.EDUCACION, new Image(getClass().getResourceAsStream("/images/ic_education.png")));
        } catch (Exception e) {
            System.err.println("Error cargando iconos de categoría. Revisa las rutas en resources/icons/");
        }
    }

    private void cargarSuscripciones() {
        if (usuarioLogueado != null) {
            masterData.clear();
            masterData.addAll(suscripcionDAO.findByTitularId(usuarioLogueado.getId_usuario()));
        }
    }

    @FXML
    private void handleNuevaSuscripcion(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/formSuscripcionView.fxml"));
            Parent root = loader.load();

            FormSuscripcionController controller = loader.getController();
            controller.setUsuario(this.usuarioLogueado);

            Stage stage = new Stage();
            stage.setTitle("Nueva Suscripción");
            stage.setScene(new Scene(root));

            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tablaSuscripciones.getScene().getWindow());
            stage.setResizable(false);

            stage.showAndWait();

            if (controller.isGuardadoExitoso()) {
                cargarSuscripciones();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirModalPago(Suscripcion seleccionada) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/formCobroView.fxml"));
            Parent root = loader.load();

            FormCobroController controller = loader.getController();
            controller.setSuscripcion(seleccionada);

            Stage stage = new Stage();
            stage.setTitle("Registrar Pago - " + seleccionada.getNombre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tablaSuscripciones.getScene().getWindow());
            stage.setResizable(false);

            stage.showAndWait();

            if (controller.isGuardadoExitoso()) {
                cargarSuscripciones();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
