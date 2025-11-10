package com.example.project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminSelectRegion implements Initializable {

    @FXML private Button proceed;
    @FXML private ListView<String> asr;
    @FXML private Label cannotProceed;

    private String selectedRegion;
    private Map<String, Integer> regionFlags = new HashMap<>();
    private SocketWrapper socketWrapper;

    public AdminSelectRegion() {
        try {
            socketWrapper = new SocketWrapper("127.0.0.1", 44444);
        } catch (IOException e) {
            asr.getItems().clear();
            asr.getItems().add("Failed to connect to server: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadRegionsFromServer();
    }

    private void loadRegionsFromServer() {
        regionFlags.clear();
        try {
            Message request = new Message();
            request.setFrom("AdminSelectRegion");
            request.setTo("GET_REGIONS");
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String[] regions = response.getText().split(";");
            asr.getItems().addAll(regions);

            for (String region : regions) {
                Message flagRequest = new Message();
                flagRequest.setFrom("AdminSelectRegion");
                flagRequest.setTo("GET_REGION_FLAG");
                flagRequest.setText(region);
                socketWrapper.write(flagRequest);
                Message flagResponse = (Message) socketWrapper.read();
                try {
                    regionFlags.put(region, Integer.parseInt(flagResponse.getText()));
                } catch (NumberFormatException e) {
                    asr.getItems().clear();
                    asr.getItems().add("Error parsing region flag for " + region);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            asr.getItems().clear();
            asr.getItems().add("Error loading regions: " + e.getMessage());
        }
    }

    @FXML
    private void proceed(ActionEvent event) {
        String selected = asr.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (regionFlags.containsKey(selected) && regionFlags.get(selected) == 0) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("voting.fxml"));
                    Parent root = loader.load();

                    VotingController controller = loader.getController();
                    controller.setRegionText(selected);

                    Stage stage = (Stage) proceed.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException e) {
                    cannotProceed.setText("Error loading voting screen: " + e.getMessage());
                }
            } else {
                cannotProceed.setText("Voting already active for this region.");
            }
        } else {
            cannotProceed.setText("Please select a region.");
        }
    }
}