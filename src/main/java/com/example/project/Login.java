package com.example.project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Login {

    @FXML private Label lp;
    @FXML private PasswordField password;
    @FXML private TextField userID;
    @FXML private Button loginButton;
    @FXML private Button returnToMain;
    @FXML private Label welcome;

    private Scene scene;
    private Parent root;
    private Stage stage;
    private SocketWrapper socketWrapper;

    public Login() {
        try {
            socketWrapper = new SocketWrapper("127.0.0.1", 44444);
        } catch (IOException e) {
            lp.setText("Failed to connect to server: " + e.getMessage());
        }
    }

    public void tryLogin(ActionEvent event) throws IOException {
        checkLogin(event);
    }

    private AdminInterface ai;

    public void setAI(AdminInterface ai) {
        this.ai = ai;
    }

    public void welcome() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("adminInterface.fxml"));
        root = loader.load();
        ai = loader.getController();
        if (ai != null) {
            ai.setWtext(userID.getText());
        }
    }

    public void returnToMain(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("userInterface.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void switchScene(ActionEvent event, String fxmlFile, String voterID, String region, int voterFlag) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        if (fxmlFile.equals("castVote.fxml")) {
            CastVoteController controller = loader.getController();
            controller.setVoterData(voterID, region, voterFlag);
        }
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void checkLogin(ActionEvent event) throws IOException {
        String inputUser = userID.getText().trim();
        String inputPass = password.getText().trim();

        try {
            Message request = new Message();
            request.setFrom(inputUser);
            request.setTo("CHECK_LOGIN");
            request.setText(inputUser + ";" + inputPass);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String result = response.getText();

            if (result.startsWith("ADMIN_SUCCESS")) {
                lp.setText("Admin Login Success!!");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("adminInterface.fxml"));
                Parent adminRoot = loader.load();
                AdminInterface aiController = loader.getController();
                if (aiController != null) {
                    aiController.setWtext(inputUser);
                }
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                scene = new Scene(adminRoot);
                stage.setScene(scene);
                stage.show();
            } else if (result.startsWith("VOTER_SUCCESS")) {
                String[] parts = result.split(";");
                if (parts.length >= 4) {
                    String voterID = parts[1];
                    String voterRegion = parts[2];
                    int voterFlag = Integer.parseInt(parts[3]);
                    lp.setText("Voter Login Success!!");
                    switchScene(event, "castVote.fxml", voterID, voterRegion, voterFlag);
                } else {
                    lp.setText("Invalid voter data from server");
                }
            } else {
                lp.setText(result);
            }
        } catch (ClassNotFoundException e) {
            lp.setText("Server communication error: " + e.getMessage());
        }
    }
}