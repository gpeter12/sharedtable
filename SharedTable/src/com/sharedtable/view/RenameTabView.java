package com.sharedtable.view;

import javafx.stage.Modality;

public class RenameTabView extends GeneralView{

    public RenameTabView() {
        super("RenameTabWindow.fxml",Modality.APPLICATION_MODAL,null,"Lap elnevezése");
        getStage().showAndWait();
    }
}
