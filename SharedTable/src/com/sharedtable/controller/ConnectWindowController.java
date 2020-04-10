package com.sharedtable.controller;

import com.sharedtable.view.MessageBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ConnectWindowController implements Initializable {

    @FXML
    void btnConnectButtonClicked(ActionEvent event) {
        try{
            connectionLink = new ConnectionLink(connectionLinkField.getText());
        } catch (IllegalArgumentException e) {
            MessageBox.showError("Érvénytelen kapcsolódási link!","A megadott kapcsolódási link érvénytelen!\n"+e.getMessage());
            return;
        }
        isCanceled = false;
        closeStage(event);
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

    private ConnectionLink connectionLink;

    @FXML
    private TextField connectionLinkField;
    @FXML
    private PasswordField passwordField;
    private boolean isCanceled = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}