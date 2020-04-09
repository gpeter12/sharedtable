package com.sharedtable.view;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class GeneralView {

    public GeneralView(String fxmlFileResource, Modality modality, Stage owner, String title) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource(fxmlFileResource));
        this.fxmlLoader = fxmlLoader;
        Parent parent;
        try{parent = fxmlLoader.load();}
        catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Scene scene = new Scene(parent);
        stage = new Stage();
        stage.setTitle(title);
        if(owner != null){
            stage.initOwner(owner);
        }
        stage.setResizable(false);
        stage.initModality(modality);
        stage.setScene(scene);

    }

    public Initializable getController() {
        return fxmlLoader.getController();
    }

    protected Stage getStage() {return stage;}

    private FXMLLoader fxmlLoader;
    private Stage stage;
}
