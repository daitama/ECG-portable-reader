package com.example.serialread;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/* DataController tidak memiliki keterikatan dengan fxmlnya sendiri, class ini bertanggung jawab
*  untuk menangani pemrosesan data yang diterima dari SerialController. Data yang diproses akan
*  diteruskan ke PlotController dan disaat yang sama disimpan ke dalam sebuah Array untuk
*  menunggu perintah dari PlotController untuk disimpan dalam bentuk '.csv' */
public class DataController {
    // Inisialisasi variable untuk menyimpan pembacaan data yang sudah diproses
    private int TimeStamp;
    private int Data1;
    private int Data2;
    private int Data3;
    private int totalLoss = 0; // Variable ini akan menghitung total data yang hilang/ terskip.

    private String Nama;
    private String Gender;
    private String TanggalLahir;
    private int Umur;

    //ArrayList untuk store Historical Data.
    private ArrayList<Integer> historicTimeStamp = new ArrayList<>();
    private ArrayList<Integer> historicData1 = new ArrayList<>();
    private ArrayList<Integer> historicData2 = new ArrayList<>();
    private ArrayList<Integer> historicData3 = new ArrayList<>();
    private ArrayList<Double> timeElapsed = new ArrayList<>();
    private ArrayList<Integer> lossTimeStamp = new ArrayList<>();
    private ArrayList<Integer> lossData1 = new ArrayList<>();
    private ArrayList<Integer> lossData2 = new ArrayList<>();
    private ArrayList<Integer> lossData3 = new ArrayList<>();

    private int previousTimeStamp = -1;
    private boolean dataIsValid = false;
    private PlotController plotControllerClass;

    public void setPlotController (PlotController plotcontroller){
        plotControllerClass = plotcontroller;
    }

    public void setSendData(String data) {
        processData(data);
    }
    public void setSendTimeElapsed(double timeelapsed){
        timeElapsed.add(timeelapsed);
    }

    //Menerima Data dari SerialController, untuk diproses (memisah->validasi->simpan)
    public void processData(String receivedString) {
        // Process received string
        String[] values = receivedString.split(","); // Membagi data yang terpisah dengan koma(',')
        if (values.length == 3) {                          // Menjadi Array
            try {
                int currentTimeStamp = Integer.parseInt(values[0].trim());

                if (currentTimeStamp > previousTimeStamp) { //Menyimpan Data yang terpisah ke dalam masing-masing variable
                    TimeStamp = currentTimeStamp;           //Dan memastikan Data valid dengan TimestampSebelum < TimeStampSekarang
                    Data1 = Integer.parseInt(values[1].trim());
                    Data2 = Integer.parseInt(values[2].trim());
                    Data3 = Data2-Data1;

                    previousTimeStamp = TimeStamp;  // Update the previous timestamp with the current one
                    dataIsValid = true;
                    verify();

                } else {
                    System.out.println("Skipped data with lower timestamp"); //Menghitung Data yang tidak valid, dan menyimpan
                    lossTimeStamp.add(currentTimeStamp);                     //Valuenya ke dalam Array.
                    lossData1.add(Integer.parseInt(values[1].trim()));
                    lossData2.add(Integer.parseInt(values[2].trim()));
                    lossData3.add((Integer.parseInt(values[2].trim()))-(Integer.parseInt(values[1].trim())));
                    totalLoss++;
                    dataIsValid = false;
                }

            } catch (NumberFormatException e) {
                System.err.println("Error parsing received data: " + e.getMessage());
            }
        }
    }

    public boolean isDataValid() {
        return dataIsValid;
    }

