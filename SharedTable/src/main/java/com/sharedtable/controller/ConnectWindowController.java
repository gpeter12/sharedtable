package com.sharedtable.controller;

import com.sharedtable.view.MessageBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ConnectWindowController implements Initializable {

    private ConnectionLink connectionLink;

    @FXML
    private TextField connectionLinkField;
    @FXML
    private PasswordField passwordField;
    private boolean isCanceled = true;

    @FXML
    void btnConnectButtonClicked(ActionEvent event) {
        try {
            onClosing();
        } catch (IllegalArgumentException e) {
            onInvalidInput(e);
            return;
        }
        closeStage(event);
    }

    @FXML
    public void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            try {
                onClosing();
            } catch (IllegalArgumentException e) {
                onInvalidInput(e);
                return;
            }
            onClosing();
            Node source = (Node) keyEvent.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }
    }

    private void onInvalidInput(IllegalArgumentException e) {
        MessageBox.showError("Érvénytelen kapcsolódási link!","A megadott kapcsolódási link érvénytelen!\n"+e.getMessage());
        isCanceled =true;
    }

    private void onClosing() throws IllegalArgumentException{
        connectionLink = new ConnectionLink(connectionLinkField.getText());
        isCanceled = false;
    }

    public ConnectionLink getConnectionLink() {
        return connectionLink;
    }

    public String getPassword() {
        return passwordField.getText();
    }

    public boolean isCanceled() {return isCanceled;}

    private void closeStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }


}