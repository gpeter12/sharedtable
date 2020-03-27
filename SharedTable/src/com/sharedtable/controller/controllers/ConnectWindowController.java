package com.sharedtable.controller.controllers;

import com.sharedtable.controller.ConnectionLink;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ConnectWindowController {

    @FXML
    void btnConnectButtonClicked(ActionEvent event) {
        connectionLink = new ConnectionLink(connectionLinkField.getText());
        closeStage(event);
    }

    public static ConnectionLink getConnectionLink() {
        return connectionLink;
    }

    private void closeStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    private static ConnectionLink connectionLink;

    @FXML
    private TextField connectionLinkField;
}