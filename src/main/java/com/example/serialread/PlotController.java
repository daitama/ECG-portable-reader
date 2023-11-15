package com.example.serialread;

import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Themes;
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
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class PlotController {

    private int TimeStamp;
    private int Data1;
    private int Data2;
    private int Data3;

    @FXML
    private Label TimeStamplabel, Data1label, Data2label, Data3label, namaPasien, jenisKelamin, tanggalLahir, Umur;
    @FXML
    private Label comWarning;
    @FXML
    private LineChart<Number, Number> data1LineChart, data2LineChart, data3LineChart;
    @FXML
    private NumberAxis xAxis1, xAxis2, xAxis3;
    @FXML
    private NumberAxis yAxis1, yAxis2, yAxis3;
    @FXML
    private Pane gridPane1, gridPane2, gridPane3;
    @FXML
    public VBox ecgExport;
    @FXML
    SerialController serialController;
    DataController dataController;

    //setup a scheduled excecutor to periodically put data into the chart
    //private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private double timeElapsed = 0;
    private static final double UPDATE_INTERVAL = 0.02; //Update every 20 miliseconds
    private static final double X_AXIS_LOWER_BOUND = 0;
    private static final double X_AXIS_UPPER_BOUND = 30; // Display 5 seconds of data
    private static final double MM_PER_SEC = 25;
    private static final double SEC_DISPLAYED = 3;


    private XYChart.Series<Number, Number> data1Series = new XYChart.Series<>();
    private XYChart.Series<Number, Number> data2Series = new XYChart.Series<>();
    private XYChart.Series<Number, Number> data3Series = new XYChart.Series<>();


    public static int calculateAge(LocalDate birthDate, LocalDate currentDate) {
        if ((birthDate != null) && (currentDate != null)) {
            return Period.between(birthDate, currentDate).getYears();
        } else {
            return 0;
        }
    }

    private void clearChartData() {
        // Reset the time elapsed and data variables to zero
        timeElapsed = 0;
        TimeStamp = 0;
        Data1 = 0;
        Data2 = 0;
        Data3 = 0;
        // Clear the series data for both charts
        data1Series.getData().clear();
        data2Series.getData().clear();
        data3Series.getData().clear();

        // Reset the x-axis lower and upper bounds
        xAxis1.setLowerBound(X_AXIS_LOWER_BOUND);
        xAxis1.setUpperBound(X_AXIS_UPPER_BOUND);
        xAxis2.setLowerBound(X_AXIS_LOWER_BOUND);
        xAxis2.setUpperBound(X_AXIS_UPPER_BOUND);
        xAxis3.setLowerBound(X_AXIS_LOWER_BOUND);
        xAxis3.setUpperBound(X_AXIS_UPPER_BOUND);

        // Clear the labels
        TimeStamplabel.setText("");
        Data1label.setText("");
        Data2label.setText("");
        Data3label.setText("");
        if(dataController != null){
            dataController.resetData();
        }
        updateUI();
    }

    public void setIdentity(String name, String gender, LocalDate birthDate){
        System.out.println("Nama Pasien :" + name);
        System.out.println("Jenis Kelamin :" + gender);
        System.out.println("Tanggal Lahir :" + birthDate);
        LocalDate currentDate = LocalDate.now();
        int umur = calculateAge(birthDate, currentDate);
        namaPasien.setText(name);
        jenisKelamin.setText(gender);
        Umur.setText(umur + " Tahun");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        String TanggalLahir = (birthDate.format(formatter)).toString();
        tanggalLahir.setText(TanggalLahir);
        dataController.setSendPatientData(name, gender, TanggalLahir, umur);
    }
    public void setSendValue(int timestamp, int ecg1, int ecg2, int ecg3) {
        TimeStamp = timestamp;
        Data1 = ecg1;
        Data2 = ecg2;
        Data3 = ecg3;
        updateUI();
    }
    public void updateUI(){
        // Update UI using Platform.runLater
        TimeStamplabel.setText(Integer.toString(TimeStamp));
        Data1label.setText(Integer.toString(Data1));
        Data2label.setText(Integer.toString(Data2));
        Data3label.setText(Integer.toString(Data3));

        // Add data to the charts
        data1Series.getData().add(new XYChart.Data<>(timeElapsed, Data1));
        data2Series.getData().add(new XYChart.Data<>(timeElapsed, Data2));
        data3Series.getData().add(new XYChart.Data<>(timeElapsed, Data3));
        dataController.setSendTimeElapsed(timeElapsed);

        if (timeElapsed > X_AXIS_UPPER_BOUND) {
            xAxis1.setLowerBound(xAxis1.getLowerBound() + UPDATE_INTERVAL);
            xAxis1.setUpperBound(xAxis1.getUpperBound() + UPDATE_INTERVAL);

            xAxis2.setLowerBound(xAxis2.getLowerBound() + UPDATE_INTERVAL);
            xAxis2.setUpperBound(xAxis2.getUpperBound() + UPDATE_INTERVAL);

            xAxis3.setLowerBound(xAxis3.getLowerBound() + UPDATE_INTERVAL);
            xAxis3.setUpperBound(xAxis3.getUpperBound() + UPDATE_INTERVAL);

            if (data1Series.getData().size() > 0) {
                data1Series.getData().remove(0);
                data2Series.getData().remove(0);
                data3Series.getData().remove(0);
            }
        }

        timeElapsed += UPDATE_INTERVAL;

    }
    @FXML
    private void handleExportPaneAction(MouseEvent event) {
        if (serialController != null && serialController.isPortOpen()) {
            // Inform the user that they need to close the port first
            // You could use a Label or a dialog to inform the user
            System.out.println("Please close the COM port before going back.");
            comWarning.setText("*Please Close the COM port before Exporting.");
            // You can set a message on the UI here if you have a status label or similar.
        } else {
            comWarning.setText("");
            exportAnchorPaneAsImage();
        }

    }
    @FXML
    private void handleExportCSV(MouseEvent event) {
        if (serialController != null && serialController.isPortOpen()) {
            // Inform the user that they need to close the port first
            // You could use a Label or a dialog to inform the user
            System.out.println("Please close the COM port before going back.");
            comWarning.setText("*Please Close the COM port before Exporting.");
            // You can set a message on the UI here if you have a status label or similar.
        } else if (serialController != null && (TimeStamplabel.getText().isEmpty() || TimeStamp == 0)) {
            // Inform the user that they need to close the port first
            // You could use a Label or a dialog to inform the user
            System.out.println("Data is Empty.");
            comWarning.setText("*The Data is Empty.");
            // You can set a message on the UI here if you have a status label or similar.
        } else {
            comWarning.setText("");
            dataController.triggerSaveDataWithDialog(ecgExport.getScene().getWindow());
        }

    }

    @FXML
    private void handleClearButtonAction(MouseEvent event) {
        if (serialController != null && serialController.isPortOpen()) {
            // Inform the user that they need to close the port first
            // You could use a Label or a dialog to inform the user
            System.out.println("Please close the COM port before going back.");
            comWarning.setText("*Please Close the COM port before Clear Data.");
            // You can set a message on the UI here if you have a status label or similar.
        } else {
            comWarning.setText("");
            System.out.println("It's Working!");
            clearChartData();
        }

    }
    public void exportAnchorPaneAsImage() {
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
        if (serialController != null && serialController.isPortOpen()) {
            // Inform the user that they need to close the port first
            // You could use a Label or a dialog to inform the user
            System.out.println("Please close the COM port before going back.");
            comWarning.setText("*Please Close the COM port before going back.");
            // You can set a message on the UI here if you have a status label or similar.
        }
        else if (serialController != null && (data1Series.getData().size()>1) && (data2Series.getData().size()>1) && (data3Series.getData().size()>1)) {
            // Inform the user that they need to close the port first
            // You could use a Label or a dialog to inform the user
            System.out.println("Please clear the Data before going back.");
            comWarning.setText("*Please Clear the Data before going back.");
            // You can set a message on the UI here if you have a status label or similar.
        }
        else {
            try {
                // Navigate back to the previous FXML screen
                // Change "previous.fxml" to the actual name of your previous FXML file
                Parent previousPage = FXMLLoader.load(getClass().getResource("Index.fxml"));
                Scene previousScene = new Scene(previousPage);
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
    }

    public void setSerialController(SerialController serialController) {
        this.serialController = serialController;
    }
    @FXML
    public void initialize() {
        System.out.println("PlotController intialized");
        dataController = new DataController();
        // Now you can set the DataController instance in SerialController
        serialController.setDataController(dataController);
        // Also set PlotController in DataController if needed
        dataController.setPlotController(this);


        data1Series.setName("Data 1");
        data2Series.setName("Data 2");
        data3Series.setName("Data 3");

        data1LineChart.getData().add(data1Series);
        data2LineChart.getData().add(data2Series);
        data3LineChart.getData().add(data3Series);

        xAxis1.setAutoRanging(false);
        xAxis1.setLowerBound(X_AXIS_LOWER_BOUND);
        xAxis1.setUpperBound(X_AXIS_UPPER_BOUND);
        xAxis1.setTickUnit(100); // Optional: adjust as needed

        yAxis1.setAutoRanging(false);

        xAxis2.setAutoRanging(false);
        xAxis2.setLowerBound(X_AXIS_LOWER_BOUND);
        xAxis2.setUpperBound(X_AXIS_UPPER_BOUND);
        xAxis2.setTickUnit(100); // Optional: adjust as needed

        yAxis2.setAutoRanging(false);

        xAxis3.setAutoRanging(false);
        xAxis3.setLowerBound(X_AXIS_LOWER_BOUND);
        xAxis3.setUpperBound(X_AXIS_UPPER_BOUND);
        xAxis3.setTickUnit(100); // Optional: adjust as needed

        yAxis3.setAutoRanging(false);


        gridPane1.widthProperty().addListener((observable, oldValue, newValue) -> drawEcgGrid(gridPane1));
        gridPane1.heightProperty().addListener((observable, oldValue, newValue) -> drawEcgGrid(gridPane1));
        gridPane2.widthProperty().addListener((observable, oldValue, newValue) -> drawEcgGrid(gridPane2));
        gridPane2.heightProperty().addListener((observable, oldValue, newValue) -> drawEcgGrid(gridPane2));
        gridPane3.widthProperty().addListener((observable, oldValue, newValue) -> drawEcgGrid(gridPane3));
        gridPane3.heightProperty().addListener((observable, oldValue, newValue) -> drawEcgGrid(gridPane3));
    }
}

