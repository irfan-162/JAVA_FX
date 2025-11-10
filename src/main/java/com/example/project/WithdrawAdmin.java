package com.example.project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.Optional;

public class WithdrawAdmin {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button confirmButton;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox confirmBox;

    private String matchedUsername;
    private String matchedPassword;

    @FXML
    public void initialize() {
        confirmBox.setVisible(false);
        statusLabel.setText("");
    }

    @FXML
    private void handleCheckCredentials() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Username and password required.");
            return;
        }

        File file = new File("Admins.txt");
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.strip().split("\\s+");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    found = true;
                    matchedUsername = username;
                    matchedPassword = password;
                    break;
                }
            }
        } catch (IOException e) {
            statusLabel.setText("Error reading Admins.txt.");
            return;
        }

        if (found) {
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("Admin found. Click Confirm Withdraw.");
            confirmBox.setVisible(true);
        } else {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Invalid username or password.");
            confirmBox.setVisible(false);
        }
    }

    @FXML
    private void handleConfirmWithdraw() {
        if (matchedUsername == null || matchedPassword == null) return;

        // Confirm dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Withdraw");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to withdraw this admin?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        File inputFile = new File("Admins.txt");
        File tempFile = new File("Admins_temp.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                String trimmed = currentLine.trim();
                if (!trimmed.equals(matchedUsername + " " + matchedPassword)) {
                    writer.println(trimmed);
                }
            }

        } catch (IOException e) {
            statusLabel.setText("Error processing file.");
            return;
        }

        // Replace file
        if (inputFile.delete()) {
            tempFile.renameTo(inputFile);
        }

        switchToUserInterface();
    }

    private void switchToUserInterface() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("userInterface.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Failed to switch scene.");
        }
    }
}
