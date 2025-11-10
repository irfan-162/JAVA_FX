package com.example.project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class VotingController implements Initializable {

    @FXML private ListView<String> selectedC;
    @FXML private ListView<String> availableC;
    @FXML private Button selectButton;
    @FXML private Button removeButton;
    @FXML private TextField hours;
    @FXML private TextField minutes;
    @FXML private TextField seconds;
    @FXML private Button startVote;
    @FXML private Label labelRegion;

    private Timeline countdownTimeline;
    private long endTimeMillis;
    private boolean isVotingStarted = false;
    private SocketWrapper socketWrapper;
    private Scene scene;
    private Parent root;
    private Stage stage;

    public VotingController() {
        try {
            socketWrapper = new SocketWrapper("127.0.0.1", 44444);
        } catch (IOException e) {
            labelRegion.setText("Failed to connect to server: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCandidatesFromFile();
        loadTimerState();
        if (isVotingStarted) {
            startCountdown();
        }
    }

    public void setRegionText(String s) {
        labelRegion.setText(s);
    }

    private void loadCandidatesFromFile() {
        try {
            Message request = new Message();
            request.setFrom("VotingController");
            request.setTo("GET_CANDIDATES");
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            List<String> candidates = List.of(response.getText().split(";"));
            if (availableC != null) availableC.getItems().addAll(candidates);
        } catch (IOException | ClassNotFoundException e) {
            if (labelRegion != null) labelRegion.setText("Error loading candidates!");
        }
    }

    private void loadTimerState() {
        try {
            Message request = new Message();
            request.setFrom("VotingController");
            request.setTo("GET_REGION_DATA");
            request.setText(labelRegion.getText());
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String[] parts = response.getText().split(";");
            if (parts.length >= 3 && parts[0].equals("1")) {
                endTimeMillis = Long.parseLong(parts[1]);
                isVotingStarted = true;
            }
        } catch (IOException | ClassNotFoundException | NumberFormatException e) {
            if (labelRegion != null) labelRegion.setText("Error loading timer state!");
        }
    }

    private void saveRegionCandidates() {
        try {
            String candidates = selectedC.getItems().stream().collect(Collectors.joining(","));
            Message request = new Message();
            request.setFrom("VotingController");
            request.setTo("START_VOTE");
            request.setText(labelRegion.getText() + ";" + (endTimeMillis - Instant.now().toEpochMilli()) / 1000 + ";" + candidates);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            labelRegion.setText(response.getText());
        } catch (IOException | ClassNotFoundException e) {
            if (labelRegion != null) labelRegion.setText("Error saving region candidates!");
        }
    }

    @FXML
    public void startVote(ActionEvent event) throws IOException {
        if (isVotingStarted) {
            labelRegion.setText("Voting already started!");
            return;
        }

        try {
            int hrs = hours.getText().isEmpty() ? 0 : Integer.parseInt(hours.getText());
            int mins = minutes.getText().isEmpty() ? 0 : Integer.parseInt(minutes.getText());
            int secs = seconds.getText().isEmpty() ? 0 : Integer.parseInt(seconds.getText());

            if (hrs < 0 || mins < 0 || mins > 59 || secs < 0 || secs > 59) {
                labelRegion.setText("Invalid time input!");
                return;
            }

            long durationSeconds = hrs * 3600L + mins * 60L + secs;
            if (durationSeconds <= 0) {
                labelRegion.setText("Please enter a valid time!");
                return;
            }

            isVotingStarted = true;
            endTimeMillis = Instant.now().toEpochMilli() + durationSeconds * 1000;
            saveRegionCandidates();
            startCountdown();
            startVote1(event);
        } catch (NumberFormatException e) {
            labelRegion.setText("Please enter valid numbers!");
        }
    }

    private void startCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    long currentTimeMillis = Instant.now().toEpochMilli();
                    long remainingMillis = endTimeMillis - currentTimeMillis;

                    if (remainingMillis <= 0) {
                        countdownTimeline.stop();
                        labelRegion.setText("Voting has ended!");
                        isVotingStarted = false;
                        try {
                            Message request = new Message();
                            request.setFrom("VotingController");
                            request.setTo("TERMINATE_VOTE");
                            request.setText(labelRegion.getText());
                            socketWrapper.write(request);
                            Message response = (Message) socketWrapper.read();
                            labelRegion.setText(response.getText());
                        } catch (IOException | ClassNotFoundException e) {
                            labelRegion.setText("Error clearing timer!");
                        }
                    }
                })
        );
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    public void backAdmin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("adminInterface.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void startVote1(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("successfulVoteStart.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void selectButton() {
        String selected = availableC.getSelectionModel().getSelectedItem();
        if (selected != null && !isVotingStarted) {
            availableC.getItems().remove(selected);
            selectedC.getItems().add(selected);
        }
    }

    @FXML
    public void removeButton() {
        String selected = selectedC.getSelectionModel().getSelectedItem();
        if (selected != null && !isVotingStarted) {
            selectedC.getItems().remove(selected);
            availableC.getItems().add(selected);
        }
    }
}