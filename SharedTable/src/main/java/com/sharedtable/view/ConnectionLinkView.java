package com.sharedtable.view;

import javafx.stage.Modality;

public class ConnectionLinkView extends GeneralView{

    public ConnectionLinkView() {
        super("ConnectionLinkWindow.fxml", Modality.APPLICATION_MODAL,null,"Kapcsolódási link generálása");
        getStage().showAndWait();
    }
}
