/*package com.example.project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CastVoteController {

    @FXML private ListView<String> candidateListView;
    @FXML private Button castVoteButton;
    @FXML private Label statusLabel;

    private String userID;
    private String region;
    private int voterFlag;
    private long endTimeMillis;
    private boolean isVoteOngoing;
    private List<Candidate> candidates = new ArrayList<>();
    private Timeline voteCheckTimeline;
    private SocketWrapper socketWrapper;

    private static class Candidate {
        String name;
        int score;

        Candidate(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    public CastVoteController() {
        try {
            socketWrapper = new SocketWrapper("127.0.0.1", 44444);
        } catch (IOException e) {
            statusLabel.setText("Failed to connect to server: " + e.getMessage());
        }
    }

    public void setVoterData(String userID, String region, int voterFlag) {
        this.userID = userID;
        this.region = region;
        this.voterFlag = voterFlag;
        this.isVoteOngoing = false;
        checkVoteStatus();
        if (isVoteOngoing) {
            loadCandidates();
        }
        startVoteCheckTimer();
    }

    private void loadCandidates() {
        candidates.clear();
        candidateListView.getItems().clear();
        try {
            Message request = new Message();
            request.setFrom(userID);
            request.setTo("GET_REGION_CANDIDATES");
            request.setText(region);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String responseText = response.getText();
            if (responseText.startsWith("Error") || responseText.startsWith("No candidates")) {
                statusLabel.setText(responseText);
                castVoteButton.setDisable(true);
                return;
            }
            String[] candidateData = responseText.split("\\|");
            for (String data : candidateData) {
                String[] parts = data.split(";");
                if (parts.length >= 2) {
                    String name = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    candidates.add(new Candidate(name, score));
                    candidateListView.getItems().add(name);
                }
            }
        } catch (IOException | ClassNotFoundException | NumberFormatException e) {
            statusLabel.setText("Failed to load candidates: " + e.getMessage());
            candidateListView.getItems().clear();
            castVoteButton.setDisable(true);
        }
    }

    private void checkVoteStatus() {
        try {
            Message request = new Message();
            request.setFrom(userID);
            request.setTo("CHECK_VOTE_STATUS");
            request.setText(region + ";" + voterFlag);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String[] parts = response.getText().split(";");
            if (parts.length >= 2) {
                statusLabel.setText(parts[0]);
                endTimeMillis = Long.parseLong(parts[1]);
                isVoteOngoing = parts[0].equals("Select a candidate to vote.");
                if (voterFlag == 0 || !isVoteOngoing) {
                    castVoteButton.setDisable(true);
                    candidateListView.getItems().clear();
                }
            } else {
                statusLabel.setText("Error checking region status");
                castVoteButton.setDisable(true);
                candidateListView.getItems().clear();
            }
        } catch (IOException | ClassNotFoundException | NumberFormatException e) {
            statusLabel.setText("Error checking region status: " + e.getMessage());
            castVoteButton.setDisable(true);
            candidateListView.getItems().clear();
        }
    }

    @FXML
    private void handleCastVote(ActionEvent event) {
        if (voterFlag == 0) {
            statusLabel.setText("Vote already casted successfully!");
            castVoteButton.setDisable(true);
            candidateListView.getItems().clear();
            return;
        }

        if (!isVoteOngoing || System.currentTimeMillis() >= endTimeMillis) {
            statusLabel.setText("No vote going on in " + region + "!");
            castVoteButton.setDisable(true);
            candidateListView.getItems().clear();
            resetRegionAndVoters();
            return;
        }

        int selectedIndex = candidateListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            statusLabel.setText("Please select a candidate!");
            return;
        }

        Candidate selectedCandidate = candidates.get(selectedIndex);
        try {
            Message request = new Message();
            request.setFrom(userID);
            request.setTo("CAST_VOTE");
            request.setText(region + ";" + userID + ";" + selectedCandidate.name);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String result = response.getText();

            if (result.equals("Vote casted successfully!")) {
                statusLabel.setStyle("-fx-text-fill: green;");
                statusLabel.setText(result);
                castVoteButton.setDisable(true);
                candidateListView.getItems().clear();
voterFlag = 1;

                // Switch back to CheckRegion scene
                FXMLLoader loader = new FXMLLoader(getClass().getResource("checkRegion.fxml"));
                Scene scene = new Scene(loader.load());
                CheckRegion controller = loader.getController();
                controller.setVoterData(userID, region, 1); // Voter flag now 1 (vote casted)
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else {
                statusLabel.setText(result);
            }
        } catch (IOException | ClassNotFoundException e) {
            statusLabel.setText("Error casting vote: " + e.getMessage());
        }
    }

    private void resetRegionAndVoters() {
        try {
            Message request = new Message();
            request.setFrom(userID);
            request.setTo("RESET_REGION_AND_VOTERS");
            request.setText(region);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            if (!response.getText().startsWith("Error")) {
                isVoteOngoing = false;
            }
        } catch (IOException | ClassNotFoundException e) {
            statusLabel.setText("Error resetting voting data: " + e.getMessage());
        }
    }

    private void startVoteCheckTimer() {
        if (voteCheckTimeline != null) {
            voteCheckTimeline.stop();
        }

        voteCheckTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    if (isVoteOngoing && System.currentTimeMillis() >= endTimeMillis && endTimeMillis != 0) {
                        resetRegionAndVoters();
                        statusLabel.setText("No vote going on in " + region + "!");
                        castVoteButton.setDisable(true);
                        candidateListView.getItems().clear();
                        isVoteOngoing = false;
                        voteCheckTimeline.stop();
                    }
                })
        );
        voteCheckTimeline.setCycleCount(Timeline.INDEFINITE);
        voteCheckTimeline.play();
    }
}*/

