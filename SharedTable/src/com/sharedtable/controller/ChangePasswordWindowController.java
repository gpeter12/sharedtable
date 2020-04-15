package com.sharedtable.controller;

import com.sharedtable.view.MessageBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ChangePasswordWindowController implements Initializable {

    public ChangePasswordWindowController() {

    }

    public void btnChangePasswordClicked(ActionEvent actionEvent) {
        try{
            onClosing();
        } catch (IllegalArgumentException e) {
            onPasswordMismatch();
            return;
        }
        closeStage(actionEvent);
    }

    @FXML
    public void onKeyPressed(KeyEvent keyEvent) {
        try{
            onClosing();
        } catch (IllegalArgumentException e) {
            onPasswordMismatch();
            return;
        }
        Node source = (Node) keyEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    private void onPasswordMismatch() {
        MessageBox.showError("A megadott jelszavak nem egyeznek meg!","A két mezőnek ugyanazt a jelszót kell tartalmaznia!");
        isCanceled = true;
    }

    private void onClosing() throws IllegalArgumentException{
        if(passwordField1.getText().equals(passwordField2.getText())) {
            isCanceled = false;
        } else {
            throw new IllegalArgumentException("password mismatch!");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }


    public String getPassword() {
        return passwordField1.getText();
    }

    public boolean isCanceled() {return isCanceled;}

    private void closeStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }


    @FXML
    private PasswordField passwordField1;
    @FXML
    private PasswordField passwordField2;


    private boolean isCanceled = true;



}
