/*package com.example.project;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.*;

import java.io.*;
import java.net.URL;
import java.util.*;

public class VoterApproval implements Initializable {

    @FXML
    private ListView<String> userListView;

    private Map<String, String> userPasswordMap = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        File file = new File("VoterRequests.txt");
        if (!file.exists()) {
            showError("VoterRequests.txt not found.");
            return;
        }

        ObservableList<String> users = FXCollections.observableArrayList();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.strip().split("\\s+");
                if (parts.length == 2) {
                    String username = parts[0];
                    String password = parts[1];
                    userPasswordMap.put(username, password);
                    users.add(username);
                }
            }
        } catch (IOException e) {
            showError("Failed to read VoterRequests.txt: " + e.getMessage());
        }

        userListView.setItems(users);
    }

    @FXML
    private void handleAdd() {
        String selectedUser = userListView.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showError("Please select a username.");
            return;
        }

        String password = userPasswordMap.get(selectedUser);
        String region = "Dhaka";
        int flag = 0;

        String lineToWrite = String.format("%-12s %-10s %-6s %d%n", selectedUser, password, region, flag);

        File output = new File("Voters.txt");

        try (FileWriter fw = new FileWriter(output, true)) {
            fw.write(lineToWrite);
            showInfo("User added to Voters.txt.");
        } catch (IOException e) {
            showError("Failed to write to Voters.txt: " + e.getMessage());
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
import java.util.Objects;
import java.util.ResourceBundle;

public class VoterApproval implements Initializable {

    @FXML
    private ListView<String> reqlist;
    @FXML
   private Label details;


    private SocketWrapper socketWrapper;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socketWrapper = new SocketWrapper("127.0.0.1", 44444);
           /* if (userListView == null || addButton == null) {
                showError("UI components not initialized. Check FXML file.");
                return;
            }*/
            loadVoterRequests();
            reqlist.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    try {
                        updateLabels(newSelection);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

        } catch (IOException e) {
            showError("Failed to connect to server: " + e.getMessage());
        }
    }
    private Scene scene;
    private Parent root;
    private Stage stage;

    @FXML public void back(ActionEvent event) throws IOException {

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(("adminInterface.fxml"))));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void updateLabels(String voter) throws IOException, ClassNotFoundException {

        Message request = new Message();
        request.setFrom("VoterApproval");
        request.setTo("GET_V_R_DETAILS");
        request.setText(voter);
        socketWrapper.write(request);
        Message response = (Message) socketWrapper.read();
        String d = response.getText();
String [] parts=d.split(";");

        details.setText("Mobile Number : "+parts[2]+"\n"+"Region : "+parts[3]);
    }

    private void loadVoterRequests() {
        try {
            Message request = new Message();
            request.setFrom("VoterApproval");
            request.setTo("GET_VOTER_REQUESTS");
            request.setText("");
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String responseText = response.getText();

            if (responseText.startsWith("Error")) {
                showError(responseText);
                return;
            }

            ObservableList<String> users = FXCollections.observableArrayList();
            if (!responseText.isEmpty()) {
                users.addAll(responseText.split(";"));
            }
            reqlist.setItems(users);
            if (users.isEmpty()) {
                showInfo("No voter requests available.");
            }
        } catch (IOException | ClassNotFoundException e) {
            showError("Error loading voter requests: " + e.getMessage());
        }
    }

    @FXML
    private void approveVoter (ActionEvent event) {
        String selectedUser = reqlist.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Please select a username.");
            return;
        }

        try {
            Message request = new Message();
            request.setFrom("VoterApproval");
            request.setTo("APPROVE_VOTER");
            request.setText(selectedUser);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String responseText = response.getText();

            if (responseText.startsWith("Error")) {
                showError(responseText);
            } else {
                showInfo(responseText);
                reqlist.getSelectionModel().clearSelection();
                loadVoterRequests();
            }
        } catch (IOException | ClassNotFoundException e) {
            showError("Error approving voter: " + e.getMessage());
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
