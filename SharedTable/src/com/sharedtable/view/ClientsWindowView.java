package com.sharedtable.view;

import com.sharedtable.controller.ClientPropertyWindowController;
import com.sharedtable.controller.ClientsWindowController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientsWindowView {

    public ClientsWindowView(Stage owner) {
        if(isOpened)
            return;
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
        stage.initOwner(owner);
        stage.initModality(Modality.NONE);
        stage.setScene(scene);
        stage.setTitle("Kliens fa");
        stage.setOnCloseRequest(event -> {
            isOpened = false;
            ((ClientsWindowController)fxmlLoader.getController()).onClose();
        });
        isOpened = true;
        stage.showAndWait();
    }

    private static boolean isOpened=false;
}
