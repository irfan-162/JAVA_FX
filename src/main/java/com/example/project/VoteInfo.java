package com.example.project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class VoteInfo implements Initializable {

    @FXML private Label timeRemains;
    @FXML private Label region;
    @FXML private ListView<String> candidates;
    @FXML private Label noVotes;

    private Timeline countdownTimeline;
    private long endTimeMillis;
    private boolean isVotingStarted = false;
    private SocketWrapper socketWrapper;

    public VoteInfo() {
        try {
            socketWrapper = new SocketWrapper("127.0.0.1", 44444);
        } catch (IOException e) {
            noVotes.setText("Failed to connect to server: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        loadTimerState();
        if (isVotingStarted) {
            loadVoteInfo();
            startCountdown();
            noVotes.setDisable(true);
        } else {
            noVotes.setText("No votes ongoing");
            candidates.setDisable(true);
            region.setDisable(true);
            timeRemains.setDisable(true);
        }
    }

    private void loadTimerState() {
        try {
            Message request = new Message();
            request.setFrom("VoteInfo");
            request.setTo("GET_VOTE_INFO");
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String[] lines = response.getText().split(";");
            if (lines.length > 0 && !lines[0].startsWith("Error")) {
                endTimeMillis = Long.parseLong(lines[lines.length - 1]);
                isVotingStarted = true;
            }
        } catch (IOException | ClassNotFoundException | NumberFormatException e) {
            noVotes.setText("Error loading timer state!");
        }
    }

    private void loadVoteInfo() {
        try {
            Message request = new Message();
            request.setFrom("VoteInfo");
            request.setTo("GET_VOTE_INFO");
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String[] lines = response.getText().split(";");
            if (lines.length >= 2) {
                endTimeMillis = Long.parseLong(lines[lines.length - 1]);
                region.setText("Region: " + lines[lines.length - 2]);
                List<String> candidateList = List.of(Arrays.copyOfRange(lines, 0, lines.length - 2));
                candidates.getItems().addAll(candidateList);
            }
        } catch (IOException | ClassNotFoundException | NumberFormatException e) {
            noVotes.setText("Error loading vote info!");
        }
    }

    private void startCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    long currentTimeMillis = System.currentTimeMillis();
                    long remainingMillis = endTimeMillis - currentTimeMillis;

                    if (remainingMillis <= 0) {
                        countdownTimeline.stop();
                        timeRemains.setText("Voting has ended!");
                        isVotingStarted = false;
                        noVotes.setText("No votes ongoing");
                        noVotes.setDisable(false);
                        candidates.setDisable(true);
                        region.setDisable(true);
                        timeRemains.setDisable(true);
                        return;
                    }
                    updateTimerLabel(remainingMillis);
                })
        );
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void updateTimerLabel(long remainingMillis) {
        long remainingSeconds = remainingMillis / 1000;
        int hrs = (int) (remainingSeconds / 3600);
        int mins = (int) ((remainingSeconds % 3600) / 60);
        int secs = (int) (remainingSeconds % 60);
        timeRemains.setText(String.format("Time remaining: %02d:%02d:%02d", hrs, mins, secs));
    }
}