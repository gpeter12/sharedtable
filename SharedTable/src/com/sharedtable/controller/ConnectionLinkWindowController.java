package com.sharedtable.controller;

import com.sharedtable.model.network.NetworkService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ResourceBundle;

public class ConnectionLinkWindowController implements Initializable {

    public ConnectionLinkWindowController() {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connectionLinkField.setText(generateConnectionLink());
    }

    @FXML
    public void btnCopyClicked(ActionEvent actionEvent) {
        placeTextToSystemClipboard(connectionLinkField.getText());
    }

    private void placeTextToSystemClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private String generateConnectionLink() {
        StringBuilder sb = new StringBuilder();
        sb.append("stconnect://")
                .append(UserID.getPublicIP()).append(":")
                .append(NetworkService.getOpenedPort());
        return sb.toString();
    }

    @FXML
    private TextField connectionLinkField;


}
