package com.sharedtable.view;

import javafx.stage.Modality;

public class AboutView extends GeneralView{

    public AboutView() {
        super("AboutWindow.fxml", Modality.APPLICATION_MODAL,null,"A szoftverről");
        getStage().showAndWait();
    }
}
