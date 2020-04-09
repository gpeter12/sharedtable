package com.sharedtable.view;

import com.sharedtable.controller.ClientPropertyWindowController;
import com.sharedtable.model.NetworkClientEntity;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientPropertyWindowView extends GeneralView {

    public ClientPropertyWindowView(NetworkClientEntity entity, ClientsWindowView owner) {
        super("ClientPropertyWindow.fxml",Modality.NONE,owner.getStage(),"Kliens tulajdonságai");
        ((ClientPropertyWindowController)getController()).setData(entity);
        getStage().showAndWait();
    }



}
