package com.sharedtable.view;

import com.sharedtable.controller.ClientPropertyWindowController;
import com.sharedtable.model.NetworkClientEntity;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientPropertyWindowView {

    public ClientPropertyWindowView(NetworkClientEntity entity) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ClientPropertyWindow.fxml"));
        Parent parent;
        try{parent = fxmlLoader.load();}
        catch (IOException e) {
            System.out.println("failed to get resource ClientPropertyWindow.fxml");
            System.out.println(e);
            e.printStackTrace();
            return;
        }

        Scene scene = new Scene(parent, 400, 300);
        Stage stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.setScene(scene);
        stage.setTitle("Kliens tulajdonságok");
        stage.show();
        ((ClientPropertyWindowController)fxmlLoader.getController()).setData(entity);
    }



}
