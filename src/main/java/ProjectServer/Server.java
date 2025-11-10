


/*package ProjectServer;

import com.example.project.Message;
import com.example.project.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Server {

    private static final String BASE_PATH = "D:\\Project\\Project\\src\\main\\resources\\com\\example\\project\\";
    private static final String VOTERS_FILE = BASE_PATH + "Voters.txt";
    private static final String ADMINS_FILE = BASE_PATH + "Admins.txt";
    private static final String REGIONS_FILE = BASE_PATH + "regions.txt";
    private static final String CANDIDATES_FILE = BASE_PATH + "candidates.txt";
    private static final String VOTER_REQUESTS_FILE = BASE_PATH + "VoterRequests.txt";
    private static final String ADMIN_REQUESTS_FILE = BASE_PATH + "AdminRequests";
    private static final String VOTE_INFO_FILE = BASE_PATH + "VoteInfo.txt";
    private static final String TIMER_FILE = BASE_PATH + "timer.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private ServerSocket serverSocket;
    private HashMap<String, SocketWrapper> clientMap;

    public Server() {
        clientMap = new HashMap<>();
        try {
            serverSocket = new ServerSocket(44444);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                serve(clientSocket);
            }
        } catch (Exception e) {
            System.out.println("Server starts: " + e);
        }
    }

    private void serve(Socket clientSocket) throws IOException, ClassNotFoundException {
        SocketWrapper socketWrapper = new SocketWrapper(clientSocket);
        new Thread(() -> {
            try {
                while (true) {
                    Object o = socketWrapper.read();
                    if (o instanceof Message) {
                        Message request = (Message) o;
                        String operation = request.getTo();
                        String clientId = request.getFrom();
                        String data = request.getText();
                        Message response = new Message();
                        response.setFrom("Server");
                        response.setTo(clientId);

                        switch (operation) {
                            case "REGISTER_VOTER":
                                response.setText(registerVoter(data));
                                break;
                            case "REGISTER_ADMIN":
                                response.setText(registerAdmin(data));
                                break;
                            case "APPROVE_ADMIN":
                                response.setText(approveAdmin(data));
                                break;
                            case "GET_ADMIN_REQUESTS":
                                response.setText(getAdminRequests());
                                break;
                            case "GET_ADMIN_DETAILS":
                                response.setText(getAdminDetails(data));
                                break;
                            case "CHECK_LOGIN":
                                response.setText(checkLogin(data));
                                break;
                            case "GET_REGIONS":
                                response.setText(getRegions());
                                break;
                            case "GET_REGION_FLAG":
                                response.setText(getRegionFlag(data));
                                break;
                            case "GET_REGION_DATA":
                                response.setText(getRegionData(data));
                                break;
                            case "GET_CANDIDATES":
                                response.setText(getCandidates());
                                break;
                            case "START_VOTE":
                                response.setText(startVote(data));
                                break;
                            case "TERMINATE_VOTE":
                                response.setText(terminateVote(data));
                                break;
                            case "GET_VOTE_INFO":
                                response.setText(getVoteInfo());
                                break;
                            case "CHECK_EXPIRED_VOTES":
                                response.setText(checkExpiredVotes());
                                break;
                            case "GET_REGION_CANDIDATES":
                                response.setText(getRegionCandidates(data));
                                break;
                            case "CHECK_VOTE_STATUS":
                                response.setText(checkVoteStatus(data));
                                break;
                            case "CAST_VOTE":
                                response.setText(castVote(data));
                                break;
                            case "RESET_REGION_AND_VOTERS":
                                response.setText(resetRegionAndVoters(data));
                                break;
                            case "GET_VOTER_REQUESTS":
                                response.setText(getVoterRequests());
                                break;
                            case "APPROVE_VOTER":
                                response.setText(approveVoter(data));
                                break;
                            case "GET_VOTERS":
                                response.setText(getVoters());
                                break;
                            case "REMOVE_VOTER":
                                response.setText(removeVoter(data));
                                break;
                            case "CHANGE_VOTER_REGION":
                                response.setText(changeVoterRegion(data));
                                break;
                            default:
                                response.setText("Unknown operation");
                        }
                        socketWrapper.write(response);
                    }
                }
            } catch (Exception e) {
                System.out.println("Client error: " + e);
                try {
                    socketWrapper.closeConnection();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    private String registerVoter(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 3) return "Invalid data format";
            String username = parts[0];
            String password = parts[1];
            String mobile = parts[2];

            // Validate inputs
            if (username.length() != 10) return "Username must be exactly 10 characters.";
            if (!mobile.matches("\\d{11}")) return "Mobile number must be exactly 11 digits.";
            if (password.length() != 8) return "Password must be exactly 8 characters.";

            File file = new File(VOTER_REQUESTS_FILE);
            if (!file.exists()) file.createNewFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] existing = line.trim().split("\\s+");
                    if (existing.length >= 1 && existing[0].equals(username)) {
                        return "Username already exists.";
                    }
                }
            }

            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(username + " " + password + " " + mobile + "\n");
                writer.flush();
            }
            return "Registration request submitted successfully.";
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    private String registerAdmin(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 3) return "Invalid data format";
            String username = parts[0];
            String password = parts[1];
            String mobile = parts[2];

            // Validate inputs
            if (username.length() != 7) return "Username must be exactly 7 characters.";
            if (!mobile.matches("\\d{11}")) return "Mobile number must be exactly 11 digits.";
            if (password.length() != 8) return "Password must be exactly 8 characters.";

            File file = new File(ADMIN_REQUESTS_FILE);
            if (!file.exists()) file.createNewFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] existing = line.trim().split("\\s+");
                    if (existing.length >= 1 && existing[0].equals(username)) {
                        return "Username already exists.";
                    }
                }
            }

            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(username + " " + password + " " + mobile + "\n");
                writer.flush();
            }
            return "Registration request submitted successfully.";
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    private String approveAdmin(String data) {
        try {
            String username = data;
            List<String> requests = Files.readAllLines(Paths.get(ADMIN_REQUESTS_FILE));
            String adminData = null;
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 3 && parts[0].equals(username)) {
                    adminData = parts[0] + " " + parts[1];
                    break;
                }
            }

            if (adminData == null) return "Admin not found in requests";

            File adminsFile = new File(ADMINS_FILE);
            if (!adminsFile.exists()) {
                adminsFile.createNewFile();
            }
            String content = Files.exists(Paths.get(ADMINS_FILE)) ? Files.readString(Paths.get(ADMINS_FILE)) : "";
            String toWrite = adminData + "\n";
            if (!content.isEmpty() && !content.endsWith("\n")) {
                toWrite = "\n" + toWrite;
            }

            try (FileWriter writer = new FileWriter(ADMINS_FILE, true)) {
                writer.write(toWrite);
            }

            List<String> updatedRequests = new ArrayList<>();
            for (String request : requests) {
                String[] requestParts = request.trim().split("\\s+");
                if (requestParts.length >= 1 && !requestParts[0].equals(username)) {
                    updatedRequests.add(request);
                }
            }

            Files.write(Paths.get(ADMIN_REQUESTS_FILE), updatedRequests);
            return "Admin approved successfully";
        } catch (IOException e) {
            return "Error approving admin: " + e.getMessage();
        }
    }

    private String getAdminRequests() {
        try {
            List<String> requests = Files.readAllLines(Paths.get(ADMIN_REQUESTS_FILE));
            List<String> usernames = new ArrayList<>();
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 1) {
                    usernames.add(parts[0]);
                }
            }
            return String.join(";", usernames);
        } catch (IOException e) {
            return "Error loading admin requests: " + e.getMessage();
        }
    }

    private String getAdminDetails(String username) {
        try {
            List<String> requests = Files.readAllLines(Paths.get(ADMIN_REQUESTS_FILE));
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 3 && parts[0].equals(username)) {
                    return "Username: " + parts[0] + "\nMobile: " + parts[2];
                }
            }
            return "Details not found for " + username;
        } catch (IOException e) {
            return "Error loading admin details: " + e.getMessage();
        }
    }

    private String checkLogin(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 2) return "Invalid data format";
            String userID = parts[0];
            String password = parts[1];
            boolean isAdmin = false;
            boolean isVoter = false;
            String voterRegion = null;
            int voterFlag = -1;

            try (BufferedReader reader = new BufferedReader(new FileReader(ADMINS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] adminParts = line.trim().split("\\s+");
                    if (adminParts.length >= 2 && adminParts[0].equals(userID) && adminParts[1].equals(password)) {
                        isAdmin = true;
                        break;
                    }
                }
            }

            if (isAdmin) return "ADMIN_SUCCESS;" + userID;

            try (BufferedReader reader = new BufferedReader(new FileReader(VOTERS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] voterParts = line.trim().split("\\s+");
                    if (voterParts.length >= 4 && voterParts[0].equals(userID) && voterParts[1].equals(password)) {
                        isVoter = true;
                        voterRegion = voterParts[2];
                        voterFlag = Integer.parseInt(voterParts[3]);
                        break;
                    }
                }
            }

            if (isVoter) return "VOTER_SUCCESS;" + userID + ";" + voterRegion + ";" + voterFlag;
            return "Wrong Login Info!!!";
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getRegions() {
        try {
            if (!Files.exists(Paths.get(REGIONS_FILE))) {
                List<String> defaultRegions = List.of("Dhaka", "Chittagong", "Rajshahi", "Khulna", "Barisal", "Sylhet");
                Files.write(Paths.get(REGIONS_FILE),
                        defaultRegions.stream().map(r -> r + " 0 0 0 0").collect(Collectors.toList()));
            }
            List<String> regions = Files.readAllLines(Paths.get(REGIONS_FILE)).stream()
                    .map(line -> line.split(" ")[0])
                    .collect(Collectors.toList());
            return String.join(";", regions);
        } catch (IOException e) {
            return "Error loading regions: " + e.getMessage();
        }
    }

    private String getRegionFlag(String region) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            for (String line : lines) {
                String[] parts = line.split(" ");
                if (parts[0].equals(region)) {
                    return parts[1];
                }
            }
            return "Region not found";
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getRegionData(String region) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            for (String line : lines) {
                String[] parts = line.split(" ", 5);
                if (parts[0].equals(region)) {
                    if (parts.length < 5) return "Invalid region data";
                    return parts[1] + ";" + parts[2] + ";" + parts[3] + ";" + parts[4];
                }
            }
            return "Region not found";
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getCandidates() {
        try {
            List<String> candidates = Files.exists(Paths.get(CANDIDATES_FILE))
                    ? Files.readAllLines(Paths.get(CANDIDATES_FILE))
                    : List.of("Abdul Rahman", "Fatima Begum", "Mohammed Hasan", "Ayesha Khatun", "Rashed Khan", "Nusrat Jahan");
            Files.write(Paths.get(CANDIDATES_FILE), candidates);
            return String.join(";", candidates);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    private String startVote(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length < 3) return "Invalid data format";
            String region = parts[0];
            long durationSeconds = Long.parseLong(parts[1]);
            String candidates = parts[2];
            String regionFile = BASE_PATH + region + ".txt";

            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            List<String> updatedLines = new ArrayList<>();
            long startTimeMillis = Instant.now().toEpochMilli();
            long endTimeMillis = startTimeMillis + durationSeconds * 1000;
            String startTime = formatter.format(Instant.ofEpochMilli(startTimeMillis));
            String endTime = formatter.format(Instant.ofEpochMilli(endTimeMillis));

            boolean regionFound = false;
            for (String line : lines) {
                String[] lineParts = line.split(" ", 5);
                if (lineParts[0].equals(region)) {
                    regionFound = true;
                    updatedLines.add(region + " 1 " + endTimeMillis + " " + startTime + " " + endTime);
                } else {
                    updatedLines.add(line);
                }
            }
            if (!regionFound) return "Region not found";

            Files.write(Paths.get(REGIONS_FILE), updatedLines);

            List<String> candidateLines = Arrays.stream(candidates.split(","))
                    .map(c -> c + " 0")
                    .collect(Collectors.toList());
            Files.write(Paths.get(regionFile), candidateLines);

            return "Voting has started";
        } catch (IOException | NumberFormatException e) {
            return "Error: " + e.getMessage();
        }
    }

    private String terminateVote(String region) {
        try {
            String regionFile = BASE_PATH + region + ".txt";
            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            List<String> updatedLines = lines.stream()
                    .map(line -> {
                        String[] parts = line.split(" ", 5);
                        if (parts[0].equals(region)) {
                            try {
                                Files.deleteIfExists(Paths.get(regionFile));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return region + " 0 0 0 0";
                        }
                        return line;
                    })
                    .collect(Collectors.toList());
            Files.write(Paths.get(REGIONS_FILE), updatedLines);
            return "Voting terminated";
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getVoteInfo() {
        try {
            List<String> lines = Files.exists(Paths.get(VOTE_INFO_FILE))
                    ? Files.readAllLines(Paths.get(VOTE_INFO_FILE))
                    : new ArrayList<>();
            return String.join(";", lines);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    private String checkExpiredVotes() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            List<String> updatedLines = lines.stream()
                    .map(line -> {
                        try {
                            String[] parts = line.split(" ", 7);
                            if (parts.length >= 5 && parts[1].equals("1")) {
                                String region = parts[0];
                                String endTimeString = parts[3] + " " + parts[4];
                                LocalDateTime endTime = LocalDateTime.parse(endTimeString, formatter);
                                LocalDateTime currentTime = LocalDateTime.now();
                                if (currentTime.isAfter(endTime)) {
                                    String regionFile = BASE_PATH + region + ".txt";
                                   // Files.deleteIfExists(Paths.get(regionFile));
                                    return region + " 0 0 " + parts[3] + " " + parts[4]+" "+parts[5]+" "+parts[6];
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("[ERROR] Failed processing line: " + line);
                        }
                        return line;
                    })
                    .collect(Collectors.toList());
            Files.write(Paths.get(REGIONS_FILE), updatedLines);
            return "Checked expired votes";
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getRegionCandidates(String region) {
        try {
            String path = BASE_PATH + region + ".txt";
            if (!Files.exists(Paths.get(path))) {
                return "No candidates found for " + region;
            }
            List<String> lines = Files.readAllLines(Paths.get(path));
            List<String> candidates = new ArrayList<>();
            for (String line : lines) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    StringBuilder name = new StringBuilder();
                    for (int i = 0; i < parts.length - 1; i++) {
                        name.append(parts[i]).append(" ");
                    }
                    int score = Integer.parseInt(parts[parts.length - 1]);
                    candidates.add(name.toString().trim() + ";" + score);
                }
            }
            return String.join("|", candidates);
        } catch (IOException e) {
            return "Error loading candidates: " + e.getMessage();
        }
    }


    private String checkVoteStatus(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 2) return "Invalid data format";
            String region = parts[0];
            int voterFlag = Integer.parseInt(parts[1]);

            List<String> regionLines = Files.readAllLines(Paths.get(REGIONS_FILE));
            boolean regionFound = false;
            String status = "";
            long endTimeMillis = 0;
            for (String line : regionLines) {
                String[] regionParts = line.trim().split(" ", 5);
                if (regionParts.length == 5 && regionParts[0].equals(region)) {
                    regionFound = true;
                    endTimeMillis = Long.parseLong(regionParts[2]);
                    boolean isVoteOngoing = regionParts[1].equals("1") && System.currentTimeMillis() < endTimeMillis;
                    if (voterFlag == 0) {
                        status = "Vote already casted successfully!";
                    } else if (!isVoteOngoing) {
                        status = "No vote going on in " + region + "!";
                    } else {
                        status = "Select a candidate to vote.";
                    }
                    break;
                }
            }
            if (!regionFound) {
                status = "Region " + region + " not found!";
            }
            return status + ";" + endTimeMillis;
        } catch (IOException | NumberFormatException e) {
            return "Error checking region status: " + e.getMessage() + ";0";
        }
    }

    private String castVote(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 3) return "Invalid data format";
            String region = parts[0];
            String userID = parts[1];
            String candidateName = parts[2];
            String path = BASE_PATH + region + ".txt";

            List<String> lines = Files.readAllLines(Paths.get(path));
            List<String> newLines = new ArrayList<>();
            boolean candidateFound = false;
            for (String line : lines) {
                String[] lineParts = line.trim().split("\\s+");
                if (lineParts.length >= 2) {
                    StringBuilder name = new StringBuilder();
                    for (int i = 0; i < lineParts.length - 1; i++) {
                        name.append(lineParts[i]).append(" ");
                    }
                    String cName = name.toString().trim();
                    int score = Integer.parseInt(lineParts[lineParts.length - 1]);
                    if (cName.equals(candidateName)) {
                        score++;
                        candidateFound = true;
                    }
                    newLines.add(cName + " " + score);
                } else {
                    newLines.add(line);
                }
            }
            if (!candidateFound) return "Candidate not found";
            Files.write(Paths.get(path), newLines);

            List<String> voterLines = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> newVoterLines = new ArrayList<>();
            boolean voterFound = false;
            for (String line : voterLines) {
                String[] voterParts = line.trim().split(" ", 4);
                if (voterParts.length == 4 && voterParts[0].equals(userID)) {
                    newVoterLines.add(voterParts[0] + " " + voterParts[1] + " " + voterParts[2] + " 1");
                    voterFound = true;
                } else {
                    newVoterLines.add(line);
                }
            }
            if (!voterFound) return "Voter not found";
            Files.write(Paths.get(VOTERS_FILE), newVoterLines);

            return "Vote casted successfully!";
        } catch (IOException e) {
            return "Error casting vote: " + e.getMessage();
        }
    }

    private String resetRegionAndVoters(String region) {
        try {
            List<String> regionLines = Files.readAllLines(Paths.get(REGIONS_FILE));
            List<String> newRegionLines = new ArrayList<>();
            String defaultTime = formatter.format(Instant.ofEpochMilli(0));
            for (String line : regionLines) {
                String[] parts = line.trim().split(" ", 5);
                if (parts.length == 5 && parts[0].equals(region)) {
                    newRegionLines.add(parts[0] + " 0 0 " + defaultTime + " " + defaultTime);
                } else {
                    newRegionLines.add(line);
                }
            }
            Files.write(Paths.get(REGIONS_FILE), newRegionLines);

            List<String> voterLines = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> newVoterLines = new ArrayList<>();
            for (String line : voterLines) {
                String[] parts = line.trim().split(" ", 4);
                if (parts.length == 4 && parts[2].equals(region)) {
                    newVoterLines.add(parts[0] + " " + parts[1] + " " + parts[2] + " 0");
                } else {
                    newVoterLines.add(line);
                }
            }
            Files.write(Paths.get(VOTERS_FILE), newVoterLines);

            String regionFile = BASE_PATH + region + ".txt";
            Files.deleteIfExists(Paths.get(regionFile));

            return "Region and voter data reset successfully";
        } catch (IOException e) {
            return "Error resetting voting data: " + e.getMessage();
        }
    }

    private String getVoterRequests() {
        try {
            List<String> requests = Files.readAllLines(Paths.get(VOTER_REQUESTS_FILE));
            List<String> usernames = new ArrayList<>();
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 2) {
                    usernames.add(parts[0]);
                }
            }
            return String.join(";", usernames);
        } catch (IOException e) {
            return "Error loading voter requests: " + e.getMessage();
        }
    }

    private String approveVoter(String username) {
        try {
            List<String> requests = Files.readAllLines(Paths.get(VOTER_REQUESTS_FILE));
            String voterData = null;
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 2 && parts[0].equals(username)) {
                    voterData = String.format("%-12s %-10s %-6s %d", parts[0], parts[1], "Dhaka", 0);
                    break;
                }
            }

            if (voterData == null) return "Voter not found in requests";

            File votersFile = new File(VOTERS_FILE);
            if (!votersFile.exists()) {
                votersFile.createNewFile();
            }
            String content = Files.exists(Paths.get(VOTERS_FILE)) ? Files.readString(Paths.get(VOTERS_FILE)) : "";
            String toWrite = voterData + "\n";
            if (!content.isEmpty() && !content.endsWith("\n")) {
                toWrite = "\n" + toWrite;
            }

            try (FileWriter writer = new FileWriter(VOTERS_FILE, true)) {
                writer.write(toWrite);
            }

            List<String> updatedRequests = new ArrayList<>();
            for (String request : requests) {
                String[] requestParts = request.trim().split("\\s+");
                if (requestParts.length >= 1 && !requestParts[0].equals(username)) {
                    updatedRequests.add(request);
                }
            }

            Files.write(Paths.get(VOTER_REQUESTS_FILE), updatedRequests);
            return "Voter approved successfully";
        } catch (IOException e) {
            return "Error approving voter: " + e.getMessage();
        }
    }

    private String getVoters() {
        try {
            List<String> voters = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> voterEntries = new ArrayList<>();
            for (String voter : voters) {
                String[] parts = voter.trim().split("\\s+");
                if (parts.length == 4) {
                    voterEntries.add(String.join(",", parts));
                }
            }
            return String.join(";", voterEntries);
        } catch (IOException e) {
            return "Error loading voters: " + e.getMessage();
        }
    }

    private String removeVoter(String username) {
        try {
            List<String> voters = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> updatedVoters = new ArrayList<>();
            boolean voterFound = false;
            for (String voter : voters) {
                String[] parts = voter.trim().split("\\s+");
                if (parts.length >= 1 && parts[0].equals(username)) {
                    voterFound = true;
                    continue;
                }
                updatedVoters.add(voter);
            }

            if (!voterFound) return "Voter not found";

            Files.write(Paths.get(VOTERS_FILE), updatedVoters);
            return "Voter removed successfully";
        } catch (IOException e) {
            return "Error removing voter: " + e.getMessage();
        }
    }

    private String changeVoterRegion(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 2) return "Invalid data format";
            String username = parts[0];
            String newRegion = parts[1];

            List<String> voters = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> updatedVoters = new ArrayList<>();
            boolean voterFound = false;
            for (String voter : voters) {
                String[] voterParts = voter.trim().split("\\s+", 4);
                if (voterParts.length == 4 && voterParts[0].equals(username)) {
                    updatedVoters.add(String.format("%-12s %-10s %-6s %s",
                            voterParts[0], voterParts[1], newRegion, voterParts[3]));
                    voterFound = true;
                } else {
                    updatedVoters.add(voter);
                }
            }

            if (!voterFound) return "Voter not found";

            Files.write(Paths.get(VOTERS_FILE), updatedVoters);
            return "Region updated successfully";
        } catch (IOException e) {
            return "Error updating voter region: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}*/

