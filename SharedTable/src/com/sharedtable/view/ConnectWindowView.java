package com.sharedtable.view;

import javafx.stage.Modality;

public class ConnectWindowView extends GeneralView {
    public ConnectWindowView() {

        super("ConnectWindow.fxml", Modality.APPLICATION_MODAL,null,"Kapcsolódás hálózathoz");
        getStage().showAndWait();
    }
}
