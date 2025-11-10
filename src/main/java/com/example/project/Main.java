package com.example.project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main extends Application {

    private static Stage stg;
    private static SocketWrapper socketWrapper;

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            socketWrapper = new SocketWrapper("127.0.0.1", 44444);
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }

        stg = primaryStage;
        primaryStage.setResizable(false);
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("userInterface.fxml")));
        primaryStage.setTitle("E-Vote");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        startVoteExpirationCheck();
    }

    private void startVoteExpirationCheck() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            try {
                Message request = new Message();
                request.setFrom("Main");
                request.setTo("CHECK_EXPIRED_VOTES");
                if (socketWrapper != null) {
                    socketWrapper.write(request);
                    Message response = (Message) socketWrapper.read();
//System.out.println("Vote expiration check: " + response.getText());
                } else {
                    System.err.println("[TIMER ERROR] socketWrapper is null");
                }


            } catch (Exception e) {
                System.err.println("[TIMER ERROR] " + e.getMessage());
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        launch();
    }
}