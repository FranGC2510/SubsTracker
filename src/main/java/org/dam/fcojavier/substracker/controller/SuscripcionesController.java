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

/**
 * Controlador de la vista principal de listado de suscripciones.
 *
 * Gestiona la tabla de datos, los filtros de búsqueda, la navegación al detalle
 * y las acciones rápidas (crear, pagar).
 *
 * Características principales:
 * Tabla responsiva con columnas personalizadas (Iconos, Colores, Botones).
 * Filtrado dinámico múltiple (Texto + Categoría + Estado).
 * Sistema de caché de iconos para optimizar el rendimiento del renderizado.
 *
 * @author Fco Javier García
 * @version 2.0
 */
public class SuscripcionesController {
    @FXML private TableView<Suscripcion> tablaSuscripciones;
    @FXML private TableColumn<Suscripcion, String> colNombre;
    @FXML private TableColumn<Suscripcion, Double> colPrecio;
    @FXML private TableColumn<Suscripcion, LocalDate> colProximoPago;
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

    /**
     * Constructor por defecto.
     * Inicializa la instancia del DAO de suscripciones.
     */
    public SuscripcionesController() {
        this.suscripcionDAO = new SuscripcionDAO();
    }

    /**
     * Método de inicialización de JavaFX.
     *
     * Se ejecuta automáticamente al cargar el FXML. Orquesta la configuración
     * de todos los componentes visuales en el orden correcto:
     *
     * Carga de recursos (iconos).
     * Configuración de las columnas de la tabla (renderizado).
     * Configuración de eventos de la tabla (doble clic, estilos de fila).
     * Inicialización de los filtros de búsqueda.
     */
    @FXML
    public void initialize() {
        cargarIconos();

        configurarColumnaNombre();
        configurarColumnaPrecio();
        configurarColumnaProximoPago();
        configurarColumnaColaboradores();
        configurarColumnaAcciones();

        configurarFilasTabla();

        configurarFiltros();
    }

    /**
     * Recibe los datos de sesión y configura el entorno.
     *
     * Este método es el punto de entrada desde el {@link MainController}.
     * Carga las suscripciones del usuario desde la base de datos.
     *
     * @param usuario El usuario que ha iniciado sesión.
     * @param mainController Referencia al controlador principal para permitir la navegación.
     */
    public void initData(Usuario usuario, MainController mainController) {
        this.usuarioLogueado = usuario;
        this.mainController = mainController;
        cargarSuscripciones();
    }

    // MÉTODOS PRIVADOS DE CONFIGURACIÓN DE COLUMNAS

    /**
     * Configura la columna "Servicio".
     *
     * Añade un icono basado en la categoría a la izquierda del nombre.
     * Aplica estilos condicionales (gris si está pausada, blanco si está activa).
     *
     */
    private void configurarColumnaNombre() {
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
                        imageView.setImage(iconosCategoria.get(s.getCategoria()));
                        setGraphic(imageView);
                    } else {
                        setGraphic(null);
                    }

                    String textoFinal = "  " + nombreServicio;
                    if (!s.isActivo()) textoFinal += " (PAUSADA)";
                    setText(textoFinal);

