package com.example.serialread;

import com.fazecast.jSerialComm.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

/* SerialController ini bertanggung jawab menangani serialController.fxml yang akan diinclude dalam plotController.fxml
*  Class ini bertanggung jawab sepenuhnya dalam menangani Serial COM Communication.
*  Data yang diterima dari SerialCOM akan diteruskan ke class Data Controller. */
public class SerialController {
    private static final int BAUD_RATE = 115200; //Sesuaikan dengan Baud rate yang diperlukan.
    private boolean isPortOpen = false;
    private SerialPort comPort;
    private DataController dataControllerClass;

    // Method to check if the port is open
    public boolean isPortOpen() {
        return isPortOpen;
    }

    /*Berikut ini adalah inisialisasi tiap component yang akan dimanipulasi oleh controller ini*/
    @FXML
    private Button portButton;
    @FXML
    private ChoiceBox<String> portList;
    @FXML
    private  Label portWarning;
    private SerialPort[] availablePorts;

    public void setDataController(DataController datacontroller){
        dataControllerClass = datacontroller;
    }



    @FXML
    // Method ini akan di initialize diawal untuk load COMPort List yang dimasukkan pada portList ChoiceBox;
    // Method ini terhubung dengan Button Refresh
    protected void loadCom(){
        portList.getItems().clear();
        availablePorts = SerialPort.getCommPorts();
        for (SerialPort availablePort : availablePorts) {
            String systemPortName = availablePort.getDescriptivePortName();
            portList.getItems().add(systemPortName);
            System.out.println(systemPortName);
        }
    }

    @FXML
    /*Method ini berfungsi untuk membuka dan menutup COMPort yang terpilih, kemudian meneruskan ke method openCom() untuk
     mengakuisisi data via Serial COM.
    Method ini terhubung dengan tombol Open/Close. */
    protected void toggleCom() {
        if (isPortOpen) {
            // Close the port and update UI
            comPort.closePort();
            System.out.println("Port Closed Successfully!");
            portWarning.setText("Port Closed Successfully!");
            portButton.setStyle(
                    "-fx-background-color: #8BC34A;" +
                            "-fx-background-radius: 100px;" +
                            "-fx-border-radius: 100px;"
            );
            dataControllerClass.showLoss();
            portButton.setText("Open");
            isPortOpen = false;
        } else {
            openCom();
        }
    }

    @FXML
    protected void openCom(){
        int selectedPort = portList.getSelectionModel().getSelectedIndex();
        System.out.println("Selected Index" + selectedPort);
        if (selectedPort >= 0) {
            isPortOpen = true;
            portButton.setText("Close");
            portButton.setStyle(
                    "-fx-background-color: #d0342c;" +
                            "-fx-background-radius: 100px;" +
                            "-fx-border-radius: 100px;"
            );
            portWarning.setText("Port Opened Successfully!");
            comPort = availablePorts[selectedPort];
            comPort.setBaudRate(BAUD_RATE);
            try {
                if (comPort.openPort()) {
                    System.out.println("Port " + comPort.getSystemPortName() + " is open ");

                    comPort.addDataListener(new SerialPortDataListener() {
                        final StringBuilder buffer = new StringBuilder();

                        @Override
                        public int getListeningEvents() {
                            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                        }

                        @Override
                        public void serialEvent(SerialPortEvent event) {
                            if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                                return;
                            }

                            byte[] newData = new byte[comPort.bytesAvailable()];
                            int numRead = comPort.readBytes(newData, newData.length);

                            for (int i = 0; i < numRead; i++) {
                                char readChar = (char) newData[i];
                                if (readChar == '\n') {
                                    String receivedString = buffer.toString();
                                    buffer.setLength(0); // Clear the buffer
                                    // Check if the DataController instance is set before calling its method
                                    if (dataControllerClass != null) {
                                        dataControllerClass.setSendData(receivedString); //Mengirimkan Data ke DataController untuk diproses.
                                    } else {
                                        System.err.println("DataController instance is not set!");
                                    }

                                } else {
                                    buffer.append(readChar);
                                }
                            }
                        }
                    });
                } else {
                    System.out.println("Failed to open port");
                }
            } catch (Exception e) {
                portWarning.setText("Failed to open the port!");
                System.err.println("Failed to open the port: " + e.getMessage());
            }
        } else {
            portWarning.setText("Select the Port first!");
        }
    }

    @FXML
    public void initialize(){
        System.out.println("SerialController intialized");
        portButton.setStyle(
                "-fx-background-color: #8BC34A;" +
                "-fx-background-radius: 100px;" +
                "-fx-border-radius: 100px;"
                );

        loadCom();
    }
}