/*package ProjectServer;

import com.example.project.Message;
import com.example.project.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Server {

    private static final String BASE_PATH = "D:\\Project\\Project\\src\\main\\resources\\com\\example\\project\\";
    private static final String VOTERS_FILE = BASE_PATH + "Voters.txt";
    private static final String ADMINS_FILE = BASE_PATH + "Admins.txt";
    private static final String REGIONS_FILE = BASE_PATH + "regions.txt";
    private static final String CANDIDATES_FILE = BASE_PATH + "candidates.txt";
    private static final String VOTER_REQUESTS_FILE = BASE_PATH + "VoterRequests.txt";
    private static final String ADMIN_REQUESTS_FILE = BASE_PATH + "AdminRequests";
    private static final String VOTE_INFO_FILE = BASE_PATH + "VoteInfo.txt";
    private static final String TIMER_FILE = BASE_PATH + "timer.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private ServerSocket serverSocket;
    private HashMap<String, SocketWrapper> clientMap;

    public Server() {
        clientMap = new HashMap<>();
        try {
            serverSocket = new ServerSocket(44444);
            System.out.println("Server started on port 44444");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                serve(clientSocket);
            }
        } catch (Exception e) {
            System.err.println("Server startup error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void serve(Socket clientSocket) throws IOException, ClassNotFoundException {
        SocketWrapper socketWrapper = new SocketWrapper(clientSocket);
        new Thread(() -> {
            try {
                while (true) {
                    Object o = socketWrapper.read();
                    if (o instanceof Message) {
                        Message request = (Message) o;
                        String operation = request.getTo();
                        String clientId = request.getFrom();
                        String data = request.getText();
                        System.out.println("Received request: " + operation + " from " + clientId + " with data: " + data);
                        Message response = new Message();
                        response.setFrom("Server");
                        response.setTo(clientId);

                        switch (operation) {
                            case "REGISTER_VOTER":
                                response.setText(registerVoter(data));
                                break;
                            case "REGISTER_ADMIN":
                                response.setText(registerAdmin(data));
                                break;
                            case "APPROVE_ADMIN":
                                response.setText(approveAdmin(data));
                                break;
                            case "GET_ADMIN_REQUESTS":
                                response.setText(getAdminRequests());
                                break;
                            case "GET_ADMIN_DETAILS":
                                response.setText(getAdminDetails(data));
                                break;
                            case "CHECK_LOGIN":
                                response.setText(checkLogin(data));
                                break;
                            case "GET_REGIONS":
                                response.setText(getRegions());
                                break;
                            case "GET_REGION_FLAG":
                                response.setText(getRegionFlag(data));
                                break;
                            case "GET_REGION_DATA":
                                response.setText(getRegionData(data));
                                break;
                            case "GET_CANDIDATES":
                                response.setText(getCandidates());
                                break;
                            case "START_VOTE":
                                response.setText(startVote(data));
                                break;
                            case "TERMINATE_VOTE":
                                response.setText(terminateVote(data));
                                break;
                            case "GET_VOTE_INFO":
                                response.setText(getVoteInfo());
                                break;
                            case "CHECK_EXPIRED_VOTES":
                                response.setText(checkExpiredVotes());
                                break;
                            case "GET_REGION_CANDIDATES":
                                response.setText(getRegionCandidates(data));
                                break;
                            case "CHECK_VOTE_STATUS":
                                response.setText(checkVoteStatus(data));
                                break;
                            case "CAST_VOTE":
                                response.setText(castVote(data));
                                break;
                            case "RESET_REGION_AND_VOTERS":
                                response.setText(resetRegionAndVoters(data));
                                break;
                            case "GET_VOTER_REQUESTS":
                                response.setText(getVoterRequests());
                                break;
                            case "APPROVE_VOTER":
                                response.setText(approveVoter(data));
                                break;
                            case "GET_VOTERS":
                                response.setText(getVoters());
                                break;
                            case "REMOVE_VOTER":
                                response.setText(removeVoter(data));
                                break;
                            case "CHANGE_VOTER_REGION":
                                response.setText(changeVoterRegion(data));
                                break;
                            case "GET_REGION_FILE":
                                response.setText(getRegionFile(data));
                                break;
                            default:
                                response.setText("Unknown operation: " + operation);
                                System.err.println("Unknown operation requested: " + operation);
                        }
                        System.out.println("Sending response to " + clientId + ": " + response.getText());
                        socketWrapper.write(response);
                    }
                }
            } catch (Exception e) {
                System.err.println("Client error: " + e.getMessage());
                try {
                    socketWrapper.closeConnection();
                } catch (IOException ex) {
                    System.err.println("Error closing connection: " + ex.getMessage());
                }
            }
        }).start();
    }

    private String registerVoter(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 3) return "Invalid data format";
            String username = parts[0];
            String password = parts[1];
            String mobile = parts[2];

            if (username.length() != 10) return "Username must be exactly 10 characters.";
            if (!mobile.matches("\\d{11}")) return "Mobile number must be exactly 11 digits.";
            if (password.length() != 8) return "Password must be exactly 8 characters.";

            File file = new File(VOTER_REQUESTS_FILE);
            if (!file.exists()) file.createNewFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] existing = line.trim().split("\\s+");
                    if (existing.length >= 1 && existing[0].equals(username)) {
                        return "Username already exists.";
                    }
                }
            }

            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(username + " " + password + " " + mobile + "\n");
                writer.flush();
            }
            return "Registration request submitted successfully.";
        } catch (IOException e) {
            System.err.println("Error registering voter: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String registerAdmin(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 3) return "Invalid data format";
            String username = parts[0];
            String password = parts[1];
            String mobile = parts[2];

            if (username.length() != 7) return "Username must be exactly 7 characters.";
            if (!mobile.matches("\\d{11}")) return "Mobile number must be exactly 11 digits.";
            if (password.length() != 8) return "Password must be exactly 8 characters.";

            File file = new File(ADMIN_REQUESTS_FILE);
            if (!file.exists()) file.createNewFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] existing = line.trim().split("\\s+");
                    if (existing.length >= 1 && existing[0].equals(username)) {
                        return "Username already exists.";
                    }
                }
            }

            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(username + " " + password + " " + mobile + "\n");
                writer.flush();
            }
            return "Registration request submitted successfully.";
        } catch (IOException e) {
            System.err.println("Error registering admin: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String approveAdmin(String data) {
        try {
            String username = data;
            List<String> requests = Files.readAllLines(Paths.get(ADMIN_REQUESTS_FILE));
            String adminData = null;
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 3 && parts[0].equals(username)) {
                    adminData = parts[0] + " " + parts[1];
                    break;
                }
            }

            if (adminData == null) return "Admin not found in requests";

            File adminsFile = new File(ADMINS_FILE);
            if (!adminsFile.exists()) {
                adminsFile.createNewFile();
            }
            String content = Files.exists(Paths.get(ADMINS_FILE)) ? Files.readString(Paths.get(ADMINS_FILE)) : "";
            String toWrite = adminData + "\n";
            if (!content.isEmpty() && !content.endsWith("\n")) {
                toWrite = "\n" + toWrite;
            }

            try (FileWriter writer = new FileWriter(ADMINS_FILE, true)) {
                writer.write(toWrite);
            }

            List<String> updatedRequests = new ArrayList<>();
            for (String request : requests) {
                String[] requestParts = request.trim().split("\\s+");
                if (requestParts.length >= 1 && !requestParts[0].equals(username)) {
                    updatedRequests.add(request);
                }
            }

            Files.write(Paths.get(ADMIN_REQUESTS_FILE), updatedRequests);
            return "Admin approved successfully";
        } catch (IOException e) {
            System.err.println("Error approving admin: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getAdminRequests() {
        try {
            List<String> requests = Files.readAllLines(Paths.get(ADMIN_REQUESTS_FILE));
            List<String> usernames = new ArrayList<>();
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 1) {
                    usernames.add(parts[0]);
                }
            }
            return String.join(";", usernames);
        } catch (IOException e) {
            System.err.println("Error loading admin requests: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getAdminDetails(String username) {
        try {
            List<String> requests = Files.readAllLines(Paths.get(ADMIN_REQUESTS_FILE));
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 3 && parts[0].equals(username)) {
                    return "Username: " + parts[0] + "\nMobile: " + parts[2];
                }
            }
            return "Details not found for " + username;
        } catch (IOException e) {
            System.err.println("Error loading admin details: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String checkLogin(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 2) return "Invalid data format";
            String userID = parts[0];
            String password = parts[1];
            boolean isAdmin = false;
            boolean isVoter = false;
            String voterRegion = null;
            int voterFlag = -1;

            try (BufferedReader reader = new BufferedReader(new FileReader(ADMINS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] adminParts = line.trim().split("\\s+");
                    if (adminParts.length >= 2 && adminParts[0].equals(userID) && adminParts[1].equals(password)) {
                        isAdmin = true;
                        break;
                    }
                }
            }

            if (isAdmin) return "ADMIN_SUCCESS;" + userID;

            try (BufferedReader reader = new BufferedReader(new FileReader(VOTERS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] voterParts = line.trim().split("\\s+");
                    if (voterParts.length >= 4 && voterParts[0].equals(userID) && voterParts[1].equals(password)) {
                        isVoter = true;
                        voterRegion = voterParts[2];
                        voterFlag = Integer.parseInt(voterParts[3]);
                        break;
                    }
                }
            }

            if (isVoter) return "VOTER_SUCCESS;" + userID + ";" + voterRegion + ";" + voterFlag;
            return "Wrong Login Info!!!";
        } catch (IOException e) {
            System.err.println("Error checking login: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getRegions() {
        try {
            if (!Files.exists(Paths.get(REGIONS_FILE))) {
                List<String> defaultRegions = List.of("Dhaka", "Chittagong", "Rajshahi", "Khulna", "Barisal", "Sylhet");
                Files.write(Paths.get(REGIONS_FILE),
                        defaultRegions.stream().map(r -> r + " 0 0 0 0").collect(Collectors.toList()));
            }
            List<String> regions = Files.readAllLines(Paths.get(REGIONS_FILE)).stream()
                    .map(line -> line.split(" ")[0])
                    .collect(Collectors.toList());
            return String.join(";", regions);
        } catch (IOException e) {
            System.err.println("Error loading regions: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getRegionFlag(String region) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            for (String line : lines) {
                String[] parts = line.split(" ", 5);
                if (parts.length == 5 && parts[0].equals(region)) {
                    return parts[1];
                }
            }
            return "Region not found";
        } catch (IOException e) {
            System.err.println("Error getting region flag: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getRegionData(String region) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            for (String line : lines) {
                String[] parts = line.split(" ", 5);
                if (parts.length == 5 && parts[0].equals(region)) {
                    return parts[1] + ";" + parts[2] + ";" + parts[3] + ";" + parts[4];
                }
            }
            return "Region not found";
        } catch (IOException e) {
            System.err.println("Error getting region data: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getCandidates() {
        try {
            List<String> candidates = Files.exists(Paths.get(CANDIDATES_FILE))
                    ? Files.readAllLines(Paths.get(CANDIDATES_FILE))
                    : List.of("Abdul Rahman", "Fatima Begum", "Mohammed Hasan", "Ayesha Khatun", "Rashed Khan", "Nusrat Jahan");
            Files.write(Paths.get(CANDIDATES_FILE), candidates);
            return String.join(";", candidates);
        } catch (IOException e) {
            System.err.println("Error getting candidates: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String startVote(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length < 3) return "Invalid data format";
            String region = parts[0];
            long durationSeconds = Long.parseLong(parts[1]);
            String candidates = parts[2];
            String regionFile = BASE_PATH + region + ".txt";

            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            List<String> updatedLines = new ArrayList<>();
            long startTimeMillis = Instant.now().toEpochMilli();
            long endTimeMillis = startTimeMillis + durationSeconds * 1000;
            String startTime = formatter.format(Instant.ofEpochMilli(startTimeMillis));
            String endTime = formatter.format(Instant.ofEpochMilli(endTimeMillis));

            boolean regionFound = false;
            for (String line : lines) {
                String[] lineParts = line.split(" ", 5);
                if (lineParts.length == 5 && lineParts[0].equals(region)) {
                    regionFound = true;
                    updatedLines.add(region + " 1 " + endTimeMillis + " " + startTime + " " + endTime);
                } else {
                    updatedLines.add(line);
                }
            }
            if (!regionFound) return "Region not found";

            Files.write(Paths.get(REGIONS_FILE), updatedLines);

            List<String> candidateLines = Arrays.stream(candidates.split(","))
                    .map(c -> c.trim() + " 0")
                    .collect(Collectors.toList());
            Files.write(Paths.get(regionFile), candidateLines);
            System.out.println("Started vote for region: " + region + ", candidates: " + candidates);
            return "Voting has started";
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error starting vote: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String terminateVote(String region) {
        try {
            String regionFile = BASE_PATH + region + ".txt";
            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            List<String> updatedLines = new ArrayList<>();
            String defaultTime = formatter.format(Instant.ofEpochMilli(0));
            boolean regionFound = false;

            for (String line : lines) {
                String[] parts = line.split(" ", 5);
                if (parts.length == 5 && parts[0].equals(region)) {
                    regionFound = true;
                    updatedLines.add(region + " 0 0 " + defaultTime + " " + defaultTime);
                    try {
                        Files.deleteIfExists(Paths.get(regionFile));
                        System.out.println("Deleted region file: " + regionFile);
                    } catch (IOException e) {
                        System.err.println("Error deleting region file: " + e.getMessage());
                    }
                } else {
                    updatedLines.add(line);
                }
            }
            if (!regionFound) return "Region not found";

            Files.write(Paths.get(REGIONS_FILE), updatedLines);
            return "Voting terminated";
        } catch (IOException e) {
            System.err.println("Error terminating vote: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getVoteInfo() {
        try {
            List<String> lines = Files.exists(Paths.get(VOTE_INFO_FILE))
                    ? Files.readAllLines(Paths.get(VOTE_INFO_FILE))
                    : new ArrayList<>();
            return String.join(";", lines);
        } catch (IOException e) {
            System.err.println("Error getting vote info: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String checkExpiredVotes() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            List<String> updatedLines = new ArrayList<>();
            boolean changesMade = false;

            for (String line : lines) {
                try {
                    String[] parts = line.split(" ", 5);
                    if (parts.length != 5) {
                        System.err.println("[ERROR] Invalid region file format: " + line);
                        updatedLines.add(line);
                        continue;
                    }
                    String region = parts[0];
                    String flag = parts[1];
                    if (flag.equals("1")) {
                        String endTimeString = parts[3] + " " + parts[4];
                        try {
                            LocalDateTime endTime = LocalDateTime.parse(endTimeString, formatter);
                            LocalDateTime currentTime = LocalDateTime.now();
                            System.out.println("Checking region: " + region + ", Current: " + currentTime + ", End: " + endTime);
                            if (currentTime.isAfter(endTime)) {
                                String regionFile = BASE_PATH + region + ".txt";
                                Files.deleteIfExists(Paths.get(regionFile));
                                System.out.println("Expired vote, deleted file: " + regionFile);
                                updatedLines.add(region + " 0 0 " + parts[3] + " " + parts[4]);
                                changesMade = true;
                            } else {
                                updatedLines.add(line);
                            }
                        } catch (DateTimeParseException e) {
                            System.err.println("[ERROR] Invalid time format for region " + region + ": " + endTimeString);
                            updatedLines.add(line);
                        }
                    } else {
                        updatedLines.add(line);
                    }
                } catch (Exception e) {
                    System.err.println("[ERROR] Failed processing line: " + line + ", Error: " + e.getMessage());
                    updatedLines.add(line);
                }
            }

            if (changesMade) {
                Files.write(Paths.get(REGIONS_FILE), updatedLines);
                System.out.println("Updated regions.txt after expiration check");
            } else {
                System.out.println("No expired votes found");
            }
            return "Checked expired votes";
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to check expired votes: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getRegionCandidates(String region) {
        try {
            String path = BASE_PATH + region + ".txt";
            if (!Files.exists(Paths.get(path))) {
                System.out.println("No candidates file found for region: " + region);
                return "No candidates found for " + region;
            }
            List<String> lines = Files.readAllLines(Paths.get(path));
            List<String> candidates = new ArrayList<>();
            for (String line : lines) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    StringBuilder name = new StringBuilder();
                    for (int i = 0; i < parts.length - 1; i++) {
                        name.append(parts[i]).append(" ");
                    }
                    int score;
                    try {
                        score = Integer.parseInt(parts[parts.length - 1]);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid vote count in line: " + line);
                        continue;
                    }
                    candidates.add(name.toString().trim() + ";" + score);
                } else {
                    System.err.println("Invalid candidate line format: " + line);
                }
            }
            String result = String.join("|", candidates);
            System.out.println("Candidates for " + region + ": " + result);
            return result.isEmpty() ? "No candidates found for " + region : result;
        } catch (IOException e) {
            System.err.println("Error loading candidates for " + region + ": " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getRegionFile(String region) {
        try {
            String path = BASE_PATH + region + ".txt";
            if (!Files.exists(Paths.get(path))) {
                System.out.println("No region file found: " + path);
                return "No candidates found for " + region;
            }
            List<String> lines = Files.readAllLines(Paths.get(path));
            String result = String.join("\n", lines);
            System.out.println("Region file contents for " + region + ": " + result);
            return result.isEmpty() ? "No candidates found for " + region : result;
        } catch (IOException e) {
            System.err.println("Error reading region file for " + region + ": " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String checkVoteStatus(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 2) return "Invalid data format";
            String region = parts[0];
            int voterFlag = Integer.parseInt(parts[1]);

            List<String> regionLines = Files.readAllLines(Paths.get(REGIONS_FILE));
            boolean regionFound = false;
            String status = "";
            long endTimeMillis = 0;
            for (String line : regionLines) {
                String[] regionParts = line.trim().split(" ", 5);
                if (regionParts.length == 5 && regionParts[0].equals(region)) {
                    regionFound = true;
                    endTimeMillis = Long.parseLong(regionParts[2]);
                    boolean isVoteOngoing = regionParts[1].equals("1") && System.currentTimeMillis() < endTimeMillis;
                    if (voterFlag == 0) {
                        status = "Vote already casted successfully!";
                    } else if (!isVoteOngoing) {
                        status = "No vote going on in " + region + "!";
                    } else {
                        status = "Select a candidate to vote.";
                    }
                    break;
                }
            }
            if (!regionFound) {
                status = "Region " + region + " not found!";
            }
            return status + ";" + endTimeMillis;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error checking vote status: " + e.getMessage());
            return "Error: " + e.getMessage() + ";0";
        }
    }

    private String castVote(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 3) return "Invalid data format";
            String region = parts[0];
            String userID = parts[1];
            String candidateName = parts[2];
            String path = BASE_PATH + region + ".txt";

            List<String> lines = Files.readAllLines(Paths.get(path));
            List<String> newLines = new ArrayList<>();
            boolean candidateFound = false;
            for (String line : lines) {
                String[] lineParts = line.trim().split("\\s+");
                if (lineParts.length >= 2) {
                    StringBuilder name = new StringBuilder();
                    for (int i = 0; i < lineParts.length - 1; i++) {
                        name.append(lineParts[i]).append(" ");
                    }
                    String cName = name.toString().trim();
                    int score = Integer.parseInt(lineParts[lineParts.length - 1]);
                    if (cName.equals(candidateName)) {
                        score++;
                        candidateFound = true;
                    }
                    newLines.add(cName + " " + score);
                } else {
                    newLines.add(line);
                }
            }
            if (!candidateFound) return "Candidate not found";
            Files.write(Paths.get(path), newLines);

            List<String> voterLines = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> newVoterLines = new ArrayList<>();
            boolean voterFound = false;
            for (String line : voterLines) {
                String[] voterParts = line.trim().split(" ", 4);
                if (voterParts.length == 4 && voterParts[0].equals(userID)) {
                    newVoterLines.add(voterParts[0] + " " + voterParts[1] + " " + voterParts[2] + " 0");
                    voterFound = true;
                } else {
                    newVoterLines.add(line);
                }
            }
            if (!voterFound) return "Voter not found";
            Files.write(Paths.get(VOTERS_FILE), newVoterLines);

            System.out.println("Vote cast for " + candidateName + " in region " + region + " by " + userID);
            return "Vote casted successfully!";
        } catch (IOException e) {
            System.err.println("Error casting vote: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String resetRegionAndVoters(String region) {
        try {
            List<String> regionLines = Files.readAllLines(Paths.get(REGIONS_FILE));
            List<String> newRegionLines = new ArrayList<>();
            String defaultTime = formatter.format(Instant.ofEpochMilli(0));
            for (String line : regionLines) {
                String[] parts = line.trim().split(" ", 5);
                if (parts.length == 5 && parts[0].equals(region)) {
                    newRegionLines.add(parts[0] + " 0 0 " + defaultTime + " " + defaultTime);
                } else {
                    newRegionLines.add(line);
                }
            }
            Files.write(Paths.get(REGIONS_FILE), newRegionLines);

            List<String> voterLines = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> newVoterLines = new ArrayList<>();
            for (String line : voterLines) {
                String[] parts = line.trim().split(" ", 4);
                if (parts.length == 4 && parts[2].equals(region)) {
                    newVoterLines.add(parts[0] + " " + parts[1] + " " + parts[2] + " 0");
                } else {
                    newVoterLines.add(line);
                }
            }
            Files.write(Paths.get(VOTERS_FILE), newVoterLines);

            String regionFile = BASE_PATH + region + ".txt";
            Files.deleteIfExists(Paths.get(regionFile));
            System.out.println("Reset region and voters for: " + region);

            return "Region and voter data reset successfully";
        } catch (IOException e) {
            System.err.println("Error resetting voting data: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getVoterRequests() {
        try {
            List<String> requests = Files.readAllLines(Paths.get(VOTER_REQUESTS_FILE));
            List<String> usernames = new ArrayList<>();
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 2) {
                    usernames.add(parts[0]);
                }
            }
            return String.join(";", usernames);
        } catch (IOException e) {
            System.err.println("Error loading voter requests: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String approveVoter(String username) {
        try {
            List<String> requests = Files.readAllLines(Paths.get(VOTER_REQUESTS_FILE));
            String voterData = null;
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 2 && parts[0].equals(username)) {
                    voterData = String.format("%-12s %-10s %-6s %d", parts[0], parts[1], "Dhaka", 0);
                    break;
                }
            }

            if (voterData == null) return "Voter not found in requests";

            File votersFile = new File(VOTERS_FILE);
            if (!votersFile.exists()) {
                votersFile.createNewFile();
            }
            String content = Files.exists(Paths.get(VOTERS_FILE)) ? Files.readString(Paths.get(VOTERS_FILE)) : "";
            String toWrite = voterData + "\n";
            if (!content.isEmpty() && !content.endsWith("\n")) {
                toWrite = "\n" + toWrite;
            }

            try (FileWriter writer = new FileWriter(VOTERS_FILE, true)) {
                writer.write(toWrite);
            }

            List<String> updatedRequests = new ArrayList<>();
            for (String request : requests) {
                String[] requestParts = request.trim().split("\\s+");
                if (requestParts.length >= 1 && !requestParts[0].equals(username)) {
                    updatedRequests.add(request);
                }
            }

            Files.write(Paths.get(VOTER_REQUESTS_FILE), updatedRequests);
            return "Voter approved successfully";
        } catch (IOException e) {
            System.err.println("Error approving voter: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getVoters() {
        try {
            List<String> voters = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> voterEntries = new ArrayList<>();
            for (String voter : voters) {
                String[] parts = voter.trim().split("\\s+");
                if (parts.length == 4) {
                    voterEntries.add(String.join(",", parts));
                }
            }
            return String.join(";", voterEntries);
        } catch (IOException e) {
            System.err.println("Error loading voters: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String removeVoter(String username) {
        try {
            List<String> voters = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> updatedVoters = new ArrayList<>();
            boolean voterFound = false;
            for (String voter : voters) {
                String[] parts = voter.trim().split("\\s+");
                if (parts.length >= 1 && parts[0].equals(username)) {
                    voterFound = true;
                    continue;
                }
                updatedVoters.add(voter);
            }

            if (!voterFound) return "Voter not found";

            Files.write(Paths.get(VOTERS_FILE), updatedVoters);
            return "Voter removed successfully";
        } catch (IOException e) {
            System.err.println("Error removing voter: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String changeVoterRegion(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 2) return "Invalid data format";
            String username = parts[0];
            String newRegion = parts[1];

            List<String> voters = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> updatedVoters = new ArrayList<>();
            boolean voterFound = false;
            for (String voter : voters) {
                String[] voterParts = voter.trim().split("\\s+", 4);
                if (voterParts.length == 4 && voterParts[0].equals(username)) {
                    updatedVoters.add(String.format("%-12s %-10s %-6s %s",
                            voterParts[0], voterParts[1], newRegion, voterParts[3]));
                    voterFound = true;
                } else {
                    updatedVoters.add(voter);
                }
            }

            if (!voterFound) return "Voter not found";

            Files.write(Paths.get(VOTERS_FILE), updatedVoters);
            return "Region updated successfully";
        } catch (IOException e) {
            System.err.println("Error updating voter region: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}*/

