/*package com.example.project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Result implements Initializable {

    @FXML private ListView<String> ResultListView;
    @FXML private Label resLabel;
    @FXML private Label winner;
    @FXML private TextArea resultContentArea;

    private Map<String, RegionData> regionDataMap;
    private SocketWrapper socketWrapper;
    private Scene scene;
    private Parent root;
    private Stage stage;

    private static class RegionData {
        int flag;
        long startTime;
        String startTimeStr;
        String endTimeStr;

        RegionData(int flag, long startTime, String startTimeStr, String endTimeStr) {
            this.flag = flag;
            this.startTime = startTime;
            this.startTimeStr = startTimeStr;
            this.endTimeStr = endTimeStr;
        }
    }

    public Result() {
        try {
            socketWrapper = new SocketWrapper("127.0.0.1", 44444);
        } catch (IOException e) {
            resLabel.setText("Failed to connect to server: " + e.getMessage());
        }
    }

    public void returnToMain(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("userInterface.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        regionDataMap = new HashMap<>();
        loadRegions();
        ResultListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        displayRegionResults(newValue);
                    }
                }
        );
    }

    private void loadRegions() {
        try {
            Message request = new Message();
            request.setFrom("Result");
            request.setTo("GET_REGIONS");
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String[] regions = response.getText().split(";");
            for (String region : regions) {
                Message dataRequest = new Message();
                dataRequest.setFrom("Result");
                dataRequest.setTo("GET_REGION_DATA");
                dataRequest.setText(region);
                socketWrapper.write(dataRequest);
                Message dataResponse = (Message) socketWrapper.read();
                String[] parts = dataResponse.getText().split(";");
                if (parts.length == 4) {
                    int flag = Integer.parseInt(parts[0]);
                    long startTime = Long.parseLong(parts[1]);
                    String startTimeStr = parts[2];
                    String endTimeStr = parts[3];
                    regionDataMap.put(region, new RegionData(flag, startTime, startTimeStr, endTimeStr));
                    ResultListView.getItems().add(region);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            resLabel.setText("Error loading regions: " + e.getMessage());
        }
    }

    private void displayRegionResults(String region) {
        RegionData data = regionDataMap.get(region);
        if (data == null) {
            resLabel.setText("No data available for " + region);
            winner.setText("");
            resultContentArea.setText("");
            return;
        }

        if (data.flag == 1) {
            resLabel.setText("Vote still going...");
            winner.setText("");
            resultContentArea.setText("Voting Period: " + data.startTimeStr + " to " + data.endTimeStr);
            return;
        }

        if (data.startTime == 0 && data.startTimeStr.equals("0 0") && data.endTimeStr.equals("0 0")) {
            resLabel.setText("No Decisive Vote happened");
            winner.setText("");
            resultContentArea.setText("Voting Period: " + data.startTimeStr + " to " + data.endTimeStr);
            return;
        }

        try {
            Message request = new Message();
            request.setFrom("Result");
            request.setTo("GET_REGION_DATA");
            request.setText(region);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String[] parts = response.getText().split(";");
            if (parts.length != 4) {
                resLabel.setText("Invalid region data");
                return;
            }

            String regionFileData = parts[0].equals("1") ? "Vote still going..." : getRegionResults(region);
            if (regionFileData.startsWith("Error")) {
                resLabel.setText(regionFileData);
                winner.setText("");
                resultContentArea.setText("");
                return;
            }

            List<Pair<String, Integer>> candidates = new ArrayList<>();
            String[] lines = regionFileData.split("\n");
            for (String line : lines) {
                String[] candidateParts = line.trim().split(" (?=[0-9])");
                if (candidateParts.length >= 2) {
                    String candidateName = candidateParts[0].trim();
                    int votes = Integer.parseInt(candidateParts[1]);
                    candidates.add(new Pair<>(candidateName, votes));
                }
            }

            candidates.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            StringBuilder results = new StringBuilder();
            for (Pair<String, Integer> candidate : candidates) {
                results.append(candidate.getKey()).append(": ").append(candidate.getValue()).append(" votes\n");
            }

            String winnerText = candidates.isEmpty() ? "No candidates" :
                    candidates.get(0).getValue() > 0 ? candidates.get(0).getKey() : "No decisive winner";

            resLabel.setText(results.toString());
            winner.setText("Winner: " + winnerText);
            resultContentArea.setText("Voting Period: " + data.startTimeStr + " to " + data.endTimeStr);
        } catch (IOException | ClassNotFoundException e) {
            resLabel.setText("Error loading results for " + region + ": " + e.getMessage());
            winner.setText("");
            resultContentArea.setText("");
        }
    }

    private String getRegionResults(String region) {
        try {
            Message request = new Message();
            request.setFrom("Result");
            request.setTo("GET_REGION_DATA");
            request.setText(region);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String[] parts = response.getText().split(";");
            if (parts.length != 4 || parts[0].equals("1")) {
                return "Vote still going...";
            }
            String regionFile = "E:\\Project\\src\\main\\resources\\com\\example\\project\\" + region + ".txt";
            Message fileRequest = new Message();
            fileRequest.setFrom("Result");
            fileRequest.setTo("GET_REGION_FILE");
            fileRequest.setText(regionFile);
            socketWrapper.write(fileRequest);
            Message fileResponse = (Message) socketWrapper.read();
            return fileResponse.getText();
        } catch (IOException | ClassNotFoundException e) {
            return "Error: " + e.getMessage();
        }
    }
}*/

