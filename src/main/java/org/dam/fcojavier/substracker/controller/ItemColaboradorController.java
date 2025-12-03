package org.dam.fcojavier.substracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.dam.fcojavier.substracker.model.enums.Ciclo;
import org.dam.fcojavier.substracker.model.Participa;

/**
 * Controlador para la vista de un ítem individual (Tarjeta) en la lista de colaboradores.
 *
 * Esta clase gestiona la representación visual de un objeto {@link Participa}.
 * Se encarga de:
 * Vincular los datos del modelo con las etiquetas de la interfaz.
 * Aplicar estilos CSS condicionales (Verde/Rojo) según el estado del pago.
 * Gestionar el evento de clic para permitir la edición del colaborador.
 *
 * @author Fco Javier García
 * @version 1.0
 */
public class ItemColaboradorController {
    @FXML private HBox root;
    @FXML private Label lblNombre;
    @FXML private Label lblDescripcion;
    @FXML private Label lblImporte;
    @FXML private Label lblEstado;

    private Participa miParticipa;

    /**
     * Configura los datos visuales y el comportamiento de la tarjeta.
     *
     * Este método es llamado externamente por el controlador de la lista ({@link DetalleSuscripcionController})
     * cada vez que se crea una nueva tarjeta en el bucle.
     *
     * @param participa El objeto con los datos del colaborador y su aporte.
     * @param cicloSuscripcion El ciclo de la suscripción padre (necesario para calcular si el pago ha caducado).
     * @param onEditAction Un {@link Runnable} (callback) que se ejecutará cuando el usuario haga clic en la tarjeta.
     * Generalmente abre la ventana modal de edición.
     */
    public void setDatos(Participa participa, Ciclo cicloSuscripcion, Runnable onEditAction) {
        this.miParticipa = participa;
        lblNombre.setText(participa.getNombreVisual());

        String desc = participa.getDescripcion();
        if (desc == null || desc.isEmpty()) {
            desc = "Pago vía " + participa.getMetodo_pago();
        }
        lblDescripcion.setText(desc);

        lblImporte.setText(String.format("%.2f €", participa.getCantidadApagar()));

        boolean pagado = participa.isAlDia(cicloSuscripcion);

        root.getStyleClass().removeAll("card-pagado", "card-pendiente");

        if (pagado) {
            lblEstado.setText("PAGADO");
            lblEstado.setStyle("-fx-text-fill: #2ecc71;");
            lblImporte.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 16px; -fx-font-weight: bold;");
            root.getStyleClass().add("card-pagado");
        } else {
            lblEstado.setText("PENDIENTE");
            lblEstado.setStyle("-fx-text-fill: #e74c3c;");
            lblImporte.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 16px; -fx-font-weight: bold;");
            root.getStyleClass().add("card-pendiente");
        }
        root.setOnMouseClicked(event -> {
            if (onEditAction != null) {
                onEditAction.run(); // Ejecutamos la acción que nos pasó el padre
            }
        });
    }
}