package com.example.project;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CastVoteController {

    @FXML private ListView<String> candidateListView;
    @FXML private Button castVoteButton;
    @FXML private Label statusLabel;

    private String userID;
    private String region;
    private int voterFlag;
    private long endTimeMillis;
    private boolean isVoteOngoing;
    private List<Candidate> candidates = new ArrayList<>();
    private Timeline voteCheckTimeline;
    private SocketWrapper socketWrapper;

    private static class Candidate {
        String name;
        int score;

        Candidate(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    public CastVoteController() {
        try {
            socketWrapper = new SocketWrapper("127.0.0.1", 44444);
            System.out.println("SocketWrapper initialized for CastVoteController");
        } catch (IOException e) {
            statusLabel.setText("Failed to connect to server: " + e.getMessage());
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }

    public void setVoterData(String userID, String region, int voterFlag) {
        this.userID = userID;
        this.region = region;
        this.voterFlag = voterFlag;
        this.isVoteOngoing = false;
        checkVoteStatus();
        if (isVoteOngoing) {
            loadCandidates();
        }
        startVoteCheckTimer();
    }

    private void loadCandidates() {
        candidates.clear();
        candidateListView.getItems().clear();
        try {
            Message request = new Message();
            request.setFrom(userID);
            request.setTo("GET_REGION_CANDIDATES");
            request.setText(region);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String responseText = response.getText();
            System.out.println("Loaded candidates for " + region + ": " + responseText);
            if (responseText.startsWith("Error") || responseText.startsWith("No candidates")) {
                statusLabel.setText(responseText);
                castVoteButton.setDisable(true);
                return;
            }
            String[] candidateData = responseText.split("\\|");
            for (String data : candidateData) {
                String[] parts = data.split(";");
                if (parts.length >= 2) {
                    String name = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    candidates.add(new Candidate(name, score));
                    candidateListView.getItems().add(name);
                }
            }
        } catch (IOException | ClassNotFoundException | NumberFormatException e) {
            statusLabel.setText("Failed to load candidates: " + e.getMessage());
            System.err.println("Error loading candidates: " + e.getMessage());
            castVoteButton.setDisable(true);
            candidateListView.getItems().clear();
        }
    }

    private void checkVoteStatus() {
        try {
            Message request = new Message();
            request.setFrom(userID);
            request.setTo("CHECK_VOTE_STATUS");
            request.setText(region + ";" + voterFlag);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String[] parts = response.getText().split(";");
            if (parts.length >= 2) {
                statusLabel.setText(parts[0]);
                endTimeMillis = Long.parseLong(parts[1]);
                isVoteOngoing = parts[0].equals("Select a candidate to vote.");
                if (voterFlag == 0 || !isVoteOngoing) {
                    castVoteButton.setDisable(true);
                    candidateListView.getItems().clear();
                }
            } else {
                statusLabel.setText("Error checking region status");
                System.err.println("Invalid vote status response: " + response.getText());
                castVoteButton.setDisable(true);
                candidateListView.getItems().clear();
            }
        } catch (IOException | ClassNotFoundException | NumberFormatException e) {
            statusLabel.setText("Error checking region status: " + e.getMessage());
            System.err.println("Error checking vote status: " + e.getMessage());
            castVoteButton.setDisable(true);
            candidateListView.getItems().clear();
        }
    }

    @FXML
    private void handleCastVote(ActionEvent event) {
        if (voterFlag == 0) {
            statusLabel.setText("Vote already casted successfully!");
            castVoteButton.setDisable(true);
            candidateListView.getItems().clear();
            return;
        }

        if (!isVoteOngoing || System.currentTimeMillis() >= endTimeMillis) {
            statusLabel.setText("No vote going on in " + region + "!");
            castVoteButton.setDisable(true);
            candidateListView.getItems().clear();
            resetRegionAndVoters();
            return;
        }

        int selectedIndex = candidateListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            statusLabel.setText("Please select a candidate!");
            return;
        }

        Candidate selectedCandidate = candidates.get(selectedIndex);
        try {
            Message request = new Message();
            request.setFrom(userID);
            request.setTo("CAST_VOTE");
            request.setText(region + ";" + userID + ";" + selectedCandidate.name);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String result = response.getText();
            System.out.println("Cast vote response: " + result);

            if (result.equals("Vote casted successfully!")) {
                statusLabel.setStyle("-fx-text-fill: green;");
                statusLabel.setText(result);
                castVoteButton.setDisable(true);
                candidateListView.getItems().clear();
                voterFlag = 0;

                // Switch back to CheckRegion scene
                FXMLLoader loader = new FXMLLoader(getClass().getResource("checkRegion.fxml"));
                Scene scene = new Scene(loader.load());
                CheckRegion controller = loader.getController();
                controller.setVoterData(userID, region, 0);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else {
                statusLabel.setText(result);
                System.err.println("Vote casting failed: " + result);
            }
        } catch (IOException | ClassNotFoundException e) {
            statusLabel.setText("Error casting vote: " + e.getMessage());
            System.err.println("Error casting vote: " + e.getMessage());
        }
    }

    private void resetRegionAndVoters() {
        try {
            Message request = new Message();
            request.setFrom(userID);
            request.setTo("RESET_REGION_AND_VOTERS");
            request.setText(region);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            if (!response.getText().startsWith("Error")) {
                isVoteOngoing = false;
                System.out.println("Region reset: " + region);
            } else {
                System.err.println("Error resetting region: " + response.getText());
            }
        } catch (IOException | ClassNotFoundException e) {
            statusLabel.setText("Error resetting voting data: " + e.getMessage());
            System.err.println("Error resetting voting data: " + e.getMessage());
        }
    }

    private void startVoteCheckTimer() {
        if (voteCheckTimeline != null) {
            voteCheckTimeline.stop();
        }

        voteCheckTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    if (isVoteOngoing && System.currentTimeMillis() >= endTimeMillis && endTimeMillis != 0) {
                        resetRegionAndVoters();
                        statusLabel.setText("No vote going on in " + region + "!");
                        castVoteButton.setDisable(true);
                        candidateListView.getItems().clear();
                        isVoteOngoing = false;
                        voteCheckTimeline.stop();
                    }
                })
        );
        voteCheckTimeline.setCycleCount(Timeline.INDEFINITE);
        voteCheckTimeline.play();
    }
}