                    if (!s.isActivo()) {
                        setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #95a5a6; -fx-alignment: CENTER-LEFT;");
                    } else {
                        setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: white; -fx-alignment: CENTER-LEFT;");
                    }
                }
            }
        });
    }

    /**
     * Configura la columna "Precio".
     *
     * Formatea el valor numérico (Double) añadiendo el símbolo de euro y limitando a 2 decimales.
     */
    private void configurarColumnaPrecio() {
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPrecio.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("%.2f €", item));
            }
        });
    }

    /**
     * Configura la columna "Próximo Pago".
     *
     * Implementa lógica de semáforo visual:
     * Rojo (Vencido): La fecha ya pasó.
     * Dorado (Hoy): El pago es hoy.
     * Naranja (Urgente): Faltan 7 días o menos.
     * Blanco (Normal): Faltan más de 7 días.
     * Pendiente 1º Pago: Si la suscripción es nueva y aún no se ha pagado nunca.
     */
    private void configurarColumnaProximoPago() {
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
                        setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else {
                        long diasRestantes = ChronoUnit.DAYS.between(hoy, fechaRenovacion);
                        String textoBase = fechaRenovacion.toString();

                        if (diasRestantes < 0) {
                            setText(textoBase + " (VENCIDO)");
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: CENTER;");
                        } else if (diasRestantes == 0) {
                            setText("¡SE PAGA HOY!");
                            setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold; -fx-font-size: 13px; -fx-alignment: CENTER;");
                        } else if (diasRestantes <= 7) {
                            setText(textoBase + " (" + diasRestantes + " días)");
                            setStyle("-fx-text-fill: #f39c12; -fx-alignment: CENTER;");
                        } else {
                            setText(textoBase);
                            setStyle("-fx-text-fill: -fx-text-light; -fx-alignment: CENTER;");
                        }
                    }
                }
            }
        });
    }

    /**
     * Configura la columna "Compartida".
     * Calcula dinámicamente si la suscripción tiene colaboradores asociados.
     */
    private void configurarColumnaColaboradores() {
        colColaboradores.setCellValueFactory(cellData -> {
            Suscripcion s = cellData.getValue();
            boolean tieneGente = !s.getParticipantes().isEmpty();
            return new SimpleStringProperty(tieneGente ? "SÍ" : "NO");
        });
    }

    /**
     * Configura la columna de "Acciones".
     * Renderiza un botón "Pagar" en cada fila para facilitar el registro rápido de cobros.
     */
    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnPagar = new Button("Pagar");
            {
                btnPagar.getStyleClass().add("button-pay");
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
                    setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(btnPagar);
                }
            }
        });
    }

    /**
     * Configura el comportamiento de las filas de la tabla.
     *
     * - Aplica estilos CSS ("row-inactive") a las suscripciones pausadas.
     * - Configura el evento de doble clic para navegar al detalle.
     */
    private void configurarFilasTabla() {
        tablaSuscripciones.setRowFactory(tv -> {
            TableRow<Suscripcion> row = new TableRow<>() {
                @Override
                protected void updateItem(Suscripcion item, boolean empty) {
                    super.updateItem(item, empty);
                    getStyleClass().remove("row-inactive");
                    if (!empty && item != null && !item.isActivo()) {
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
    }

    /**
     * Inicializa y configura la lógica de filtrado de datos.
     *
     * Carga los opciones de los ComboBox y establece los listeners que re-evalúan
     * qué datos mostrar cada vez que el usuario cambia un criterio.
     */
    private void configurarFiltros() {
        filterCategoria.getItems().add("TODAS");
        for (Categoria c : Categoria.values()) filterCategoria.getItems().add(c.name());
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

    // MÉTODOS DE LÓGICA DE NEGOCIO

    /**
     * Aplica la lógica combinada de los 3 filtros (Texto + Categoría + Estado).
     *
     * Se utiliza una lógica AND: una suscripción debe cumplir TODOS los criterios seleccionados
     * para ser mostrada en la tabla.
     *
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

    /**
     * Carga los iconos de las categorías en memoria.
     *
     * Evita tener que leer los archivos del disco cada vez que se renderiza una celda,
     * mejorando drásticamente el rendimiento del scroll.
     *
     */
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

    /**
     * Consulta la base de datos para obtener las suscripciones actualizadas del usuario.
     * Actualiza la {@code masterData}, lo que refresca automáticamente la tabla.
     */
    private void cargarSuscripciones() {
        if (usuarioLogueado != null) {
            masterData.clear();
            masterData.addAll(suscripcionDAO.findByTitularId(usuarioLogueado.getId_usuario()));
        }
    }

    // ACCIONES FXML

    /**
     * Maneja la acción de crear una nueva suscripción.
     * Abre el formulario modal correspondiente.
     */
    @FXML
    private void handleNuevaSuscripcion(ActionEvent event) {
        abrirModal("/org/dam/fcojavier/substracker/view/formSuscripcionView.fxml", "Nueva Suscripción", null);
    }

    /**
     * Maneja la acción de registrar un pago para una suscripción específica.
     *
     * @param seleccionada La suscripción sobre la que se va a aplicar el pago.
     */
    @FXML
    private void abrirModalPago(Suscripcion seleccionada) {
        abrirModal("/org/dam/fcojavier/substracker/view/formCobroView.fxml", "Registrar Pago", seleccionada);
    }

    /**
     * Método genérico auxiliar para abrir ventanas modales.
     *
     * Centraliza la lógica de carga de FXML, configuración de controladores y gestión
     * de refresco post-cierre.
     *
     * @param fxmlPath Ruta al archivo FXML de la vista modal.
     * @param titulo Título de la ventana.
     * @param suscripcion Objeto suscripción opcional (para pagos).
     */
    private void abrirModal(String fxmlPath, String titulo, Suscripcion suscripcion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();

            if (controller instanceof FormSuscripcionController) {
                ((FormSuscripcionController) controller).setUsuario(this.usuarioLogueado);
            } else if (controller instanceof FormCobroController) {
                ((FormCobroController) controller).setSuscripcion(suscripcion);
            }

            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tablaSuscripciones.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

            boolean exito = false;
            if (controller instanceof FormSuscripcionController) exito = ((FormSuscripcionController) controller).isGuardadoExitoso();
            if (controller instanceof FormCobroController) exito = ((FormCobroController) controller).isGuardadoExitoso();

            if (exito) cargarSuscripciones();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
