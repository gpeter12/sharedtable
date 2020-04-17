package com.sharedtable.controller;

import com.sharedtable.view.MessageBox;
import com.sharedtable.view.UpdateNotificationView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

public class UpdateNotificationWindowController implements Initializable {

    public UpdateNotificationWindowController() {}

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        webView.getEngine().load("http://gpeter12.web.elte.hu/verdesc.html");
    }

    @FXML
    private WebView webView;

    public void onUpdateButtonClicked(ActionEvent actionEvent) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI("http://gpeter12.web.elte.hu/sharedtable/download.html"));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }).start();
        } else {
            MessageBox.showError("Nem nyitható meg böngésző!","Ez az OS nem támogatja \nböngésző ablak megnyitását");
        }
    }
}