/*package ProjectServer;

import com.example.project.Message;
import com.example.project.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Server {

    private static final String BASE_PATH = "D:\\Project\\Project\\src\\main\\resources\\com\\example\\project\\";
    private static final String VOTERS_FILE = BASE_PATH + "Voters.txt";
    private static final String ADMINS_FILE = BASE_PATH + "Admins.txt";
    private static final String REGIONS_FILE = BASE_PATH + "regions.txt";
    private static final String CANDIDATES_FILE = BASE_PATH + "candidates.txt";
    private static final String VOTER_REQUESTS_FILE = BASE_PATH + "VoterRequests.txt";
    private static final String ADMIN_REQUESTS_FILE = BASE_PATH + "AdminRequests";
    private static final String VOTE_INFO_FILE = BASE_PATH + "VoteInfo.txt";
    private static final String TIMER_FILE = BASE_PATH + "timer.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private ServerSocket serverSocket;
    private HashMap<String, SocketWrapper> clientMap;

    public Server() {
        clientMap = new HashMap<>();
        try {
            serverSocket = new ServerSocket(44444);
            System.out.println("Server started on port 44444");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                serve(clientSocket);
            }
        } catch (Exception e) {
            System.err.println("Server startup error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void serve(Socket clientSocket) throws IOException, ClassNotFoundException {
        SocketWrapper socketWrapper = new SocketWrapper(clientSocket);
        new Thread(() -> {
            try {
                while (true) {
                    Object o = socketWrapper.read();
                    if (o instanceof Message) {
                        Message request = (Message) o;
                        String operation = request.getTo();
                        String clientId = request.getFrom();
                        String data = request.getText();
                        System.out.println("Received request: " + operation + " from " + clientId + " with data: " + data);
                        Message response = new Message();
                        response.setFrom("Server");
                        response.setTo(clientId);

                        switch (operation) {
                            case "REGISTER_VOTER":
                                response.setText(registerVoter(data));
                                break;
                            case "REGISTER_ADMIN":
                                response.setText(registerAdmin(data));
                                break;
                            case "APPROVE_ADMIN":
                                response.setText(approveAdmin(data));
                                break;
                            case "GET_ADMIN_REQUESTS":
                                response.setText(getAdminRequests());
                                break;
                            case "GET_ADMIN_DETAILS":
                                response.setText(getAdminDetails(data));
                                break;
                            case "CHECK_LOGIN":
                                response.setText(checkLogin(data));
                                break;
                            case "GET_REGIONS":
                                response.setText(getRegions());
                                break;
                            case "GET_REGION_FLAG":
                                response.setText(getRegionFlag(data));
                                break;
                            case "GET_REGION_DATA":
                                response.setText(getRegionData(data));
                                break;
                            case "GET_CANDIDATES":
                                response.setText(getCandidates());
                                break;
                            case "START_VOTE":
                                response.setText(startVote(data));
                                break;
                            case "TERMINATE_VOTE":
                                response.setText(terminateVote(data));
                                break;
                            case "GET_VOTE_INFO":
                                response.setText(getVoteInfo());
                                break;
                            case "CHECK_EXPIRED_VOTES":
                                response.setText(checkExpiredVotes());
                                break;
                            case "GET_REGION_CANDIDATES":
                                response.setText(getRegionCandidates(data));
                                break;
                            case "CHECK_VOTE_STATUS":
                                response.setText(checkVoteStatus(data));
                                break;
                            case "CAST_VOTE":
                                response.setText(castVote(data));
                                break;
                            case "RESET_REGION_AND_VOTERS":
                                response.setText(resetRegionAndVoters(data));
                                break;
                            case "GET_VOTER_REQUESTS":
                                response.setText(getVoterRequests());
                                break;
                            case "APPROVE_VOTER":
                                response.setText(approveVoter(data));
                                break;
                            case "GET_VOTERS":
                                response.setText(getVoters());
                                break;
                            case "REMOVE_VOTER":
                                response.setText(removeVoter(data));
                                break;
                            case "CHANGE_VOTER_REGION":
                                response.setText(changeVoterRegion(data));
                                break;
                            case "GET_REGION_FILE":
                                response.setText(getRegionFile(data));
                                break;
                            default:
                                response.setText("Unknown operation: " + operation);
                                System.err.println("Unknown operation requested: " + operation);
                        }
                        System.out.println("Sending response to " + clientId + ": " + response.getText());
                        socketWrapper.write(response);
                    }
                }
            } catch (Exception e) {
                System.err.println("Client error: " + e.getMessage());
                try {
                    socketWrapper.closeConnection();
                } catch (IOException ex) {
                    System.err.println("Error closing connection: " + ex.getMessage());
                }
            }
        }).start();
    }

    private String registerVoter(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 3) return "Invalid data format";
            String username = parts[0];
            String password = parts[1];
            String mobile = parts[2];

            if (username.length() != 10) return "Username must be exactly 10 characters.";
            if (!mobile.matches("\\d{11}")) return "Mobile number must be exactly 11 digits.";
            if (password.length() != 8) return "Password must be exactly 8 characters.";

            File file = new File(VOTER_REQUESTS_FILE);
            if (!file.exists()) file.createNewFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] existing = line.trim().split("\\s+");
                    if (existing.length >= 1 && existing[0].equals(username)) {
                        return "Username already exists.";
                    }
                }
            }

            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(username + " " + password + " " + mobile + "\n");
                writer.flush();
            }
            return "Registration request submitted successfully.";
        } catch (IOException e) {
            System.err.println("Error registering voter: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String registerAdmin(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 3) return "Invalid data format";
            String username = parts[0];
            String password = parts[1];
            String mobile = parts[2];

            if (username.length() != 7) return "Username must be exactly 7 characters.";
            if (!mobile.matches("\\d{11}")) return "Mobile number must be exactly 11 digits.";
            if (password.length() != 8) return "Password must be exactly 8 characters.";

            File file = new File(ADMIN_REQUESTS_FILE);
            if (!file.exists()) file.createNewFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] existing = line.trim().split("\\s+");
                    if (existing.length >= 1 && existing[0].equals(username)) {
                        return "Username already exists.";
                    }
                }
            }

            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(username + " " + password + " " + mobile + "\n");
                writer.flush();
            }
            return "Registration request submitted successfully.";
        } catch (IOException e) {
            System.err.println("Error registering admin: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String approveAdmin(String data) {
        try {
            String username = data;
            List<String> requests = Files.readAllLines(Paths.get(ADMIN_REQUESTS_FILE));
            String adminData = null;
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 3 && parts[0].equals(username)) {
                    adminData = parts[0] + " " + parts[1];
                    break;
                }
            }

            if (adminData == null) return "Admin not found in requests";

            File adminsFile = new File(ADMINS_FILE);
            if (!adminsFile.exists()) {
                adminsFile.createNewFile();
            }
            String content = Files.exists(Paths.get(ADMINS_FILE)) ? Files.readString(Paths.get(ADMINS_FILE)) : "";
            String toWrite = adminData + "\n";
            if (!content.isEmpty() && !content.endsWith("\n")) {
                toWrite = "\n" + toWrite;
            }

            try (FileWriter writer = new FileWriter(ADMINS_FILE, true)) {
                writer.write(toWrite);
            }

            List<String> updatedRequests = new ArrayList<>();
            for (String request : requests) {
                String[] requestParts = request.trim().split("\\s+");
                if (requestParts.length >= 1 && !requestParts[0].equals(username)) {
                    updatedRequests.add(request);
                }
            }

            Files.write(Paths.get(ADMIN_REQUESTS_FILE), updatedRequests);
            return "Admin approved successfully";
        } catch (IOException e) {
            System.err.println("Error approving admin: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getAdminRequests() {
        try {
            List<String> requests = Files.readAllLines(Paths.get(ADMIN_REQUESTS_FILE));
            List<String> usernames = new ArrayList<>();
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 1) {
                    usernames.add(parts[0]);
                }
            }
            return String.join(";", usernames);
        } catch (IOException e) {
            System.err.println("Error loading admin requests: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getAdminDetails(String username) {
        try {
            List<String> requests = Files.readAllLines(Paths.get(ADMIN_REQUESTS_FILE));
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 3 && parts[0].equals(username)) {
                    return "Username: " + parts[0] + "\nMobile: " + parts[2];
                }
            }
            return "Details not found for " + username;
        } catch (IOException e) {
            System.err.println("Error loading admin details: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String checkLogin(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 2) return "Invalid data format";
            String userID = parts[0];
            String password = parts[1];
            boolean isAdmin = false;
            boolean isVoter = false;
            String voterRegion = null;
            int voterFlag = -1;

            try (BufferedReader reader = new BufferedReader(new FileReader(ADMINS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] adminParts = line.trim().split("\\s+");
                    if (adminParts.length >= 2 && adminParts[0].equals(userID) && adminParts[1].equals(password)) {
                        isAdmin = true;
                        break;
                    }
                }
            }

            if (isAdmin) return "ADMIN_SUCCESS;" + userID;

            try (BufferedReader reader = new BufferedReader(new FileReader(VOTERS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] voterParts = line.trim().split("\\s+");
                    if (voterParts.length >= 4 && voterParts[0].equals(userID) && voterParts[1].equals(password)) {
                        isVoter = true;
                        voterRegion = voterParts[2];
                        voterFlag = Integer.parseInt(voterParts[3]);
                        break;
                    }
                }
            }

            if (isVoter) return "VOTER_SUCCESS;" + userID + ";" + voterRegion + ";" + voterFlag;
            return "Wrong Login Info!!!";
        } catch (IOException e) {
            System.err.println("Error checking login: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getRegions() {
        try {
            if (!Files.exists(Paths.get(REGIONS_FILE))) {
                List<String> defaultRegions = List.of("Dhaka", "Chittagong", "Rajshahi", "Khulna", "Barisal", "Sylhet");
                Files.write(Paths.get(REGIONS_FILE),
                        defaultRegions.stream().map(r -> r + " 0 0 0 0").collect(Collectors.toList()));
            }
            List<String> regions = Files.readAllLines(Paths.get(REGIONS_FILE)).stream()
                    .map(line -> line.split(" ")[0])
                    .collect(Collectors.toList());
            return String.join(";", regions);
        } catch (IOException e) {
            System.err.println("Error loading regions: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getRegionFlag(String region) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            for (String line : lines) {
                String[] parts = line.split(" ", 5);
                if (parts.length == 5 && parts[0].equals(region)) {
                    return parts[1];
                }
            }
            return "Region not found";
        } catch (IOException e) {
            System.err.println("Error getting region flag: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getRegionData(String region) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            for (String line : lines) {
                String[] parts = line.split(" ", 5);
                if (parts.length == 5 && parts[0].equals(region)) {
                    return parts[1] + ";" + parts[2] + ";" + parts[3] + ";" + parts[4];
                }
            }
            return "Region not found";
        } catch (IOException e) {
            System.err.println("Error getting region data: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getCandidates() {
        try {
            List<String> candidates = Files.exists(Paths.get(CANDIDATES_FILE))
                    ? Files.readAllLines(Paths.get(CANDIDATES_FILE))
                    : List.of("Abdul Rahman", "Fatima Begum", "Mohammed Hasan", "Ayesha Khatun", "Rashed Khan", "Nusrat Jahan");
            Files.write(Paths.get(CANDIDATES_FILE), candidates);
            return String.join(";", candidates);
        } catch (IOException e) {
            System.err.println("Error getting candidates: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String startVote(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length < 3) return "Invalid data format";
            String region = parts[0];
            long durationSeconds = Long.parseLong(parts[1]);
            String candidates = parts[2];
            String regionFile = BASE_PATH + region + ".txt";

            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            List<String> updatedLines = new ArrayList<>();
            long startTimeMillis = Instant.now().toEpochMilli();
            long endTimeMillis = startTimeMillis + durationSeconds * 1000;
            String startTime = formatter.format(Instant.ofEpochMilli(startTimeMillis));
            String endTime = formatter.format(Instant.ofEpochMilli(endTimeMillis));

            boolean regionFound = false;
            for (String line : lines) {
                String[] lineParts = line.split(" ", 5);
                if (lineParts.length == 5 && lineParts[0].equals(region)) {
                    regionFound = true;
                    updatedLines.add(region + " 1 " + endTimeMillis + " " + startTime + " " + endTime);
                } else {
                    updatedLines.add(line);
                }
            }
            if (!regionFound) return "Region not found";

            Files.write(Paths.get(REGIONS_FILE), updatedLines);

            List<String> candidateLines = Arrays.stream(candidates.split(","))
                    .map(c -> c.trim() + " 0")
                    .collect(Collectors.toList());
            Files.write(Paths.get(regionFile), candidateLines);

            // Update voter flags to 1 for all voters in the region
            List<String> voterLines = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> newVoterLines = new ArrayList<>();
            for (String line : voterLines) {
                String[] voterParts = line.trim().split(" ", 4);
                if (voterParts.length == 4 && voterParts[2].equals(region)) {
                    newVoterLines.add(voterParts[0] + " " + voterParts[1] + " " + voterParts[2] + " 1");
                } else {
                    newVoterLines.add(line);
                }
            }
            Files.write(Paths.get(VOTERS_FILE), newVoterLines);

            System.out.println("Started vote for region: " + region + ", candidates: " + candidates + ", voter flags set to 1");
            return "Voting has started";
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error starting vote: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String terminateVote(String region) {
        try {
            String regionFile = BASE_PATH + region + ".txt";
            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            List<String> updatedLines = new ArrayList<>();
            String defaultTime = formatter.format(Instant.ofEpochMilli(0));
            boolean regionFound = false;

            for (String line : lines) {
                String[] parts = line.split(" ", 5);
                if (parts.length == 5 && parts[0].equals(region)) {
                    regionFound = true;
                    updatedLines.add(region + " 0 0 " + defaultTime + " " + defaultTime);
                    //Files.deleteIfExists(Paths.get(regionFile));
                    System.out.println("Deleted region file: " + regionFile);
                } else {
                    updatedLines.add(line);
                }
            }
            if (!regionFound) return "Region not found";

            Files.write(Paths.get(REGIONS_FILE), updatedLines);

            // Update voter flags to 0 for all voters in the region
            List<String> voterLines = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> newVoterLines = new ArrayList<>();
            for (String line : voterLines) {
                String[] voterParts = line.trim().split(" ", 4);
                if (voterParts.length == 4 && voterParts[2].equals(region)) {
                    newVoterLines.add(voterParts[0] + " " + voterParts[1] + " " + voterParts[2] + " 0");
                } else {
                    newVoterLines.add(line);
                }
            }
            Files.write(Paths.get(VOTERS_FILE), newVoterLines);

            System.out.println("Terminated vote for region: " + region + ", voter flags set to 0");
            return "Voting terminated";
        } catch (IOException e) {
            System.err.println("Error terminating vote: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getVoteInfo() {
        try {
            List<String> lines = Files.exists(Paths.get(VOTE_INFO_FILE))
                    ? Files.readAllLines(Paths.get(VOTE_INFO_FILE))
                    : new ArrayList<>();
            return String.join(";", lines);
        } catch (IOException e) {
            System.err.println("Error getting vote info: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String checkExpiredVotes() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            List<String> updatedLines = new ArrayList<>();
            boolean changesMade = false;

            for (String line : lines) {
                try {
                    String[] parts = line.split(" ", 7);
                    if (parts.length != 7) {
                        System.err.println("[ERROR] Invalid region file format: " + line);
                        updatedLines.add(line);
                        continue;
                    }
                    String region = parts[0];
                    String flag = parts[1];
                    if (flag.equals("1")) {
                        String endTimeString = parts[5] + " " + parts[6];
                        try {
                            LocalDateTime endTime = LocalDateTime.parse(endTimeString, formatter);
                            LocalDateTime currentTime = LocalDateTime.now();
                            System.out.println("Checking region: " + region + ", Current: " + currentTime + ", End: " + endTime);
                            if (currentTime.isAfter(endTime)) {
                                String regionFile = BASE_PATH + region + ".txt";
                               // Files.deleteIfExists(Paths.get(regionFile));
                                System.out.println("Expired vote, deleted file: " + regionFile);
                                updatedLines.add(region + " 0 0 " + parts[3] + " " + parts[4]+parts[5] + " " + parts[6]);
                                // Update voter flags to 0
                                List<String> voterLines = Files.readAllLines(Paths.get(VOTERS_FILE));
                                List<String> newVoterLines = new ArrayList<>();
                                for (String voterLine : voterLines) {
                                    String[] voterParts = voterLine.trim().split(" ", 4);
                                    if (voterParts.length == 4 && voterParts[2].equals(region)) {
                                        newVoterLines.add(voterParts[0] + " " + voterParts[1] + " " + voterParts[2] + " 0");
                                    } else {
                                        newVoterLines.add(voterLine);
                                    }
                                }
                                Files.write(Paths.get(VOTERS_FILE), newVoterLines);
                                System.out.println("Set voter flags to 0 for region: " + region);
                                changesMade = true;
                            } else {
                                updatedLines.add(line);
                            }
                        } catch (DateTimeParseException e) {
                            System.err.println("[ERROR] Invalid time format for region " + region + ": " + endTimeString);
                            updatedLines.add(line);
                        }
                    } else {
                        updatedLines.add(line);
                    }
                } catch (Exception e) {
                    System.err.println("[ERROR] Failed processing line: " + line + ", Error: " + e.getMessage());
                    updatedLines.add(line);
                }
            }

            if (changesMade) {
                Files.write(Paths.get(REGIONS_FILE), updatedLines);
                System.out.println("Updated regions.txt after expiration check");
            } else {
                System.out.println("No expired votes found");
            }
            return "Checked expired votes";
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to check expired votes: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getRegionCandidates(String region) {
        try {
            String path = BASE_PATH + region + ".txt";
            if (!Files.exists(Paths.get(path))) {
                System.out.println("No candidates file found for region: " + region);
                return "No candidates found for " + region;
            }
            List<String> lines = Files.readAllLines(Paths.get(path));
            List<String> candidates = new ArrayList<>();
            for (String line : lines) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    StringBuilder name = new StringBuilder();
                    for (int i = 0; i < parts.length - 1; i++) {
                        name.append(parts[i]).append(" ");
                    }
                    int score;
                    try {
                        score = Integer.parseInt(parts[parts.length - 1]);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid vote count in line: " + line);
                        continue;
                    }
                    candidates.add(name.toString().trim() + ";" + score);
                } else {
                    System.err.println("Invalid candidate line format: " + line);
                }
            }
            String result = String.join("|", candidates);
            System.out.println("Candidates for " + region + ": " + result);
            return result.isEmpty() ? "No candidates found for " + region : result;
        } catch (IOException e) {
            System.err.println("Error loading candidates for " + region + ": " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getRegionFile(String region) {
        try {
            String path = BASE_PATH + region + ".txt";
            if (!Files.exists(Paths.get(path))) {
                System.out.println("No region file found: " + path);
                return "No candidates found for " + region;
            }
            List<String> lines = Files.readAllLines(Paths.get(path));
            String result = String.join("\n", lines);
            System.out.println("Region file contents for " + region + ": " + result);
            return result.isEmpty() ? "No candidates found for " + region : result;
        } catch (IOException e) {
            System.err.println("Error reading region file for " + region + ": " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String checkVoteStatus(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 2) return "Invalid data format";
            String region = parts[0];
            int voterFlag = Integer.parseInt(parts[1]);

            List<String> regionLines = Files.readAllLines(Paths.get(REGIONS_FILE));
            boolean regionFound = false;
            String status = "";
            long endTimeMillis = 0;
            for (String line : regionLines) {
                String[] regionParts = line.trim().split(" ", 5);
                if (regionParts.length == 5 && regionParts[0].equals(region)) {
                    regionFound = true;
                    endTimeMillis = Long.parseLong(regionParts[2]);
                    boolean isVoteOngoing = regionParts[1].equals("1") && System.currentTimeMillis() < endTimeMillis;
                    if (voterFlag == 0) {
                        status = "Vote already casted successfully!";
                    } else if (!isVoteOngoing) {
                        status = "No vote going on in " + region + "!";
                    } else {
                        status = "Select a candidate to vote.";
                    }
                    break;
                }
            }
            if (!regionFound) {
                status = "Region " + region + " not found!";
            }
            System.out.println("Vote status for region " + region + ": " + status + ", endTimeMillis: " + endTimeMillis);
            return status + ";" + endTimeMillis;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error checking vote status: " + e.getMessage());
            return "Error: " + e.getMessage() + ";0";
        }
    }

    private String castVote(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 3) return "Invalid data format";
            String region = parts[0];
            String userID = parts[1];
            String candidateName = parts[2];
            String path = BASE_PATH + region + ".txt";

            List<String> lines = Files.readAllLines(Paths.get(path));
            List<String> newLines = new ArrayList<>();
            boolean candidateFound = false;
            for (String line : lines) {
                String[] lineParts = line.trim().split("\\s+");
                if (lineParts.length >= 2) {
                    StringBuilder name = new StringBuilder();
                    for (int i = 0; i < lineParts.length - 1; i++) {
                        name.append(lineParts[i]).append(" ");
                    }
                    String cName = name.toString().trim();
                    int score = Integer.parseInt(lineParts[lineParts.length - 1]);
                    if (cName.equals(candidateName)) {
                        score++;
                        candidateFound = true;
                    }
                    newLines.add(cName + " " + score);
                } else {
                    newLines.add(line);
                }
            }
            if (!candidateFound) return "Candidate not found";
            Files.write(Paths.get(path), newLines);

            List<String> voterLines = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> newVoterLines = new ArrayList<>();
            boolean voterFound = false;
            for (String line : voterLines) {
                String[] voterParts = line.trim().split(" ", 4);
                if (voterParts.length == 4 && voterParts[0].equals(userID)) {
                    newVoterLines.add(voterParts[0] + " " + voterParts[1] + " " + voterParts[2] + " 0");
                    voterFound = true;
                } else {
                    newVoterLines.add(line);
                }
            }
            if (!voterFound) return "Voter not found";
            Files.write(Paths.get(VOTERS_FILE), newVoterLines);

            System.out.println("Vote cast for " + candidateName + " in region " + region + " by " + userID);
            return "Vote casted successfully!";
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
     catch (RuntimeException e) {
        System.err.println("Error casting vote: " + e.getMessage());
        return "Error: " + e.getMessage();
    }
}

private String resetRegionAndVoters(String region) {
    try {
        List<String> regionLines = Files.readAllLines(Paths.get(REGIONS_FILE));
        List<String> newRegionLines = new ArrayList<>();
        String defaultTime = formatter.format(Instant.ofEpochMilli(0));
        for (String line : regionLines) {
            String[] parts = line.trim().split(" ", 5);
            if (parts.length == 5 && parts[0].equals(region)) {
                newRegionLines.add(parts[0] + " 0 0 " + defaultTime + " " + defaultTime);
            } else {
                newRegionLines.add(line);
            }
        }
        Files.write(Paths.get(REGIONS_FILE), newRegionLines);

        List<String> voterLines = Files.readAllLines(Paths.get(VOTERS_FILE));
        List<String> newVoterLines = new ArrayList<>();
        for (String line : voterLines) {
            String[] parts = line.trim().split(" ", 4);
            if (parts.length == 4 && parts[2].equals(region)) {
                newVoterLines.add(parts[0] + " " + parts[1] + " " + parts[2] + " 0");
            } else {
                newVoterLines.add(line);
            }
        }
        Files.write(Paths.get(VOTERS_FILE), newVoterLines);

        String regionFile = BASE_PATH + region + ".txt";
        Files.deleteIfExists(Paths.get(regionFile));
        System.out.println("Reset region and voters for: " + region);

        return "Region and voter data reset successfully";
    } catch (IOException e) {
        System.err.println("Error resetting voting data: " + e.getMessage());
        return "Error: " + e.getMessage();
    }
}

private String getVoterRequests() {
    try {
        List<String> requests = Files.readAllLines(Paths.get(VOTER_REQUESTS_FILE));
        List<String> usernames = new ArrayList<>();
        for (String request : requests) {
            String[] parts = request.trim().split("\\s+");
            if (parts.length >= 2) {
                usernames.add(parts[0]);
            }
        }
        return String.join(";", usernames);
    } catch (IOException e) {
        System.err.println("Error loading voter requests: " + e.getMessage());
        return "Error: " + e.getMessage();
    }
}

private String approveVoter(String username) {
    try {
        List<String> requests = Files.readAllLines(Paths.get(VOTER_REQUESTS_FILE));
        String voterData = null;
        for (String request : requests) {
            String[] parts = request.trim().split("\\s+");
            if (parts.length >= 2 && parts[0].equals(username)) {
                voterData = String.format("%-12s %-10s %-6s %d", parts[0], parts[1], "Dhaka", 0);
                break;
            }
        }

        if (voterData == null) return "Voter not found in requests";

        File votersFile = new File(VOTERS_FILE);
        if (!votersFile.exists()) {
            votersFile.createNewFile();
        }
        String content = Files.exists(Paths.get(VOTERS_FILE)) ? Files.readString(Paths.get(VOTERS_FILE)) : "";
        String toWrite = voterData + "\n";
        if (!content.isEmpty() && !content.endsWith("\n")) {
            toWrite = "\n" + toWrite;
        }

        try (FileWriter writer = new FileWriter(VOTERS_FILE, true)) {
            writer.write(toWrite);
        }

        List<String> updatedRequests = new ArrayList<>();
        for (String request : requests) {
            String[] requestParts = request.trim().split("\\s+");
            if (requestParts.length >= 1 && !requestParts[0].equals(username)) {
                updatedRequests.add(request);
            }
        }

        Files.write(Paths.get(VOTER_REQUESTS_FILE), updatedRequests);
        return "Voter approved successfully";
    } catch (IOException e) {
        System.err.println("Error approving voter: " + e.getMessage());
        return "Error: " + e.getMessage();
    }
}

private String getVoters() {
    try {
        List<String> voters = Files.readAllLines(Paths.get(VOTERS_FILE));
        List<String> voterEntries = new ArrayList<>();
        for (String voter : voters) {
            String[] parts = voter.trim().split("\\s+");
            if (parts.length == 4) {
                voterEntries.add(String.join(",", parts));
            }
        }
        return String.join(";", voterEntries);
    } catch (IOException e) {
        System.err.println("Error loading voters: " + e.getMessage());
        return "Error: " + e.getMessage();
    }
}

private String removeVoter(String username) {
    try {
        List<String> voters = Files.readAllLines(Paths.get(VOTERS_FILE));
        List<String> updatedVoters = new ArrayList<>();
        boolean voterFound = false;
        for (String voter : voters) {
            String[] parts = voter.trim().split("\\s+");
            if (parts.length >= 1 && parts[0].equals(username)) {
                voterFound = true;
                continue;
            }
            updatedVoters.add(voter);
        }

        if (!voterFound) return "Voter not found";

        Files.write(Paths.get(VOTERS_FILE), updatedVoters);
        return "Voter removed successfully";
    } catch (IOException e) {
        System.err.println("Error removing voter: " + e.getMessage());
        return "Error: " + e.getMessage();
    }
}

private String changeVoterRegion(String data) {
    try {
        String[] parts = data.split(";");
        if (parts.length != 2) return "Invalid data format";
        String username = parts[0];
        String newRegion = parts[1];

        List<String> voters = Files.readAllLines(Paths.get(VOTERS_FILE));
        List<String> updatedVoters = new ArrayList<>();
        boolean voterFound = false;
        for (String voter : voters) {
            String[] voterParts = voter.trim().split("\\s+", 4);
            if (voterParts.length == 4 && voterParts[0].equals(username)) {
                updatedVoters.add(String.format("%-12s %-10s %-6s %s",
                        voterParts[0], voterParts[1], newRegion, voterParts[3]));
                voterFound = true;
            } else {
                updatedVoters.add(voter);
            }
        }

        if (!voterFound) return "Voter not found";

        Files.write(Paths.get(VOTERS_FILE), updatedVoters);
        return "Region updated successfully";
    } catch (IOException e) {
        System.err.println("Error updating voter region: " + e.getMessage());
        return "Error: " + e.getMessage();
    }
}

public static void main(String[] args) {
    new Server();
}}*/


