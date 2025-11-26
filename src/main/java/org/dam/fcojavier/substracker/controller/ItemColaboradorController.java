package org.dam.fcojavier.substracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.dam.fcojavier.substracker.model.enums.Ciclo;
import org.dam.fcojavier.substracker.model.Participa;

public class ItemColaboradorController {
    @FXML private HBox root;
    @FXML private Label lblNombre;
    @FXML private Label lblDescripcion;
    @FXML private Label lblImporte;
    @FXML private Label lblEstado;

    private Participa miParticipa;

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
            lblEstado.setStyle("-fx-text-fill: #2ecc71;"); // Texto verde
            lblImporte.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 16px; -fx-font-weight: bold;");
            root.getStyleClass().add("card-pagado"); // Borde verde
        } else {
            lblEstado.setText("PENDIENTE");
            lblEstado.setStyle("-fx-text-fill: #e74c3c;"); // Texto rojo
            lblImporte.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 16px; -fx-font-weight: bold;");
            root.getStyleClass().add("card-pendiente"); // Borde rojo
        }
        root.setOnMouseClicked(event -> {
            if (onEditAction != null) {
                onEditAction.run(); // Ejecutamos la acción que nos pasó el padre
            }
        });
    }
}