package com.example.project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Result implements Initializable {

    @FXML private ListView<String> ResultListView;
    @FXML private Label resLabel;
    @FXML private Label winner;
    @FXML private TextArea resultContentArea;
    @FXML private Button adminMenu;

    private Map<String, RegionData> regionDataMap;
    private SocketWrapper socketWrapper;
    private Scene scene;
    private Parent root;
    private Stage stage;

    private static class RegionData {
        int flag;
        long startTime;
        String startTimeStr;
        String endTimeStr;

        RegionData(int flag, long startTime, String startTimeStr, String endTimeStr) {
            this.flag = flag;
            this.startTime = startTime;
            this.startTimeStr = startTimeStr;
            this.endTimeStr = endTimeStr;
        }
    }

    public void setToFalse(){

        adminMenu.setVisible(false);
    }

    public void setToTrue(){

        adminMenu.setVisible(true);
    }

    public Result() {
        try {
            socketWrapper = new SocketWrapper("127.0.0.1", 44444);
        } catch (IOException e) {
            resLabel.setText("Failed to connect to server: " + e.getMessage());
        }
    }

    public void returnToMain(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("userInterface.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void returnToAdmin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("adminInterface.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        regionDataMap = new HashMap<>();
        loadRegions();
        ResultListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        displayRegionResults(newValue);
                    }
                }
        );
    }

    private void loadRegions() {
        try {
            Message request = new Message();
            request.setFrom("Result");
            request.setTo("GET_REGIONS");
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            String[] regions = response.getText().split(";");
            for (String region : regions) {
                Message dataRequest = new Message();
                dataRequest.setFrom("Result");
                dataRequest.setTo("GET_REGION_DATA");
                dataRequest.setText(region);
                socketWrapper.write(dataRequest);
                Message dataResponse = (Message) socketWrapper.read();
                String[] parts = dataResponse.getText().split(";");
                if (parts.length == 5) {
                    int flag = Integer.parseInt(parts[0]);
                    long startTime = Long.parseLong(parts[1]);
                    String startTimeStr = parts[2]+" "+parts[3];
                    String endTimeStr = parts[4];
                    regionDataMap.put(region, new RegionData(flag, startTime, startTimeStr, endTimeStr));
                    ResultListView.getItems().add(region);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            resLabel.setText("Error loading regions: " + e.getMessage());
        }
    }

    private void displayRegionResults(String region) {
        RegionData data = regionDataMap.get(region);
        if (data == null) {
            resLabel.setText("No data available for " + region);
            winner.setText("");
            resultContentArea.setText("");
            return;
        }

        if (data.flag == 1) {
            resLabel.setText("Vote still ongoing in " + region);
            winner.setText("");
            resultContentArea.setText("Voting Period: " + data.startTimeStr + " to " + data.endTimeStr);
            return;
        }

        if (data.startTime == 0 && data.startTimeStr.equals("0") && data.endTimeStr.equals("0")) {
            resLabel.setText("No vote has occurred in " + region);
            winner.setText("");
            resultContentArea.setText("Voting Period: Not yet started");
            return;
        }

        String regionFileData = getRegionResults(region);
        if (regionFileData.startsWith("Error") || regionFileData.startsWith("No candidates found")) {
            resLabel.setText("No vote results available for " + region);
            winner.setText("");
            resultContentArea.setText("Voting Period: " + data.startTimeStr + " to " + data.endTimeStr);
            return;
        }

        List<Pair<String, Integer>> candidates = new ArrayList<>();
        String[] lines = regionFileData.split("\n");
        for (String line : lines) {
            String[] candidateParts = line.trim().split(" (?=[0-9])");
            if (candidateParts.length >= 2) {
                String candidateName = candidateParts[0].trim();
                int votes;
                try {
                    votes = Integer.parseInt(candidateParts[1]);
                } catch (NumberFormatException e) {
                    continue; // Skip invalid vote counts
                }
                candidates.add(new Pair<>(candidateName, votes));
            }
        }

        candidates.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        StringBuilder results = new StringBuilder();
        results.append("Vote Results for ").append(region).append(":\n");
        for (Pair<String, Integer> candidate : candidates) {
            results.append(candidate.getKey()).append(": ").append(candidate.getValue()).append(" votes\n");
        }

        String winnerText = candidates.isEmpty() ? "No candidates" :
                candidates.get(0).getValue() > 0 ? candidates.get(0).getKey() :
                        candidates.size() > 1 && candidates.get(0).getValue().equals(candidates.get(1).getValue()) ?
                                "No decisive winner" : candidates.get(0).getKey();

        resLabel.setText(results.toString());
        winner.setText("Winner: " + winnerText);
        resultContentArea.setText("Voting Period: " + data.startTimeStr + " to " + data.endTimeStr);
    }

    private String getRegionResults(String region) {
        try {
            Message request = new Message();
            request.setFrom("Result");
            request.setTo("GET_REGION_FILE");
            request.setText(region);
            socketWrapper.write(request);
            Message response = (Message) socketWrapper.read();
            return response.getText();
        } catch (IOException | ClassNotFoundException e) {
            return "Error: " + e.getMessage();
        }
    }
}