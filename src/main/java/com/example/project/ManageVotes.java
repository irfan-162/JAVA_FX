package com.example.project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ManageVotes implements Initializable {

    @FXML private Button terminate;
    @FXML private ListView<String> manageList;
    @FXML private Label details;
    @FXML private Label labelOnOff;
    @FXML private Button mainMenu;
    @FXML private Button adminMenu;

    @FXML private Label reg;
    @FXML private Label info;

    private Scene scene;
    private Parent root;
    private Stage stage;
    private SocketWrapper socketWrapper;

    public ManageVotes() {
        try {
            socketWrapper = new SocketWrapper("127.0.0.1", 44444);
        } catch (IOException e) {
            details.setText("Failed to connect to server: " + e.getMessage());
        }
    }

    public void setToAdminMode(){

        reg.setVisible(true);
        info.setVisible(false);
        adminMenu.setVisible(true);
        terminate.setVisible(true);
    }

    public void setToNormalMode(){

        reg.setVisible(false);
        info.setVisible(true);
        adminMenu.setVisible(false);
        terminate.setVisible(false);

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadRegions();
        manageList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateLabels(newSelection);
            }
        });
    }

    private void loadRegions() {
        try {
            Message request = new Message();
            request.setFrom("ManageVotes");
            request.setTo("GET_REGIONS");
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            List<String> regions = List.of(response.getText().split(";"));
            manageList.getItems().setAll(regions);
        } catch (IOException | ClassNotFoundException e) {
            manageList.getItems().setAll("Error loading regions!");
        }
    }

    private void updateLabels(String region) {
        try {
            Message request = new Message();
            request.setFrom("ManageVotes");
            request.setTo("GET_REGION_DATA");
            request.setText(region);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String[] parts = response.getText().split(";");
            if (parts.length < 4) {
                details.setText("Error reading region data!");
                return;
            }
            boolean isVoting = parts[0].equals("1");
            labelOnOff.setText(isVoting ? "Voting: ON" : "Voting: OFF");
            if (isVoting) {
                long endTimeMillis = Long.parseLong(parts[1]);
                long remainingMillis = endTimeMillis - System.currentTimeMillis();
                String startTime = parts[2];
                String endTime = parts[3];
                if (remainingMillis > 0) {
                    long hours = remainingMillis / 3600000;
                    long minutes = (remainingMillis % 3600000) / 60000;
                    long seconds = (remainingMillis % 60000) / 1000;
                    details.setText(String.format("%s: %dh %dm %ds remaining, Started: %s, Ends: %s",
                            region, hours, minutes, seconds, startTime, endTime));
                } else {
                    details.setText(String.format("%s: Voting has ended, Started: %s, Ended: %s",
                            region, startTime, endTime));
                    labelOnOff.setText("Voting: OFF");
                }
            } else {
                details.setText(region + ": No active voting");
            }
        } catch (IOException | ClassNotFoundException e) {
            details.setText("Error reading region data!");
        }
    }

    @FXML
    private void terminate(ActionEvent event) {
        String selectedRegion = manageList.getSelectionModel().getSelectedItem();
        if (selectedRegion != null) {
            try {
                Message request = new Message();
                request.setFrom("ManageVotes");
                request.setTo("TERMINATE_VOTE");
                request.setText(selectedRegion);
                socketWrapper.write(request);
                Message response = (Message) socketWrapper.read();
                labelOnOff.setText("Voting: OFF");
                details.setText(selectedRegion + ": " + response.getText());
            } catch (IOException | ClassNotFoundException e) {
                details.setText("Error terminating vote: " + e.getMessage());
            }
        } else {
            details.setText("Please select a region!");
        }
    }

    @FXML
    private void adminMenu(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("adminInterface.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void mainMenu(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("userInterface.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}