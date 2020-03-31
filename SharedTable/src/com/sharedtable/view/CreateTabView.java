package com.sharedtable.view;

import com.sharedtable.controller.CreateTabController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class CreateTabView {


    public CreateTabView() {
        //System.out.println("URL: "+new URL("file:/home/gpeter/Dokumentumok/projects/SharedTable/SharedTable/out/production/SharedTable/com/sharedtable/view/CreateTabWindow.fxml"));
        FXMLLoader fxmlLoader = null;
        try {
            fxmlLoader = new FXMLLoader(new URL("file:/home/gpeter/Dokumentumok/projects/SharedTable/SharedTable/out/production/SharedTable/com/sharedtable/view/CreateTabWindow.fxml"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.fxmlLoader = fxmlLoader;
        Parent parent;
        try{parent = fxmlLoader.load();}
        catch (IOException e) {
            System.out.println("failed to get resource CreateTabWindow.fxml");
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
