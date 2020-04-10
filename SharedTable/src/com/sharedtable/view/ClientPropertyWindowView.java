package com.sharedtable.view;

import com.sharedtable.controller.ClientPropertyWindowController;
import com.sharedtable.model.Network.NetworkClientEntity;
import javafx.stage.Modality;

public class ClientPropertyWindowView extends GeneralView {

    public ClientPropertyWindowView(NetworkClientEntity entity, ClientsWindowView owner) {
        super("ClientPropertyWindow.fxml",Modality.NONE,owner.getStage(),"Kliens tulajdonságai");
        ((ClientPropertyWindowController)getController()).setData(entity);
        getStage().showAndWait();
    }



}
