package com.sharedtable.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SetClientDataWindowController implements Initializable {

    @FXML
    private TextField nicknameField;
    private boolean isCanceled = true;

    public SetClientDataWindowController() {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    public void btnOKClicked(ActionEvent actionEvent) {
        isCanceled = false;
        closeStage(actionEvent);
    }

    @FXML
    public void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            isCanceled = false;
            Node source = (Node) keyEvent.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }
    }

    public boolean isCanceled() {return isCanceled;}

    private void closeStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
    public String getNickname() {
        if(nicknameField.getText().isEmpty())
            return "nickname";
        else
            return nicknameField.getText();
    }




}
