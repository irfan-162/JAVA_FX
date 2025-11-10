package com.example.project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class UserInterface {

   @FXML
   private Button primaryLogin;
   @FXML private Button adminReg;
    @FXML private Button voterReg;
    @FXML private Button credits;
   @FXML  private Button help;
   @FXML private Button resultsUI;

    private Scene scene;
    private Parent root;
    private Stage stage;

    public void primaryLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(("loginInterface.fxml"))));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void voterReg(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(("voterRegistration.fxml"))));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void adminReg(ActionEvent event) throws IOException {

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(("adminRegistration.fxml"))));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    };
    public void credits(){};
    public void seeInfo(ActionEvent event) throws IOException {


        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("manageVotes.fxml")));
        Parent root = loader.load();
        ManageVotes controller = loader.getController();
        controller.setToNormalMode();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    };
    public void resultsUI(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("Result.fxml")));
        Parent root = loader.load();
        Result controller = loader.getController();
        controller.setToFalse(); // Call after FXML loading
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
