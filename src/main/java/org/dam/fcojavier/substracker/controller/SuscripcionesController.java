package org.dam.fcojavier.substracker.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.dam.fcojavier.substracker.dao.SuscripcionDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.dam.fcojavier.substracker.model.Suscripcion;
import org.dam.fcojavier.substracker.model.Usuario;

import java.io.IOException;
import java.time.LocalDate;

public class SuscripcionesController {
    @FXML private TableView<Suscripcion> tablaSuscripciones;
    @FXML private TableColumn<Suscripcion, String> colNombre;
    @FXML private TableColumn<Suscripcion, Double> colPrecio;
    @FXML private TableColumn<Suscripcion, String> colCiclo;
    @FXML private TableColumn<Suscripcion, String> colCategoria;
    @FXML private TableColumn<Suscripcion, LocalDate> colProximoPago;
    @FXML private TableColumn<Suscripcion, Boolean> colActivo;

    private Usuario usuarioLogueado;
    private final SuscripcionDAO suscripcionDAO;

    private MainController mainController;

    private ObservableList<Suscripcion> listaSuscripciones;

    public SuscripcionesController() {
        this.suscripcionDAO = new SuscripcionDAO();
    }

    @FXML
    public void initialize() {
        // Configurar las columnas para que lean los atributos de la clase Suscripcion
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colCiclo.setCellValueFactory(new PropertyValueFactory<>("ciclo"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colProximoPago.setCellValueFactory(new PropertyValueFactory<>("fechaRenovacion"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        colActivo.setCellFactory(column -> new EstadoCell());

        tablaSuscripciones.setRowFactory(tv -> {
            TableRow<Suscripcion> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty()) ) {
                    Suscripcion rowData = row.getItem();

                    // EN LUGAR DE ABRIR VENTANA, NAVEGAMOS
                    if (mainController != null) {
                        mainController.mostrarDetalleSuscripcion(rowData);
                    }
                }
            });
            return row;
        });
    }

    // Este método lo llamará el MainController
    public void initData(Usuario usuario, MainController mainController) {
        this.usuarioLogueado = usuario;
        this.mainController = mainController; // Guardamos la referencia
        cargarSuscripciones();
    }

    private void cargarSuscripciones() {
        if (usuarioLogueado != null) {
            listaSuscripciones = FXCollections.observableArrayList(
                    suscripcionDAO.findByTitularId(usuarioLogueado.getId_usuario())
            );
            tablaSuscripciones.setItems(listaSuscripciones);
        }
    }

    // --- ACCIONES DE BOTONES (Pendientes para próximos pasos) ---

    @FXML
    private void handleNuevaSuscripcion(ActionEvent event) {
        try {
            // 1. Cargar el FXML del formulario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam/fcojavier/substracker/view/formSuscripcionView.fxml"));
            Parent root = loader.load();

            // 2. Pasar el usuario al controlador del formulario
            FormSuscripcionController controller = loader.getController();
            controller.setUsuario(this.usuarioLogueado);

            // 3. Crear el Stage (Ventana)
            Stage stage = new Stage();
            stage.setTitle("Nueva Suscripción");
            stage.setScene(new Scene(root));

            // Configuración Modal (Bloquea la ventana de atrás)
            stage.initModality(Modality.WINDOW_MODAL);
            // Definimos quién es el dueño (para que bloquee correctamente)
            stage.initOwner(tablaSuscripciones.getScene().getWindow());
            stage.setResizable(false);

            // 4. Mostrar y Esperar
            stage.showAndWait(); // El código se detiene aquí hasta que se cierre la ventana

            // 5. Al cerrar, verificamos si se guardó algo para refrescar la tabla
            if (controller.isGuardadoExitoso()) {
                cargarSuscripciones(); // Recarga desde la BD
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditar(ActionEvent event) {
        Suscripcion seleccionada = tablaSuscripciones.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            System.out.println("Editando: " + seleccionada.getNombre());
        } else {
            System.out.println("Selecciona una fila primero");
        }
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
        Suscripcion seleccionada = tablaSuscripciones.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            System.out.println("Eliminando: " + seleccionada.getNombre());
        }
    }

    // CLASE INTERNA ESTÁTICA PARA GESTIONAR LA VISTA DE LA CELDA

    private static class EstadoCell extends TableCell<Suscripcion, Boolean> {

        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);

            // 1. Limpieza obligatoria (por el reciclaje de celdas de JavaFX)
            setText(null);
            getStyleClass().removeAll("estado-activo", "estado-pausado");

            if (empty || item == null) {
                return;
            }

            // 2. Lógica de presentación
            if (item) {
                setText("ACTIVA");
                getStyleClass().add("estado-activo"); // Clase del CSS
            } else {
                setText("PAUSADA");
                getStyleClass().add("estado-pausado"); // Clase del CSS
            }
        }
    }
}
