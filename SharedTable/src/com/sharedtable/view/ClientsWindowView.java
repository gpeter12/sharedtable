package com.sharedtable.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientsWindowView {

    public ClientsWindowView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ClientsWindow.fxml"));
        Parent parent;
        try{parent = fxmlLoader.load();}
        catch (IOException e) {
            System.out.println("failed to get resource ClientsWindow.fxml");
            System.out.println(e);
            return;
        }

        Scene scene = new Scene(parent, 450, 450);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setTitle("Kliens fa");
        stage.showAndWait();

    }
}
