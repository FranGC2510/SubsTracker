package org.dam.fcojavier.substracker.controller;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Region;
import org.dam.fcojavier.substracker.dao.ParticipaDAO;
import org.dam.fcojavier.substracker.dao.SuscripcionDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.dam.fcojavier.substracker.model.enums.Ciclo;
import org.dam.fcojavier.substracker.model.Participa;
import org.dam.fcojavier.substracker.model.Suscripcion;
import org.dam.fcojavier.substracker.model.Usuario;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InformesController {
    @FXML private Label lblGastoMensual;
    @FXML private Label lblAhorro;
    @FXML private Label lblProyeccionAnual;
    @FXML private BarChart<String, Number> chartBarras;
    @FXML private VBox boxTopGastos;

    private SuscripcionDAO suscripcionDAO;
    private ParticipaDAO participaDAO;
    private Usuario usuarioLogueado;

    public InformesController() {
        this.suscripcionDAO = new SuscripcionDAO();
        this.participaDAO = new ParticipaDAO();
    }

    public void initData(Usuario usuario) {
        this.usuarioLogueado = usuario;
        calcularYMostrarDatos();
    }

    private void calcularYMostrarDatos() {
        List<Suscripcion> misSuscripciones = suscripcionDAO.findByTitularId(usuarioLogueado.getId_usuario());

        double gastoMensualTotal = 0;
        double ahorroMensualTotal = 0;
        Map<String, Double> gastoPorCategoria = new HashMap<>();

        Map<Suscripcion, Double> costesMensualesMap = new HashMap<>();

        for (Suscripcion s : misSuscripciones) {
            if (!s.isActivo()) continue; // Ignoramos las pausadas

            double costeServicioMes = normalizarAMes(s.getPrecio(), s.getCiclo());

            List<Participa> colaboradores = participaDAO.findBySuscripcionId(s.getIdSuscripcion());
            double aporteColaboradoresMes = 0;

            for (Participa p : colaboradores) {
                aporteColaboradoresMes += normalizarAMes(p.getCantidadApagar(), s.getCiclo());
            }

            //Mi Gasto Neto Mensual
            double miGastoNetoMes = costeServicioMes - aporteColaboradoresMes;
            if (miGastoNetoMes < 0) miGastoNetoMes = 0;

            gastoMensualTotal += miGastoNetoMes;
            ahorroMensualTotal += aporteColaboradoresMes;

            String catNombre = s.getCategoria().name();
            gastoPorCategoria.put(catNombre, gastoPorCategoria.getOrDefault(catNombre, 0.0) + miGastoNetoMes);

            // Guardar para el Top 3
            costesMensualesMap.put(s, miGastoNetoMes);
        }

        // ACTUALIZAR UI
        lblGastoMensual.setText(String.format("%.2f €", gastoMensualTotal));
        lblAhorro.setText(String.format("%.2f €", ahorroMensualTotal));
        lblProyeccionAnual.setText(String.format("%.2f €", gastoMensualTotal * 12));

        chartBarras.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Gastos");

        for (Map.Entry<String, Double> entry : gastoPorCategoria.entrySet()) {
            if (entry.getValue() > 0) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        }
        chartBarras.getData().add(series);

        mostrarTop3(costesMensualesMap);
    }

    private void mostrarTop3(Map<Suscripcion, Double> mapaCostes) {
        boxTopGastos.getChildren().clear();

        // Ordenar el mapa por valor descendente y coger los 3 primeros
        List<Map.Entry<Suscripcion, Double>> top3 = mapaCostes.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue())) // Orden inverso
                .limit(3)
                .collect(Collectors.toList());

        if (top3.isEmpty()) {
            boxTopGastos.getChildren().add(new Label("No hay datos suficientes"));
            return;
        }

        int ranking = 1;
        for (Map.Entry<Suscripcion, Double> entry : top3) {
            Suscripcion s = entry.getKey();
            Double coste = entry.getValue();

            HBox fila = new HBox();
            fila.setSpacing(10);
            fila.setStyle("-fx-padding: 10; -fx-background-color: rgba(0,0,0,0.1); -fx-background-radius: 5;");

            Label lblRank = new Label("#" + ranking);
            lblRank.setStyle("-fx-text-fill: -fx-accent-color; -fx-font-weight: bold; -fx-font-size: 16;");

            Label lblNombre = new Label(s.getNombre());
            lblNombre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label lblPrecio = new Label(String.format("%.2f €/mes", coste));
            lblPrecio.setStyle("-fx-text-fill: -fx-text-muted;");

            fila.getChildren().addAll(lblRank, lblNombre, spacer, lblPrecio);
            boxTopGastos.getChildren().add(fila);

            ranking++;
        }
    }

    /**
     * Convierte cualquier precio a su equivalente mensual.
     */
    private double normalizarAMes(double precio, Ciclo ciclo) {
        switch (ciclo) {
            case MENSUAL: return precio;
            case TRIMESTRAL: return precio / 3.0;
            case ANUAL: return precio / 12.0;
            default: return precio;
        }
    }
}