package ProjectServer;

import com.example.project.Message;
import com.example.project.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Server {

    private static final String BASE_PATH = "D:\\Project\\Project\\src\\main\\resources\\com\\example\\project\\";
    private static final String VOTERS_FILE = BASE_PATH + "Voters.txt";
    private static final String ADMINS_FILE = BASE_PATH + "Admins.txt";
    private static final String REGIONS_FILE = BASE_PATH + "regions.txt";
    private static final String CANDIDATES_FILE = BASE_PATH + "candidates.txt";
    private static final String VOTER_REQUESTS_FILE = BASE_PATH + "VoterRequests.txt";
    private static final String ADMIN_REQUESTS_FILE = BASE_PATH + "AdminRequests";
    private static final String VOTE_INFO_FILE = BASE_PATH + "VoteInfo.txt";
    private static final String TIMER_FILE = BASE_PATH + "timer.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private ServerSocket serverSocket;
    private HashMap<String, SocketWrapper> clientMap;

    public Server() {
        clientMap = new HashMap<>();
        try {
            serverSocket = new ServerSocket(44444);
            System.out.println("Server started on port 44444");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                serve(clientSocket);
            }
        } catch (Exception e) {
            System.err.println("Server startup error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void serve(Socket clientSocket) throws IOException, ClassNotFoundException {
        SocketWrapper socketWrapper = new SocketWrapper(clientSocket);
        new Thread(() -> {
            try {
                while (true) {
                    Object o = socketWrapper.read();
                    if (o instanceof Message) {
                        Message request = (Message) o;
                        String operation = request.getTo();
                        String clientId = request.getFrom();
                        String data = request.getText();
                        System.out.println("Received request: " + operation + " from " + clientId + " with data: " + data);
                        Message response = new Message();
                        response.setFrom("Server");
                        response.setTo(clientId);

                        switch (operation) {

                            case "REGISTER_VOTER":
                                response.setText(registerVoter(data));
                                break;
                            case "REGISTER_ADMIN":
                                response.setText(registerAdmin(data));
                                break;
                            case "APPROVE_ADMIN":
                                response.setText(approveAdmin(data));
                                break;
                            case "GET_ADMIN_REQUESTS":
                                response.setText(getAdminRequests());
                                break;
                            case "GET_ADMIN_DETAILS":
                                response.setText(getAdminDetails(data));
                                break;
                            case "CHECK_LOGIN":
                                response.setText(checkLogin(data));
                                break;
                            case "GET_REGIONS":
                                response.setText(getRegions());
                                break;
                            case "GET_REGION_FLAG":
                                response.setText(getRegionFlag(data));
                                break;
                            case "GET_REGION_DATA":
                                response.setText(getRegionData(data));
                                break;
                            case "GET_CANDIDATES":
                                response.setText(getCandidates());
                                break;
                            case "GET_V_R_DETAILS":
                                response.setText(getvrd(data));
                                break;
                            case "START_VOTE":
                                response.setText(startVote(data));
                                break;
                            case "TERMINATE_VOTE":
                                response.setText(terminateVote(data));
                                break;
                            case "GET_VOTE_INFO":
                                response.setText(getVoteInfo());
                                break;
                            case "CHECK_EXPIRED_VOTES":
                                response.setText(checkExpiredVotes());
                                break;
                            case "GET_REGION_CANDIDATES":
                                response.setText(getRegionCandidates(data));
                                break;
                            case "CHECK_VOTE_STATUS":
                                response.setText(checkVoteStatus(data));
                                break;
                            case "CAST_VOTE":
                                response.setText(castVote(data));
                                break;
                            case "RESET_REGION_AND_VOTERS":
                                response.setText(resetRegionAndVoters(data));
                                break;
                            case "GET_VOTER_REQUESTS":
                                response.setText(getVoterRequests());
                                break;
                            case "APPROVE_VOTER":
                                response.setText(approveVoter(data));
                                break;
                            case "GET_VOTERS":
                                response.setText(getVoters());
                                break;
                            case "REMOVE_VOTER":
                                response.setText(removeVoter(data));
                                break;
                            case "CHANGE_VOTER_REGION":
                                response.setText(changeVoterRegion(data));
                                break;
                            case "GET_REGION_FILE":
                                response.setText(getRegionFile(data));
                                break;
                            default:
                                response.setText("Unknown operation: " + operation);
                                System.err.println("Unknown operation requested: " + operation);
                        }
                        System.out.println("Sending response to " + clientId + ": " + response.getText());
                        socketWrapper.write(response);
                    }
                }
            } catch (Exception e) {
                System.err.println("Client error: " + e.getMessage());
                try {
                    socketWrapper.closeConnection();
                } catch (IOException ex) {
                    System.err.println("Error closing connection: " + ex.getMessage());
                }
            }
        }).start();
    }

    private String getvrd(String voter){

        try {
            List<String> lines = Files.readAllLines(Paths.get(VOTER_REQUESTS_FILE));
            for (String line : lines) {
                String[] parts = line.split(" ", 4);
                if (parts.length == 4 && parts[0].equals(voter)) {
                    String result = parts[0] + ";" + parts[1] + ";" + parts[2] + ";" + parts[3]; ;

                    return result;
                }
            }
           // System.err.println("Region not found: " + region);
           // return "Region not found";
        } catch (IOException e) {
            System.err.println("Error getting region data: " + e.getMessage());
            return "Error: " + e.getMessage();
        }

        return "Nai";
    }

    private String registerVoter(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 4) return "Invalid data format";
            String username = parts[0];
            String password = parts[1];
            String mobile = parts[2];
String region=parts[3];

            if (username.length() != 10) return "Username must be exactly 10 characters.";
            if (!mobile.matches("\\d{11}")) return "Mobile number must be exactly 11 digits.";
            if (password.length() < 8) return "Password should be at least 8 characters.";

            File file = new File(VOTER_REQUESTS_FILE);
            if (!file.exists()) file.createNewFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] existing = line.trim().split("\\s+");
                    if (existing.length >= 1 && existing[0].equals(username)) {
                        return "Username already exists.";
                    }
                }
            }

            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(username + " " + password + " " + mobile + " " + region+ "\n");
                writer.flush();
            }
            return "Registration request submitted successfully.";
        } catch (IOException e) {
            System.err.println("Error registering voter: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String registerAdmin(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 3) return "Invalid data format";
            String username = parts[0];
            String password = parts[1];
            String mobile = parts[2];

            if (username.length() != 7) return "Username must be exactly 7 characters.";
            if (!mobile.matches("\\d{11}")) return "Mobile number must be exactly 11 digits.";
            if (password.length() != 8) return "Password must be exactly 8 characters.";

            File file = new File(ADMIN_REQUESTS_FILE);
            if (!file.exists()) file.createNewFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] existing = line.trim().split("\\s+");
                    if (existing.length >= 1 && existing[0].equals(username)) {
                        return "Username already exists.";
                    }
                }
            }

            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(username + " " + password + " " + mobile + "\n");
                writer.flush();
            }
            return "Registration request submitted successfully.";
        } catch (IOException e) {
            System.err.println("Error registering admin: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String approveAdmin(String data) {
        try {
            String username = data;
            List<String> requests = Files.readAllLines(Paths.get(ADMIN_REQUESTS_FILE));
            String adminData = null;
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 3 && parts[0].equals(username)) {
                    adminData = parts[0] + " " + parts[1];
                    break;
                }
            }

            if (adminData == null) return "Admin not found in requests";

            File adminsFile = new File(ADMINS_FILE);
            if (!adminsFile.exists()) {
                adminsFile.createNewFile();
            }
            String content = Files.exists(Paths.get(ADMINS_FILE)) ? Files.readString(Paths.get(ADMINS_FILE)) : "";
            String toWrite = adminData + "\n";
            if (!content.isEmpty() && !content.endsWith("\n")) {
                toWrite = "\n" + toWrite;
            }

            try (FileWriter writer = new FileWriter(ADMINS_FILE, true)) {
                writer.write(toWrite);
            }

            List<String> updatedRequests = new ArrayList<>();
            for (String request : requests) {
                String[] requestParts = request.trim().split("\\s+");
                if (requestParts.length >= 1 && !requestParts[0].equals(username)) {
                    updatedRequests.add(request);
                }
            }

            Files.write(Paths.get(ADMIN_REQUESTS_FILE), updatedRequests);
            return "Admin approved successfully";
        } catch (IOException e) {
            System.err.println("Error approving admin: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getAdminRequests() {
        try {
            List<String> requests = Files.readAllLines(Paths.get(ADMIN_REQUESTS_FILE));
            List<String> usernames = new ArrayList<>();
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 1) {
                    usernames.add(parts[0]);
                }
            }
            return String.join(";", usernames);
        } catch (IOException e) {
            System.err.println("Error loading admin requests: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getAdminDetails(String username) {
        try {
            List<String> requests = Files.readAllLines(Paths.get(ADMIN_REQUESTS_FILE));
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 3 && parts[0].equals(username)) {
                    return "Username: " + parts[0] + "\nMobile: " + parts[2];
                }
            }
            return "Details not found for " + username;
        } catch (IOException e) {
            System.err.println("Error loading admin details: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String checkLogin(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 2) return "Invalid data format";
            String userID = parts[0];
            String password = parts[1];
            boolean isAdmin = false;
            boolean isVoter = false;
            String voterRegion = null;
            int voterFlag = -1;

            try (BufferedReader reader = new BufferedReader(new FileReader(ADMINS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] adminParts = line.trim().split("\\s+");
                    if (adminParts.length >= 2 && adminParts[0].equals(userID) && adminParts[1].equals(password)) {
                        isAdmin = true;
                        break;
                    }
                }
            }

            if (isAdmin) return "ADMIN_SUCCESS;" + userID;

            try (BufferedReader reader = new BufferedReader(new FileReader(VOTERS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] voterParts = line.trim().split("\\s+");
                    if (voterParts.length >= 4 && voterParts[0].equals(userID) && voterParts[1].equals(password)) {
                        isVoter = true;
                        voterRegion = voterParts[2];
                        voterFlag = Integer.parseInt(voterParts[3]);
                        break;
                    }
                }
            }

            if (isVoter) return "VOTER_SUCCESS;" + userID + ";" + voterRegion + ";" + voterFlag;
            return "Wrong Login Info!!!";
        } catch (IOException e) {
            System.err.println("Error checking login: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getRegions() {
        try {
            if (!Files.exists(Paths.get(REGIONS_FILE))) {
                List<String> defaultRegions = List.of("Dhaka", "Chittagong", "Rajshahi", "Khulna", "Barisal", "Sylhet");
                Files.write(Paths.get(REGIONS_FILE),
                        defaultRegions.stream().map(r -> r + " 0 0 0 0").collect(Collectors.toList()));
            }
            List<String> regions = Files.readAllLines(Paths.get(REGIONS_FILE)).stream()
                    .map(line -> line.split(" ")[0])
                    .collect(Collectors.toList());
            //System.out.println("Regions loaded: " + String.join(";", regions));
            return String.join(";", regions);
        } catch (IOException e) {
            System.err.println("Error loading regions: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getRegionFlag(String region) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            for (String line : lines) {
                String[] parts = line.split(" ", 6);
                if (parts.length == 6&& parts[0].equals(region)) {
                    System.out.println("Region flag for " + region + ": " + parts[1]);
                    return parts[1];
                }
            }
            System.err.println("Region not found: " + region);
            return "Region not found";
        } catch (IOException e) {
            System.err.println("Error getting region flag: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getRegionData(String region) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            for (String line : lines) {
                String[] parts = line.split(" ", 6);
                if (parts.length == 6 && parts[0].equals(region)) {
                    String result = parts[1] + ";" + parts[2] + ";" + parts[3] + ";" + parts[4]+";"+ parts[5] ;
                    System.out.println("Region data for " + region + ": " + result);
                    return result;
                }
            }
            System.err.println("Region not found: " + region);
            return "Region not found";
        } catch (IOException e) {
            System.err.println("Error getting region data: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getCandidates() {
        try {
            List<String> candidates = Files.exists(Paths.get(CANDIDATES_FILE))
                    ? Files.readAllLines(Paths.get(CANDIDATES_FILE))
                    : List.of("Abdul Rahman", "Fatima Begum", "Mohammed Hasan", "Ayesha Khatun", "Rashed Khan", "Nusrat Jahan");
            Files.write(Paths.get(CANDIDATES_FILE), candidates);
            System.out.println("Candidates loaded: " + String.join(";", candidates));
            return String.join(";", candidates);
        } catch (IOException e) {
            System.err.println("Error getting candidates: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String startVote(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length < 3) return "Invalid data format";
            String region = parts[0];
            long durationSeconds = Long.parseLong(parts[1]);
            String candidates = parts[2];
            String regionFile = BASE_PATH + region + ".txt";

            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            List<String> updatedLines = new ArrayList<>();
            long startTimeMillis = Instant.now().toEpochMilli();
            long endTimeMillis = startTimeMillis + durationSeconds * 1000;
            String startTime = formatter.format(Instant.ofEpochMilli(startTimeMillis));
            String endTime = formatter.format(Instant.ofEpochMilli(endTimeMillis));

            boolean regionFound = false;
            for (String line : lines) {
                String[] lineParts = line.split(" ", 5);
                if (lineParts.length == 5 && lineParts[0].equals(region)) {
                    regionFound = true;
                    updatedLines.add(region + " 1 " + endTimeMillis + " " + startTime + " " + endTime);
                } else {
                    updatedLines.add(line);
                }
            }
            if (!regionFound) return "Region not found";

            Files.write(Paths.get(REGIONS_FILE), updatedLines);

            List<String> candidateLines = Arrays.stream(candidates.split(","))
                    .map(c -> c.trim() + " 0")
                    .collect(Collectors.toList());
            Files.write(Paths.get(regionFile), candidateLines);

            // Update voter flags to 1 for all voters in the region
            List<String> voterLines = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> newVoterLines = new ArrayList<>();
            for (String line : voterLines) {
                String[] voterParts = line.trim().split(" ", 4);
                if (voterParts.length == 4 && voterParts[2].equals(region)) {
                    newVoterLines.add(voterParts[0] + " " + voterParts[1] + " " + voterParts[2] + " 1");
                } else {
                    newVoterLines.add(line);
                }
            }
            Files.write(Paths.get(VOTERS_FILE), newVoterLines);

            System.out.println("Started vote for region: " + region + ", candidates: " + candidates + ", voter flags set to 1");
            return "Voting has started";
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error starting vote: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String terminateVote(String region) {
        try {
            String regionFile = BASE_PATH + region + ".txt";
            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            List<String> updatedLines = new ArrayList<>();
            String defaultTime = formatter.format(Instant.ofEpochMilli(0));
            boolean regionFound = false;

            for (String line : lines) {
                String[] parts = line.split(" ", 7);
                if (parts.length == 7 && parts[0].equals(region)) {
                    regionFound = true;
                    updatedLines.add(region + " 0 0 " + parts[3] + " " + parts[4]+" "+parts[5] + " " + parts[6]);
                    //Files.deleteIfExists(Paths.get(regionFile));
                   // System.out.println("Deleted region file: " + regionFile);
                } else {
                    updatedLines.add(line);
                }
            }
            if (!regionFound) return "Region not found";

            Files.write(Paths.get(REGIONS_FILE), updatedLines);

            // Update voter flags to 0 for all voters in the region
            List<String> voterLines = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> newVoterLines = new ArrayList<>();
            for (String line : voterLines) {
                String[] voterParts = line.trim().split(" ", 4);
                if (voterParts.length == 4 && voterParts[2].equals(region)) {
                    newVoterLines.add(voterParts[0] + " " + voterParts[1] + " " + voterParts[2] + " 0");
                } else {
                    newVoterLines.add(line);
                }
            }
            Files.write(Paths.get(VOTERS_FILE), newVoterLines);

            System.out.println("Terminated vote for region: " + region + ", voter flags set to 0");
            return "Voting terminated";
        } catch (IOException e) {
            System.err.println("Error terminating vote: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getVoteInfo() {
        try {
            List<String> lines = Files.exists(Paths.get(VOTE_INFO_FILE))
                    ? Files.readAllLines(Paths.get(VOTE_INFO_FILE))
                    : new ArrayList<>();
            return String.join(";", lines);
        } catch (IOException e) {
            System.err.println("Error getting vote info: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String checkExpiredVotes() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(REGIONS_FILE));
            List<String> updatedLines = new ArrayList<>();
            boolean changesMade = false;

            for (String line : lines) {
                try {
                    String[] parts = line.split(" ", 7);
                    if (parts.length != 7) {
                        System.err.println("[ERROR] Invalid region file format: " + line);
                        updatedLines.add(line);
                        continue;
                    }
                    String region = parts[0];
                    String flag = parts[1];
                    if (flag.equals("1")) {
                        String endTimeString = parts[5] + " " + parts[6];
                        try {
                            LocalDateTime endTime = LocalDateTime.parse(endTimeString, formatter);
                            LocalDateTime currentTime = LocalDateTime.now();
                            System.out.println("Checking region: " + region + ", Current: " + currentTime + ", End: " + endTime);
                            if (currentTime.isAfter(endTime)) {
                                String regionFile = BASE_PATH + region + ".txt";
                                //Files.deleteIfExists(Paths.get(regionFile));
                                System.out.println("Expired vote, deleted file: " + regionFile);
                                updatedLines.add(region + " 0 0 " + parts[3] + " " + parts[4]+" "+parts[5] + " " + parts[6]);
                                // Update voter flags to 0
                                List<String> voterLines = Files.readAllLines(Paths.get(VOTERS_FILE));
                                List<String> newVoterLines = new ArrayList<>();
                                for (String voterLine : voterLines) {
                                    String[] voterParts = voterLine.trim().split(" ", 4);
                                    if (voterParts.length == 4 && voterParts[2].equals(region)) {
                                        newVoterLines.add(voterParts[0] + " " + voterParts[1] + " " + voterParts[2] + " 0");
                                    } else {
                                        newVoterLines.add(voterLine);
                                    }
                                }
                                Files.write(Paths.get(VOTERS_FILE), newVoterLines);
                                //System.out.println("Set voter flags to 0 for region: " + region);
                                changesMade = true;
                            } else {
                                updatedLines.add(line);
                            }
                        } catch (DateTimeParseException e) {
                            System.err.println("[ERROR] Invalid time format for region " + region + ": " + endTimeString);
                            updatedLines.add(line);
                        }
                    } else {
                        updatedLines.add(line);
                    }
                } catch (Exception e) {
                    System.err.println("[ERROR] Failed processing line: " + line + ", Error: " + e.getMessage());
                    updatedLines.add(line);
                }
            }

            if (changesMade) {
                Files.write(Paths.get(REGIONS_FILE), updatedLines);
                System.out.println("Updated regions.txt after expiration check");
            } else {
                System.out.println("No expired votes found");
            }
            return "Checked expired votes";
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to check expired votes: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getRegionCandidates(String region) {
        try {
            String path = BASE_PATH + region + ".txt";
            if (!Files.exists(Paths.get(path))) {
                System.out.println("No candidates file found for region: " + region);
                return "No candidates found for " + region;
            }
            List<String> lines = Files.readAllLines(Paths.get(path));
            List<String> candidates = new ArrayList<>();
            for (String line : lines) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    StringBuilder name = new StringBuilder();
                    for (int i = 0; i < parts.length - 1; i++) {
                        name.append(parts[i]).append(" ");
                    }
                    int score;
                    try {
                        score = Integer.parseInt(parts[parts.length - 1]);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid vote count in line: " + line);
                        continue;
                    }
                    candidates.add(name.toString().trim() + ";" + score);
                } else {
                    System.err.println("Invalid candidate line format: " + line);
                }
            }
            String result = String.join("|", candidates);
            System.out.println("Candidates for " + region + ": " + result);
            return result.isEmpty() ? "No candidates found for " + region : result;
        } catch (IOException e) {
            System.err.println("Error loading candidates for " + region + ": " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getRegionFile(String region) {
        try {
            String path = BASE_PATH + region + ".txt";
            if (!Files.exists(Paths.get(path))) {
                System.out.println("No region file found: " + path);
                return "No candidates found for " + region;
            }
            List<String> lines = Files.readAllLines(Paths.get(path));
            String result = String.join("\n", lines);
            System.out.println("Region file contents for " + region + ": " + result);
            return result.isEmpty() ? "No candidates found for " + region : result;
        } catch (IOException e) {
            System.err.println("Error reading region file for " + region + ": " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String checkVoteStatus(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 2) return "Invalid data format";
            String region = parts[0];
            int voterFlag = Integer.parseInt(parts[1]);

            List<String> regionLines = Files.readAllLines(Paths.get(REGIONS_FILE));
            boolean regionFound = false;
            String status = "";
            long endTimeMillis = 0;
            for (String line : regionLines) {
                String[] regionParts = line.trim().split(" ", 5);
                if (regionParts.length == 5 && regionParts[0].equals(region)) {
                    regionFound = true;
                    endTimeMillis = Long.parseLong(regionParts[2]);
                    boolean isVoteOngoing = regionParts[1].equals("1") && System.currentTimeMillis() < endTimeMillis;
                    if (voterFlag == 0) {
                        status = "Vote already casted successfully!";
                    } else if (!isVoteOngoing) {
                        status = "No vote going on in " + region + "!";
                    } else {
                        status = "Select a candidate to vote.";
                    }
                    break;
                }
            }
            if (!regionFound) {
                status = "Region " + region + " not found!";
            }
            System.out.println("Vote status for region " + region + ": " + status + ", endTimeMillis: " + endTimeMillis);
            return status + ";" + endTimeMillis;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error checking vote status: " + e.getMessage());
            return "Error: " + e.getMessage() + ";0";
        }
    }

    private String castVote(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 3) return "Invalid data format";
            String region = parts[0];
            String userID = parts[1];
            String candidateName = parts[2];
            String path = BASE_PATH + region + ".txt";

            List<String> lines = Files.readAllLines(Paths.get(path));
            List<String> newLines = new ArrayList<>();
            boolean candidateFound = false;
            for (String line : lines) {
                String[] lineParts = line.trim().split("\\s+");
                if (lineParts.length >= 2) {
                    StringBuilder name = new StringBuilder();
                    for (int i = 0; i < lineParts.length - 1; i++) {
                        name.append(lineParts[i]).append(" ");
                    }
                    String cName = name.toString().trim();
                    int score = Integer.parseInt(lineParts[lineParts.length - 1]);
                    if (cName.equals(candidateName)) {
                        score++;
                        candidateFound = true;
                    }
                    newLines.add(cName + " " + score);
                } else {
                    newLines.add(line);
                }
            }
            if (!candidateFound) return "Candidate not found";
            Files.write(Paths.get(path), newLines);

            List<String> voterLines = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> newVoterLines = new ArrayList<>();
            boolean voterFound = false;
            for (String line : voterLines) {
                String[] voterParts = line.trim().split(" ", 4);
                if (voterParts.length == 4 && voterParts[0].equals(userID)) {
                    newVoterLines.add(voterParts[0] + " " + voterParts[1] + " " + voterParts[2] + " 0");
                    voterFound = true;
                } else {
                    newVoterLines.add(line);
                }
            }
            if (!voterFound) return "Voter not found";
            Files.write(Paths.get(VOTERS_FILE), newVoterLines);

            System.out.println("Vote cast for " + candidateName + " in region " + region + " by " + userID);
            return "Vote casted successfully!";
        } catch (IOException e) {
            System.err.println("Error casting vote: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String resetRegionAndVoters(String region) {
        try {
            List<String> regionLines = Files.readAllLines(Paths.get(REGIONS_FILE));
            List<String> newRegionLines = new ArrayList<>();
            String defaultTime = formatter.format(Instant.ofEpochMilli(0));
            for (String line : regionLines) {
                String[] parts = line.trim().split(" ", 7);
                if (parts.length == 7 && parts[0].equals(region)) {
                    newRegionLines.add(parts[0] + " 0 0 " + parts[3] + " " +parts[4] + " " +parts[5] + " " +parts[6]);
                } else {
                    newRegionLines.add(line);
                }
            }
            Files.write(Paths.get(REGIONS_FILE), newRegionLines);

            List<String> voterLines = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> newVoterLines = new ArrayList<>();
            for (String line : voterLines) {
                String[] parts = line.trim().split(" ", 4);
                if (parts.length == 4 && parts[2].equals(region)) {
                    newVoterLines.add(parts[0] + " " + parts[1] + " " + parts[2] + " 0");
                } else {
                    newVoterLines.add(line);
                }
            }
            Files.write(Paths.get(VOTERS_FILE), newVoterLines);

            String regionFile = BASE_PATH + region + ".txt";
           // Files.deleteIfExists(Paths.get(regionFile));
            System.out.println("Reset region and voters for: " + region);

            return "Region and voter data reset successfully";
        } catch (IOException e) {
            System.err.println("Error resetting voting data: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getVoterRequests() {
        try {
            List<String> requests = Files.readAllLines(Paths.get(VOTER_REQUESTS_FILE));
            List<String> usernames = new ArrayList<>();
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 2) {
                    usernames.add(parts[0]);
                }
            }
            return String.join(";", usernames);
        } catch (IOException e) {
            System.err.println("Error loading voter requests: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String approveVoter(String username) {
        try {
            List<String> requests = Files.readAllLines(Paths.get(VOTER_REQUESTS_FILE));
            String voterData = null;
            for (String request : requests) {
                String[] parts = request.trim().split("\\s+");
                if (parts.length >= 3 && parts[0].equals(username)) {
                    voterData = parts[0]+" "+parts[1]+" "+parts[3]+" "+"0";
                    break;
                }
            }

            if (voterData == null) return "Voter not found in requests";

            File votersFile = new File(VOTERS_FILE);
            if (!votersFile.exists()) {
                votersFile.createNewFile();
            }
            String content = Files.exists(Paths.get(VOTERS_FILE)) ? Files.readString(Paths.get(VOTERS_FILE)) : "";
            String toWrite = voterData + "\n";
            if (!content.isEmpty() && !content.endsWith("\n")) {
                toWrite = "\n" + toWrite;
            }

            try (FileWriter writer = new FileWriter(VOTERS_FILE, true)) {
                writer.write(toWrite);
            }

            List<String> updatedRequests = new ArrayList<>();
            for (String request : requests) {
                String[] requestParts = request.trim().split("\\s+");
                if (requestParts.length >= 1 && !requestParts[0].equals(username)) {
                    updatedRequests.add(request);
                }
            }

            Files.write(Paths.get(VOTER_REQUESTS_FILE), updatedRequests);
            return "Voter approved successfully";
        } catch (IOException e) {
            System.err.println("Error approving voter: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String getVoters() {
        try {
            List<String> voters = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> voterEntries = new ArrayList<>();
            for (String voter : voters) {
                String[] parts = voter.trim().split("\\s+");
                if (parts.length == 4) {
                    voterEntries.add(String.join(",", parts));
                }
            }
            return String.join(";", voterEntries);
        } catch (IOException e) {
            System.err.println("Error loading voters: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String removeVoter(String username) {
        try {
            List<String> voters = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> updatedVoters = new ArrayList<>();
            boolean voterFound = false;
            for (String voter : voters) {
                String[] parts = voter.trim().split("\\s+");
                if (parts.length >= 1 && parts[0].equals(username)) {
                    voterFound = true;
                    continue;
                }
                updatedVoters.add(voter);
            }

            if (!voterFound) return "Voter not found";

            Files.write(Paths.get(VOTERS_FILE), updatedVoters);
            return "Voter removed successfully";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String changeVoterRegion(String data) {
        try {
            String[] parts = data.split(";");
            if (parts.length != 2) return "Invalid data format";
            String username = parts[0];
            String newRegion = parts[1];

            List<String> voters = Files.readAllLines(Paths.get(VOTERS_FILE));
            List<String> updatedVoters = new ArrayList<>();
            boolean voterFound = false;
            for (String voter : voters) {
                String[] voterParts = voter.trim().split("\\s+", 4);
                if (voterParts.length == 4 && voterParts[0].equals(username)) {
                    updatedVoters.add(voterParts[0]+" " +voterParts[1]+" " +newRegion+" "+ voterParts[3]);
                    voterFound = true;
                } else {
                    updatedVoters.add(voter);
                }
            }

            if (!voterFound) return "Voter not found";

            Files.write(Paths.get(VOTERS_FILE), updatedVoters);
            return "Region updated successfully";
        } catch (IOException e) {
            System.err.println("Error updating voter region: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}