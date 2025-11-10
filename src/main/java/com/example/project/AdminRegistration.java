package com.example.project;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class AdminRegistration {

    @FXML private TextField name;
    @FXML private TextField pass;
    @FXML private TextField confirmPass;
    @FXML private TextField mobo;
    @FXML private Label errorMessage;
    @FXML private Button backToMain;

    private Scene scene;
    private Parent root;
    private Stage stage;
    private SocketWrapper socketWrapper;

    public AdminRegistration() {
        try {
            socketWrapper = new SocketWrapper("127.0.0.1", 44444);
        } catch (IOException e) {
            errorMessage.setText("Failed to connect to server: " + e.getMessage());
        }
    }

    public void backToMain(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("userInterface.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void reqReg(ActionEvent event) {
        String s1 = name.getText().trim();           // username
        String s2 = pass.getText().trim();           // password
        String s3 = confirmPass.getText().trim();    // confirm password
        String s4 = mobo.getText().trim();           // mobile number

        // Validate name
        if (s1.length() != 7) {
            errorMessage.setText("Username must be exactly 7 characters.");
            return;
        }

        // Validate mobile
        if (!s4.matches("\\d{11}")) {
            errorMessage.setText("Mobile number must be exactly 11 digits.");
            return;
        }

        // Validate password
        if (s2.length() != 8) {
            errorMessage.setText("Password must be exactly 8 characters.");
            return;
        }

        // Confirm password
        if (!s2.equals(s3)) {
            errorMessage.setText("Passwords do not match.");
            return;
        }

        try {
            Message request = new Message();
            request.setFrom(s1);
            request.setTo("REGISTER_ADMIN");
            request.setText(s1 + ";" + s2 + ";" + s4);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            errorMessage.setText(response.getText());
        } catch (IOException | ClassNotFoundException e) {
            errorMessage.setText("Server communication error: " + e.getMessage());
        }
    }
}