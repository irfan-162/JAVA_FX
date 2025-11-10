package com.example.project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ApproveAdmins {

    @FXML
    private ListView<String> reqlist; // ListView for admin request usernames

    @FXML
    private Button approveAdmin; // Button to approve selected admin

    @FXML
    private Label details; // Label to display admin details

    private SocketWrapper socketWrapper;

    public void initialize() {
        try {
            socketWrapper = new SocketWrapper("127.0.0.1", 44444);
            if (reqlist == null || approveAdmin == null || details == null) {
                System.err.println("FXML elements not initialized: reqlist=" + reqlist + ", approveAdmin=" + approveAdmin + ", details=" + details);
                if (details != null) {
                    details.setText("Error: UI components not initialized. Check FXML file.");
                }
                return;
            }
            loadAdminRequests();
            setupListViewListener();
        } catch (IOException e) {
            if (details != null) {
                details.setText("Failed to connect to server: " + e.getMessage());
            }
            System.err.println("Failed to connect to server: " + e.getMessage());
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

    private void loadAdminRequests() {
        try {
            Message request = new Message();
            request.setFrom("AdminPanel");
            request.setTo("GET_ADMIN_REQUESTS");
            request.setText("");
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String responseText = response.getText();

            if (responseText.startsWith("Error")) {
                details.setText(responseText);
                return;
            }

            ObservableList<String> adminRequests = FXCollections.observableArrayList();
            if (!responseText.isEmpty()) {
                adminRequests.addAll(responseText.split(";"));
            }
            reqlist.setItems(adminRequests);
            if (adminRequests.isEmpty()) {
                details.setText("No admin requests available.");
            }
        } catch (IOException | ClassNotFoundException e) {
            details.setText("Error loading admin requests: " + e.getMessage());
        }
    }

    private void setupListViewListener() {
        reqlist.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    Message request = new Message();
                    request.setFrom("AdminPanel");
                    request.setTo("GET_ADMIN_DETAILS");
                    request.setText(newValue);
                    socketWrapper.write(request);
                    Message response = (Message) socketWrapper.read();
                    details.setText(response.getText());
                } catch (IOException | ClassNotFoundException e) {
                    details.setText("Error loading details: " + e.getMessage());
                }
            } else {
                details.setText("Select an admin to view details.");
            }
        });
    }

    @FXML
    private void approveAdmin(ActionEvent event) {
        String selectedAdmin = reqlist.getSelectionModel().getSelectedItem();
        if (selectedAdmin == null) {
            details.setText("Please select an admin to approve.");
            return;
        }

        try {
            Message request = new Message();
            request.setFrom("AdminPanel");
            request.setTo("APPROVE_ADMIN");
            request.setText(selectedAdmin);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            details.setText(response.getText());

            // Clear selection and reload the list after approval
            reqlist.getSelectionModel().clearSelection();
            loadAdminRequests();
        } catch (IOException | ClassNotFoundException e) {
            details.setText("Error approving admin: " + e.getMessage());
        }
    }

    // Ensure socket is closed when the controller is destroyed
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