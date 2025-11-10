/*package com.example.project;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.*;

import java.io.*;
import java.net.URL;
import java.util.*;

public class VoterManager implements Initializable {

    @FXML
    private ListView<String> voterListView;

    @FXML
    private TextField regionInput;

    @FXML
    private Button addRegionButton;

    private List<String[]> voterData = new ArrayList<>(); // Stores full lines [username, password, region, flag]

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadVoters();
        regionInput.setVisible(false);
        addRegionButton.setVisible(false);
    }

    private void loadVoters() {
        voterData.clear();
        File file = new File("Voters.txt");

        if (!file.exists()) {
            showError("Voters.txt not found.");
            return;
        }

        ObservableList<String> displayList = FXCollections.observableArrayList();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null && !line.strip().isEmpty()) {
                String[] parts = line.strip().split("\\s+");
                if (parts.length == 4) {
                    voterData.add(parts);
                    displayList.add(parts[0] + " " + parts[2]); // username + region
                }
            }
        } catch (IOException e) {
            showError("Error reading Voters.txt: " + e.getMessage());
        }

        voterListView.setItems(displayList);
    }

    @FXML
    private void handleRemove() {
        int selectedIndex = voterListView.getSelectionModel().getSelectedIndex();

        if (selectedIndex == -1) {
            showError("Please select a voter to remove.");
            return;
        }

        voterData.remove(selectedIndex);
        saveVoters();
        loadVoters();
        showInfo("Voter removed.");
    }

    @FXML
    private void handleChangeRegion() {
        if (voterListView.getSelectionModel().getSelectedIndex() == -1) {
            showError("Select a voter to change region.");
            return;
        }

        regionInput.setVisible(true);
        addRegionButton.setVisible(true);
    }

    @FXML
    private void handleAddRegion() {
        int selectedIndex = voterListView.getSelectionModel().getSelectedIndex();
        String newRegion = regionInput.getText().trim();

        if (newRegion.isEmpty()) {
            showError("Enter a region.");
            return;
        }

        // Update region in the selected voter data
        voterData.get(selectedIndex)[2] = newRegion;
        saveVoters();
        loadVoters();

        regionInput.clear();
        regionInput.setVisible(false);
        addRegionButton.setVisible(false);

        showInfo("Region updated.");
    }

    private void saveVoters() {
        try (FileWriter fw = new FileWriter("Voters.txt", false)) {
            for (String[] parts : voterData) {
                fw.write(String.join(" ", parts) + "\n");
            }
        } catch (IOException e) {
            showError("Failed to save Voters.txt: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }
}*/

package com.example.project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class VoterManager implements Initializable {

    @FXML
    private ListView<String> voterListView;

    @FXML
    private TextField regionInput;

    @FXML
    private Button addRegionButton;

    private List<String[]> voterData = new ArrayList<>(); // Stores [username, password, region, flag]
    private SocketWrapper socketWrapper;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socketWrapper = new SocketWrapper("127.0.0.1", 44444);
            if (voterListView == null || regionInput == null || addRegionButton == null) {
                showError("UI components not initialized. Check FXML file.");
                return;
            }
            regionInput.setVisible(false);
            addRegionButton.setVisible(false);
            loadVoters();
        } catch (IOException e) {
            showError("Failed to connect to server: " + e.getMessage());
        }
    }

    private Scene scene;
    private Parent root;
    private Stage stage;

  @FXML  public void back(ActionEvent event) throws IOException {

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(("adminInterface.fxml"))));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void loadVoters() {
        voterData.clear();
        try {
            Message request = new Message();
            request.setFrom("VoterManager");
            request.setTo("GET_VOTERS");
            request.setText("");
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String responseText = response.getText();

            if (responseText.startsWith("Error")) {
                showError(responseText);
                return;
            }

            ObservableList<String> displayList = FXCollections.observableArrayList();
            if (!responseText.isEmpty()) {
                String[] voterEntries = responseText.split(";");
                for (String entry : voterEntries) {
                    String[] parts = entry.split(",");
                    if (parts.length == 4) {
                        voterData.add(parts);
                        displayList.add(parts[0] + " " + parts[2]); // username + region
                    }
                }
            }
            voterListView.setItems(displayList);
            if (displayList.isEmpty()) {
                showInfo("No voters available.");
            }
        } catch (IOException | ClassNotFoundException e) {
            showError("Error loading voters: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemove(ActionEvent event) {
        int selectedIndex = voterListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1) {
            showError("Please select a voter to remove.");
            return;
        }

        String username = voterData.get(selectedIndex)[0];
        try {
            Message request = new Message();
            request.setFrom("VoterManager");
            request.setTo("REMOVE_VOTER");
            request.setText(username);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String responseText = response.getText();

            if (responseText.startsWith("Error")) {
                showError(responseText);
            } else {
                showInfo(responseText);
                loadVoters();
            }
        } catch (IOException | ClassNotFoundException e) {
            showError("Error removing voter: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangeRegion(ActionEvent event) {
        if (voterListView.getSelectionModel().getSelectedIndex() == -1) {
            showError("Select a voter to change region.");
            return;
        }
        regionInput.setVisible(true);
        addRegionButton.setVisible(true);
    }

    @FXML
    private void handleAddRegion(ActionEvent event) {
        int selectedIndex = voterListView.getSelectionModel().getSelectedIndex();
        String newRegion = regionInput.getText().trim();

        if (newRegion.isEmpty()) {
            showError("Enter a region.");
            return;
        }

        String username = voterData.get(selectedIndex)[0];
        try {
            Message request = new Message();
            request.setFrom("VoterManager");
            request.setTo("CHANGE_VOTER_REGION");
            request.setText(username + ";" + newRegion);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String responseText = response.getText();

            if (responseText.startsWith("Error")) {
                showError(responseText);
            } else {
                showInfo(responseText);
                loadVoters();
                regionInput.clear();
                regionInput.setVisible(false);
                addRegionButton.setVisible(false);
            }
        } catch (IOException | ClassNotFoundException e) {
            showError("Error updating region: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }

    public void shutdown() {
        try {
            if (socketWrapper != null) {
                socketWrapper.closeConnection();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }
}