    public void verify() {
        if (isDataValid()) {
            // Display or process TimeStamp, Data1, Data2
            System.out.println("Received Data: " + TimeStamp + "," + Data1 + "," + Data2 + "," + Data3);
            System.out.println("Timestamp: " + TimeStamp);
            System.out.println("ECG Data1: " + Data1);
            System.out.println("ECG Data2: " + Data2);
            System.out.println("ECG Data3: " + Data3);
            //Store Data ke Array
            historicTimeStamp.add(TimeStamp);
            historicData1.add(Data1);
            historicData2.add(Data2);
            historicData3.add(Data3);

            //Tempat buat plot data ke grafik
            Platform.runLater(() -> {
                if (plotControllerClass != null) {
                    plotControllerClass.setSendValue(TimeStamp, Data1, Data2, Data3); //Kirimkan Data yang sudah valid
                } else {                                                             // ke PlotController untuk ditampilkan.
                    System.out.println("PlotController is not initialized!");
                }
            });
        } else {
            // Skip or do nothing
        }
    }

    // Method to trigger the save dialog from the GUI
    public void triggerSaveDataWithDialog(Window primaryStage) {
        // Make sure this is run on the JavaFX Application Thread
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Data to CSV");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fileChooser.showSaveDialog(primaryStage);

            if (file != null) {
                saveDataToCSV(file.getAbsolutePath());
            }
        });
    }

    //Terima Data Identitas dari Plot Controller
    public void setSendPatientData(String nama, String gender, String tanggalLahir, int umur){
        Nama = nama;
        Gender = gender;
        TanggalLahir = tanggalLahir;
        Umur = umur;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        // Note: This method must be run on the JavaFX Application Thread.
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header
        alert.setContentText(content);

        alert.showAndWait(); // Show the alert and wait for the user to close it.
    }


    // Method to save the data to a CSV file
    public void saveDataToCSV(String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            // Write the header
            bw.write("TimeStamp;Data1;Data2;Data3;TimeElapsed;LossTimeStamp;LossData1;LossData2;LossData3;TotalDataLoss;NamaPasien;GenderPasien;TanggalLahirPasien;UmurPasien");
            bw.newLine();
            String line = historicTimeStamp.get(0) + ";" + historicData1.get(0) + ";" + historicData2.get(0)+ ";" + historicData3.get(0)+ ";" + timeElapsed.get(0) + ";" + lossTimeStamp.get(0) + ";" + lossData1.get(0) + ";" + lossData2.get(0) + ";" + lossData3.get(0) + ";" + totalLoss + ";" + Nama + ";" + Gender + ";" + TanggalLahir + ";" + Umur;
            bw.write(line);
            bw.newLine();

            // Write the data
            for (int i = 1; i < historicTimeStamp.size(); i++) {
                if( i < lossTimeStamp.size()){
                    line =  historicTimeStamp.get(i) + ";" + historicData1.get(i) + ";" + historicData2.get(i)+ ";" + historicData3.get(i) + ";" + timeElapsed.get(i) + ";" + lossTimeStamp.get(i) + ";" + lossData1.get(i) + ";" + lossData2.get(i) + ";" + lossData3.get(i);
                }else{
                    line = historicTimeStamp.get(i) + ";" + historicData1.get(i) + ";" + historicData2.get(i)+ ";" + historicData3.get(i) + ";" + timeElapsed.get(i);
                }
                bw.write(line);
                bw.newLine();

            }
            System.out.println("Data saved to CSV file: " + filePath);
            showAlert("Data Saved", "Data saved to CSV file: " + filePath , AlertType.INFORMATION);
        } catch (IOException e) {
            showAlert("Error Saving Data", "An error occurred while saving the data.", AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Call this method to reset the data, including the previous timestamp
    public void resetData() {
        TimeStamp = 0;
        Data1 = 0;
        Data2 = 0;
        Data3 = 0;
        totalLoss = 0;
        historicTimeStamp.clear();
        historicData1.clear();
        historicData2.clear();
        historicData3.clear();
        timeElapsed.clear();
        lossTimeStamp.clear();
        lossData1.clear();
        lossData2.clear();
        lossData3.clear();
        previousTimeStamp = -1;
        dataIsValid = false;
        // If additional reset logic is needed, add it here
    }

    public void showLoss(){
        System.out.println("Data skipped :" + totalLoss);
        System.out.println("Array size :" + lossTimeStamp.size());
    }




}
