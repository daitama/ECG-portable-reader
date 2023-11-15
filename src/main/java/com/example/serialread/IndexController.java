package com.example.serialread;

import io.github.palexdev.materialfx.controls.MFXDatePicker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class IndexController {
    /* Class IndexController ini bertanggung jawab untuk menangani seluruh fungsi/method yang
    * berjalan di atas IndexController.fxml.
    * Method untuk tiap-tiap Tab akan dibuat secara terpisah */

    @FXML
    private TabPane tabPane;

    /* ECG REAL-TIME MONITORING TAB
    * Code dibawah ini akan menangani method yang digunakan dalam Tab Real-Time Monitoring.
    * kode @FXML diatas setiap method/object berarti menginjectkan object tersebut pada file
    * .fxml yang terhubung dengan controler ini sehingga controller ini dapat memanipulasi
    *  component pada suatu FXML */


    /*Berikut ini adalah inisialisasi tiap component yang akan dimanipulasi oleh controller ini*/
    @FXML
    private TextField namaPasien; //Component TextField untuk memasukan namaPasien.
    @FXML
    private MFXDatePicker tanggalLahir; //Component DatePicker untuk memasukkan tanggal lahir pasien.
    @FXML
    private ChoiceBox<String> genderType; //Component ChoiceBox untuk memilih Jenis Kelamin Pasien.
    @FXML
    private Label nameWarning, genderWarning, dateWarning; //Component Label Transparent yang akan muncul ketika
                                                           //input data kosong.
    private final String[] gender = {"Laki-Laki","Perempuan"}; //List dari choice untuk diinject ke component ChoiceBox.

    /*Ini adalah method untuk menghandle Button START ketika diklik*/
    @FXML
    protected void handleStartButton(ActionEvent event) {
        // Melakukan Validasi sebelum melanjutkan.
        if (!namaPasien.getText().trim().isEmpty() && //Jika Tiap Field terisi (Data Valid)
                tanggalLahir.getValue() != null &&
                genderType.getValue() != null) {
            try {
                nameWarning.setText("");
                dateWarning.setText("");
                genderWarning.setText("");
                submit(event); // Eksekusi method submit untuk mengirim data ke fxml selanjutnya.
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception, maybe show an error message to the user
            }
        }
        if(namaPasien.getText().trim().isEmpty()){ //Jika Field namaPasien kosong.
            nameWarning.setText("*Nama tidak boleh kosong");
        }
        if(tanggalLahir.getValue() == null){ //Jika Field tanggalLahir kosong
            dateWarning.setText("*Tanggal Lahir tidak boleh kosong");
        }
        if(genderType.getValue() == null){ //Jika Field Jenis Kelamin kosong.
            genderWarning.setText("*Jenis Kelamin tidak boleh kosong");
        }
        // Otherwise, you can set error messages or prompts to fill all the details
    }
    protected void submit(ActionEvent event) throws IOException {
        /*Method ini akan mengambil data dari tiap-tiap Field yang terisi kemudian mengirimkannya ke
        * Controller class yang bertanggung jawab mengangani plotController.fxml*/
        FXMLLoader loader = new FXMLLoader(getClass().getResource("plotController.fxml"));
        Parent root = loader.load();
        PlotController plotControllerClass = loader.getController();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
        String nama = namaPasien.getText();
        String kelamin = genderType.getValue();
        LocalDate lahir = tanggalLahir.getValue();
        plotControllerClass.setIdentity(nama, kelamin, lahir); //Dalam class controller milik plotController terdapat method untuk
        stage.show();                                          //Menerima data yang dikirim dari class ini.
    }

    /* ECG DATA PLOT TAB
     * Code dibawah ini akan menangani method yang digunakan dalam Tab ECG Data Plot.
     * kode @FXML diatas setiap method/object berarti menginjectkan object tersebut pada file
     * .fxml yang terhubung dengan controler ini sehingga controller ini dapat memanipulasi
     *  component pada suatu FXML */

    /*Berikut ini adalah inisialisasi tiap component yang akan dimanipulasi oleh controller ini*/
    @FXML
    private TextField namaFile;
    @FXML
    private Label fileWarning;

    private String filePath;
    private File file;

    @FXML
    protected void handleBrowseButton(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV File");
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)","*.csv");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show open file dialog
        file = fileChooser.showOpenDialog(namaFile.getScene().getWindow());

        if(file != null){
            namaFile.setText(file.getName());
            filePath = file.getAbsolutePath();
            System.out.println("Get Files from :" + filePath);
        }
    }

    @FXML
    protected void handlePlotButton(ActionEvent event){
        if (file != null){
            try {
                fileWarning.setText("");
                Plot(event); // Now pass the event along
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception, maybe show an error message to the user
            }
        } else {
            fileWarning.setText("*Pilih Data terlebih dahulu.");
            System.out.println("Data Kosong");
        }
    }

    protected void Plot(ActionEvent event) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("historicPlotController.fxml"));
        Parent root = loader.load();
        HistoricPlotController historicPlotControllerClass = loader.getController();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scene);
        historicPlotControllerClass.setfilePath(filePath);
        stage.show();
    }

    /* Ini adalah code yang akan dijalankan sekali ketika sebuah FXML ditampilkan*/
    @FXML
    public void initialize() {
        genderType.getItems().addAll(gender); //Memasukan list dari Array 'gender' ke dalam ChoiceBox 'genderType'
    }
}
