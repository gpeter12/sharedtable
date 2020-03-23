package com.sharedtable.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ConnectWindowView {
    public ConnectWindowView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ConnectWindow.fxml"));
        Parent parent;
        try{parent = fxmlLoader.load();}
        catch (IOException e) {
            System.out.println("failed to get resource MainView.fxml");
            return;
        }

        Scene scene = new Scene(parent, 600, 100);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
    }
}
