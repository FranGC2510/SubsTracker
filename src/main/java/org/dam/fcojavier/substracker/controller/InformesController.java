package org.dam.fcojavier.substracker.controller;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Region;
import org.dam.fcojavier.substracker.dao.ParticipaDAO;
import org.dam.fcojavier.substracker.dao.SuscripcionDAO;
import javafx.fxml.FXML;
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

/**
 * Controlador de la vista de Informes y Estadísticas Financieras.
 *
 * Esta clase se encarga de procesar todos los datos de suscripciones y colaboraciones
 * para generar una "foto fija" de la salud financiera del usuario.
 *
 * Funcionalidades clave:
 * Normalización de costes (todo se convierte a base mensual).
 * Cálculo de KPIs (Gasto Neto, Ahorro por copagos, Proyección anual).
 * Visualización gráfica de gastos por categoría (BarChart).
 * Generación de ranking "Top 3" de servicios más costosos.
 *
 * @author Tu Nombre
 * @version 2.0 (Refactorizado y Documentado)
 */
public class InformesController {
    @FXML private Label lblGastoMensual;
    @FXML private Label lblAhorro;
    @FXML private Label lblProyeccionAnual;
    @FXML private BarChart<String, Number> chartBarras;
    @FXML private VBox boxTopGastos;

    private SuscripcionDAO suscripcionDAO;
    private ParticipaDAO participaDAO;
    private Usuario usuarioLogueado;

    /**
     * Constructor por defecto. Inicializa los DAOs.
     */
    public InformesController() {
        this.suscripcionDAO = new SuscripcionDAO();
        this.participaDAO = new ParticipaDAO();
    }

    /**
     * Punto de entrada principal. Recibe el usuario y desencadena el cálculo de estadísticas.
     *
     * @param usuario El usuario del cual se mostrarán los informes.
     */
    public void initData(Usuario usuario) {
        this.usuarioLogueado = usuario;
        procesarDatosFinancieros();
    }

    /**
     * Motor de cálculo principal.
     *
     * 1. Recupera todas las suscripciones activas.
     * 2. Itera sobre ellas normalizando precios a base mensual.
     * 3. Acumula los totales globales, por categoría y por servicio.
     * 4. Delega la actualización de la interfaz a métodos específicos.
     *
     */
    private void procesarDatosFinancieros() {
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

            double miGastoNetoMes = costeServicioMes - aporteColaboradoresMes;
            if (miGastoNetoMes < 0) miGastoNetoMes = 0;

            gastoMensualTotal += miGastoNetoMes;
            ahorroMensualTotal += aporteColaboradoresMes;

            String catNombre = s.getCategoria().name();
            gastoPorCategoria.put(catNombre, gastoPorCategoria.getOrDefault(catNombre, 0.0) + miGastoNetoMes);

            costesMensualesMap.put(s, miGastoNetoMes);
        }

        actualizarKPIs(gastoMensualTotal, ahorroMensualTotal);
        actualizarGraficoBarras(gastoPorCategoria);

        mostrarTop3(costesMensualesMap);
    }

    /**
     * Actualiza las tarjetas numéricas superiores (Big Numbers).
     *
     * @param gastoMensual Gasto neto mensual total.
     * @param ahorroMensual Ahorro total por copagos.
     */
    private void actualizarKPIs(double gastoMensual, double ahorroMensual) {
        lblGastoMensual.setText(String.format("%.2f €", gastoMensual));
        lblAhorro.setText(String.format("%.2f €", ahorroMensual));

        lblProyeccionAnual.setText(String.format("%.2f €", gastoMensual * 12));
    }

    /**
     * Genera y muestra el gráfico de barras por categoría.
     *
     * @param gastoPorCategoria Mapa con los totales acumulados por categoría.
     */
    private void actualizarGraficoBarras(Map<String, Double> gastoPorCategoria) {
        chartBarras.getData().clear();
        chartBarras.setAnimated(true); //Anmación suave al cargar

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Gasto Mensual");

        for (Map.Entry<String, Double> entry : gastoPorCategoria.entrySet()) {
            if (entry.getValue() > 0) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        }

        chartBarras.getData().add(series);
    }

    /**
     * Genera la lista visual de los 3 servicios más costosos.
     *
     * @param mapaCostes Mapa con el coste mensual de cada suscripción individual.
     */
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
            crearFilaRanking(ranking++, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Método auxiliar para crear visualmente una fila del ranking.
     * Genera un HBox estilizado con el puesto, nombre y precio.
     *
     * @param ranking Posición (1, 2, 3).
     * @param s Suscripción.
     * @param costeMensual Coste calculado.
     */
    private void crearFilaRanking(int ranking, Suscripcion s, Double costeMensual) {
        HBox fila = new HBox();
        fila.setSpacing(10);
        fila.setStyle("-fx-padding: 10; -fx-background-color: rgba(0,0,0,0.1); -fx-background-radius: 5; -fx-alignment: CENTER_LEFT;");


        Label lblNombre = new Label(s.getNombre());
        lblNombre.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblPrecio = new Label(String.format("%.2f €/mes", costeMensual));
        lblPrecio.setStyle("-fx-text-fill: -fx-text-muted;");

        fila.getChildren().addAll(lblNombre, spacer, lblPrecio);
        boxTopGastos.getChildren().add(fila);
    }

    /**
     * Utilidad matemática para normalizar precios a una base mensual.
     *
     * @param precio El importe original.
     * @param ciclo La frecuencia de pago.
     * @return El importe equivalente mensual.
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
