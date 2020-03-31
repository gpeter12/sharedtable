package com.sharedtable.view;

import com.sharedtable.controller.CreateTabController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class CreateTabView {


    public CreateTabView() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("CreateTabWindow.fxml"));
        this.fxmlLoader = fxmlLoader;
        Parent parent;
        try{parent = fxmlLoader.load();}
        catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Scene scene = new Scene(parent, 600, 100);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
    }

    public CreateTabController getController() {
        return fxmlLoader.getController();
    }

    private FXMLLoader fxmlLoader;


}
