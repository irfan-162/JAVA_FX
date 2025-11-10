/*package com.example.project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class CheckRegion {

    @FXML private VBox container;
    private String userID;
    private String region;
    private int voterFlag;
    private Scene scene;
    private Parent root;
    private Stage stage;
    private SocketWrapper socketWrapper;

    public CheckRegion() {
        try {
            socketWrapper = new SocketWrapper("127.0.0.1", 44444);
        } catch (IOException e) {
            showMessage("Failed to connect to server: " + e.getMessage(), false);
        }
    }

    public void setVoterData(String userID, String region, int flag) {
        this.userID = userID;
        this.region = region;
        this.voterFlag = flag;

        try {
            int regionFlag = readRegionFlag(region);
            showUIBasedOnFlags(regionFlag, voterFlag);
        } catch (IOException | ClassNotFoundException e) {
            showMessage("Error loading region data: " + e.getMessage(), false);
        }
    }

    private int readRegionFlag(String region) throws IOException, ClassNotFoundException {
        Message request = new Message();
        request.setFrom(userID);
        request.setTo("GET_REGION_FLAG");
        request.setText(region);
        socketWrapper.write(request);
        Message response = (Message) socketWrapper.read();
        return Integer.parseInt(response.getText());
    }

    private void showUIBasedOnFlags(int regionFlag, int voterFlag) {
        container.getChildren().clear();

        if (regionFlag == 1 && voterFlag == 1) {
            String msg = "Vote is available in region " + region + "\nFor casting vote click Proceed button";
            Label msgLabel = new Label(msg);
            msgLabel.setStyle(
                    "-fx-font-size: 19px; -fx-text-fill: black;"
            );

            Button proceedButton = new Button("Proceed");
            proceedButton.setStyle(
                    "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: green;"
            );
            proceedButton.setOnAction(this::handleProceed);

            container.getChildren().addAll(msgLabel, proceedButton);
        } else if (voterFlag == 0) {
            showMessage("Thanks for your valuable vote. \n       Your Vote is casted", true);
        } else if (regionFlag == 0) {
            showMessage("No vote running in this region", true);
        } else {
            showMessage("Voting not available", true);
        }
    }

    private void handleProceed(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("castVote.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

            CastVoteController controller = loader.getController();
            controller.setVoterData(userID, region, voterFlag);
        } catch (IOException e) {
            showMessage("Failed to load voting scene.", false);
        }
    }

    private void showMessage(String message, boolean isError) {
        Label msgLabel = new Label(message);
        msgLabel.setStyle(
                "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + (isError ? "red;" : "black;")
        );
        container.getChildren().add(msgLabel);
    }

    public void returnToMain(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("userInterface.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}*/

package com.example.project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class CheckRegion {

    @FXML private VBox container;
    private String userID;
    private String region;
    private int voterFlag;
    private Scene scene;
    private Parent root;
    private Stage stage;
    private SocketWrapper socketWrapper;

    public CheckRegion() {
        try {
            socketWrapper = new SocketWrapper("127.0.0.1", 44444);
            System.out.println("SocketWrapper initialized for CheckRegion");
        } catch (IOException e) {
            showMessage("Failed to connect to server: " + e.getMessage(), true);
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }

    public void setVoterData(String userID, String region, int flag) {
        this.userID = userID;
        this.region = region;
        this.voterFlag = flag;

        try {
            int regionFlag = readRegionFlag(region);
            showUIBasedOnFlags(regionFlag, voterFlag);
        } catch (IOException | ClassNotFoundException e) {
            showMessage("Error loading region data: " + e.getMessage(), true);
            System.err.println("Error loading region data: " + e.getMessage());
        }
    }

    private int readRegionFlag(String region) throws IOException, ClassNotFoundException {
        Message request = new Message();
        request.setFrom(userID);
        request.setTo("GET_REGION_FLAG");
        request.setText(region);
        socketWrapper.write(request);
        Message response = (Message) socketWrapper.read();
        try {
            return Integer.parseInt(response.getText());
        } catch (NumberFormatException e) {
            System.err.println("Invalid region flag format: " + response.getText());
            return -1;
        }
    }

    private void showUIBasedOnFlags(int regionFlag, int voterFlag) {
        container.getChildren().clear();

        if (regionFlag == 1 && voterFlag == 1) {
            String msg = "Vote is available in region " + region + "\nFor casting vote click Proceed button";
            Label msgLabel = new Label(msg);
            msgLabel.setStyle(
                    "-fx-font-size: 19px; -fx-text-fill: black;"
            );

            Button proceedButton = new Button("Proceed");
            proceedButton.setStyle(
                    "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: green;"
            );
            proceedButton.setOnAction(this::handleProceed);

            container.getChildren().addAll(msgLabel, proceedButton);
        } else if (voterFlag == 0) {
            showMessage("Thanks for your valuable vote.\nYour vote has been casted successfully!", false);
        } else if (regionFlag == 0) {
            showMessage("No vote running in this region", true);
        } else {
            showMessage("Voting not available", true);
        }
    }

    private void handleProceed(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("castVote.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

            CastVoteController controller = loader.getController();
            controller.setVoterData(userID, region, voterFlag);
        } catch (IOException e) {
            showMessage("Failed to load voting scene: " + e.getMessage(), true);
            System.err.println("Error loading voting scene: " + e.getMessage());
        }
    }

    private void showMessage(String message, boolean isError) {
        Label msgLabel = new Label(message);
        msgLabel.setStyle(
                "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + (isError ? "red;" : "green;")
        );
        container.getChildren().add(msgLabel);
    }

    public void returnToMain(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("userInterface.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}