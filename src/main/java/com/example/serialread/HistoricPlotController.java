package com.example.serialread;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Themes;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoricPlotController {
    private String filePath;
    private String NamaPasien;
    private String GenderPasien;
    private String TanggalLahir;
    private String UmurPasien;

    private final double aspectRatio = 2.40; // As calculated earlier

    private final ArrayList<Integer> TimeStamps = new ArrayList<>();
    private final ArrayList<Integer> Datas1 = new ArrayList<>();
    private final ArrayList<Integer> Datas2 = new ArrayList<>();
    private final ArrayList<Integer> Datas3 = new ArrayList<>();
    private final ArrayList<Double>  TimesElapsed = new ArrayList<>();

    @FXML
    private Label namaPasien, jenisKelamin, tanggalLahir, Umur;

    @FXML
    private LineChart<Number, Number> data1LineChart, data2LineChart, data3LineChart;
    @FXML
    private NumberAxis xAxis1, xAxis2, xAxis3;

    @FXML
    private Pane gridPane1, gridPane2, gridPane3;
    @FXML
    public VBox ecgExport;
    @FXML
    private ScrollBar dataScrollBar; // Add ScrollBar FXML reference

    //setup a scheduled excecutor to periodically put data into the chart
    //private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private static final double MM_PER_SEC = 25;
    private static final double SEC_DISPLAYED = 3;
    private static final int DATA_POINT_INTERVAL = 2; // the time interval between data points in milliseconds
    private final int pointsToDisplay = (int) SEC_DISPLAYED * 1000 / DATA_POINT_INTERVAL;


    private XYChart.Series<Number, Number> data1Series = new XYChart.Series<>();
    private XYChart.Series<Number, Number> data2Series = new XYChart.Series<>();
    private XYChart.Series<Number, Number> data3Series = new XYChart.Series<>();

    private XYChart.Series<Number, Number> createSeries(List<Double> times, List<Integer> dataPoints) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0; i < times.size(); i++) {
            series.getData().add(new XYChart.Data<>(times.get(i), dataPoints.get(i)));
        }
        return series;
    }

    public void setfilePath(String filepath){
        filePath = filepath;
        System.out.println("File Selected" + filepath);
        readCSV(filePath);
    }

    protected void readCSV(String filePath) {
        try (
                Reader reader = new FileReader(filePath);
                CSVReader csvReader = new CSVReaderBuilder(reader)
                        .withCSVParser(new com.opencsv.CSVParserBuilder().withSeparator(';').build())
                        .withSkipLines(1).build() // Skipping header line
        ) {
            String[] firstRecord = csvReader.readNext();
            TimeStamps.add(Integer.parseInt(firstRecord[0]));
            Datas1.add(Integer.parseInt(firstRecord[1]));
            Datas2.add(Integer.parseInt(firstRecord[2]));
            Datas3.add(Integer.parseInt(firstRecord[3]));
            TimesElapsed.add(Double.parseDouble(firstRecord[4]));
            NamaPasien = firstRecord[10];
            GenderPasien = firstRecord[11];
            TanggalLahir = firstRecord[12];
            UmurPasien = firstRecord[13];
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                TimeStamps.add(Integer.parseInt(nextRecord[0]));
                Datas1.add(Integer.parseInt(nextRecord[1]));
                Datas2.add(Integer.parseInt(nextRecord[2]));
                Datas3.add(Integer.parseInt(nextRecord[3]));
                TimesElapsed.add(Double.parseDouble(nextRecord[4]));
                // Process the data as needed
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        updateUI();
        System.out.println("TimeStamp Array size is :" + TimeStamps.size());
        System.out.println("Data1 Array size is :" + Datas1.size());
        System.out.println("Data2 Array size is :" + Datas2.size());
        System.out.println("Data3 Array size is :" + Datas3.size());
        System.out.println("Time Elapsed Array size is :" + TimesElapsed.size());
        System.out.println(Collections.max(TimesElapsed));
        namaPasien.setText(NamaPasien);
        jenisKelamin.setText(GenderPasien);
        tanggalLahir.setText(TanggalLahir);
        Umur.setText(UmurPasien);

    }

    protected void updateUI() {

        double minTimeElapsed = Collections.min(TimesElapsed);
        double maxTimeElapsed = Collections.max(TimesElapsed);
        xAxis1.setLowerBound(minTimeElapsed);
        xAxis1.setUpperBound(maxTimeElapsed);

        xAxis2.setLowerBound(minTimeElapsed);
        xAxis2.setUpperBound(maxTimeElapsed);

        xAxis3.setLowerBound(minTimeElapsed);
        xAxis3.setUpperBound(maxTimeElapsed);

        if (TimesElapsed.size() < pointsToDisplay){
            for (int i =0; i < TimesElapsed.size(); i++) {
                data1Series.getData().add(new XYChart.Data<>(TimesElapsed.get(i), Datas1.get(i)));
                data2Series.getData().add(new XYChart.Data<>(TimesElapsed.get(i), Datas2.get(i)));
                data3Series.getData().add(new XYChart.Data<>(TimesElapsed.get(i), Datas3.get(i)));
            }
        } else{
        initializeScrollBar();
        }
    }
    private void initializeScrollBar() {

        // Set the scroll bar properties
        dataScrollBar.setMin(0);
        // Set the maximum to the number of timestamps minus the points to display
        dataScrollBar.setMax(TimeStamps.size() - pointsToDisplay);
        // The visible amount is the ratio of points to display to the total number of points
//        dataScrollBar.setVisibleAmount((double)  TimeStamps.size() / pointsToDisplay );
        dataScrollBar.setVisibleAmount( Collections.max(TimesElapsed) / SEC_DISPLAYED );


        Platform.runLater(() -> {
//        for (int i =0; i < TimeStamps.size(); i++) {
//            data1Series.getData().add(new XYChart.Data<>(TimesElapsed.get(i), Datas1.get(i)));
//            data2Series.getData().add(new XYChart.Data<>(TimesElapsed.get(i), Datas2.get(i)));
//            data3Series.getData().add(new XYChart.Data<>(TimesElapsed.get(i), Datas3.get(i)));
//        }
            data1Series = createSeries(TimesElapsed, Datas1);
            data2Series = createSeries(TimesElapsed, Datas2);
            data3Series = createSeries(TimesElapsed, Datas3);

            data1LineChart.getData().addAll(data1Series);
            data2LineChart.getData().addAll(data2Series);
            data3LineChart.getData().addAll(data3Series);
        });
        xAxis1.setLowerBound(0);
        xAxis1.setUpperBound(30);
        xAxis2.setLowerBound(0);
        xAxis2.setUpperBound(30);
        xAxis3.setLowerBound(0);
        xAxis3.setUpperBound(30);
        System.out.println("Width is : " + ecgExport.getWidth());
        System.out.println("Height is : " + ecgExport.getHeight());
        // Add a listener to update the chart when the scrollbar is moved
        dataScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
            int startIndex = newValue.intValue();
            updateChartToDisplayRange(startIndex);
        });
    }

    private void updateChartToDisplayRange(int startIndex) {
        // Clear existing data
        //data1Series.getData().clear();
        // Calculate endIndex, ensuring it does not go past the size of TimeStamps
        int endIndex = Math.min(startIndex + pointsToDisplay, TimeStamps.size());
        Platform.runLater(() -> {
        // Update the series data to reflect the new range
//        for (int i = startIndex; i < endIndex; i++) {
//            data1Series.getData().add(new XYChart.Data<>(TimesElapsed.get(i), Datas1.get(i)));
//            data2Series.getData().add(new XYChart.Data<>(TimesElapsed.get(i), Datas2.get(i)));
//            data3Series.getData().add(new XYChart.Data<>(TimesElapsed.get(i), Datas3.get(i)));
//        }

        // Set the range of the x-axis to the time elapsed range
        xAxis1.setLowerBound(TimesElapsed.get(startIndex));
        xAxis1.setUpperBound(TimesElapsed.get(Math.min(endIndex - 1, TimesElapsed.size() - 1)));
        xAxis2.setLowerBound(TimesElapsed.get(startIndex));
        xAxis2.setUpperBound(TimesElapsed.get(Math.min(endIndex - 1, TimesElapsed.size() - 1)));
        xAxis3.setLowerBound(TimesElapsed.get(startIndex));
        xAxis3.setUpperBound(TimesElapsed.get(Math.min(endIndex - 1, TimesElapsed.size() - 1)));
        });
    }
    public void exportAnchorPaneAsImage() {
        dataScrollBar.setVisible(false);
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.WHITE); // Setting a background color

        double scaleFactor = 2; // Increase this as necessary
        params.setTransform(javafx.scene.transform.Transform.scale(scaleFactor, scaleFactor));

        // Ensure the layout is updated before capturing the snapshot
        ecgExport.applyCss();
        ecgExport.layout();

        // Create a scaled instance of WritableImage
        WritableImage image = new WritableImage(
                (int) Math.round(ecgExport.getWidth() * scaleFactor),
                (int) Math.round(ecgExport.getHeight() * scaleFactor)
        );

        image = ecgExport.snapshot(params, image);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Files", "*.png")
        );

        File file = fileChooser.showSaveDialog(ecgExport.getScene().getWindow());
        if (file != null) {
            try {
                String extension = "png"; // could also be "jpg" or "bmp" etc...
                if (!file.getName().endsWith("." + extension)) {
                    file = new File(file.getAbsolutePath() + "." + extension);
                }
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), extension, file);
            } catch (IOException e) {
                // Handle exception here
                e.printStackTrace();
            }
        }
        dataScrollBar.setVisible(true);
    }

    private void drawEcgGrid(Pane gpane) {
        gpane.getChildren().clear();

        double width = gpane.getWidth();
        double height = gpane.getHeight();
        //double majorGridSpacing = 50;
        double majorGridSpacing = width / (MM_PER_SEC * SEC_DISPLAYED / 5); //5mm boxes

        //double minorGridSpacing = 10; // adjust as needed
        double minorGridSpacing = majorGridSpacing / 5; // adjust as needed

        for (double x = 0; x < width; x += majorGridSpacing) {
            Line majorVerticalLine = new Line(x, 0, x, height);
            majorVerticalLine.setStroke(Color.DARKRED);
            gpane.getChildren().add(majorVerticalLine);

            for (double minorX = x; minorX < x + majorGridSpacing; minorX += minorGridSpacing) {
                Line minorVerticalLine = new Line(minorX, 0, minorX, height);
                minorVerticalLine.setStroke(Color.RED);
                minorVerticalLine.setOpacity(0.5);
                gpane.getChildren().add(minorVerticalLine);
            }
        }

        for (double y = 0; y < height; y += majorGridSpacing) {
            Line majorHorizontalLine = new Line(0, y, width, y);
            majorHorizontalLine.setStroke(Color.DARKRED);
            gpane.getChildren().add(majorHorizontalLine);

            for (double minorY = y; minorY < y + majorGridSpacing; minorY += minorGridSpacing) {
                Line minorHorizontalLine = new Line(0, minorY, width, minorY);
                minorHorizontalLine.setStroke(Color.RED);
                minorHorizontalLine.setOpacity(0.5);
                gpane.getChildren().add(minorHorizontalLine);
            }
        }
    }

    // This method should be updated or created to handle the "Back" action
    @FXML
    private void handleBackButton(MouseEvent event) {
            try {
                // Navigate back to the previous FXML screen
                // Change "previous.fxml" to the actual name of your previous FXML file
                Parent previousPage = FXMLLoader.load(getClass().getResource("Index.fxml"));
                Scene previousScene = new Scene(previousPage);
                previousScene.getStylesheets().add(getClass().getResource("IndexStyle.css").toExternalForm());
                MFXThemeManager.addOn(previousScene, Themes.DEFAULT, Themes.LEGACY);
                // Get the current stage using the event source
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // Change the scene on the current stage to the previous scene
                stage.setScene(previousScene);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception, maybe show a dialog to the user
            }
        }

    @FXML
    public void initialize() {
        System.out.println("HistoricPlotController intialized");
        data1LineChart.getData().clear();
        data2LineChart.getData().clear();
        data3LineChart.getData().clear();

        data1LineChart.getData().add(data1Series);
        data2LineChart.getData().add(data2Series);
        data3LineChart.getData().add(data3Series);


        gridPane1.widthProperty().addListener((observable, oldValue, newValue) -> drawEcgGrid(gridPane1));
        gridPane1.heightProperty().addListener((observable, oldValue, newValue) -> drawEcgGrid(gridPane1));
        gridPane2.widthProperty().addListener((observable, oldValue, newValue) -> drawEcgGrid(gridPane2));
        gridPane2.heightProperty().addListener((observable, oldValue, newValue) -> drawEcgGrid(gridPane2));
        gridPane3.widthProperty().addListener((observable, oldValue, newValue) -> drawEcgGrid(gridPane3));
        gridPane3.heightProperty().addListener((observable, oldValue, newValue) -> drawEcgGrid(gridPane3));
//        ecgExport.widthProperty().addListener((observable, oldValue, newValue) -> {
//            double width = newValue.doubleValue();
//            // Set the desired aspect ratio
//            double aspectRatio = 0.7236467236467236; // Height is 75% of the width
//            ecgExport.setPrefHeight(width / aspectRatio);
//        });

    }
}

