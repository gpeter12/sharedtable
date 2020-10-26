package com.sharedtable.view;

import javafx.stage.Modality;

public class SyncProcessView extends GeneralView{

    public SyncProcessView() {
        super("SyncProcessWindow.fxml", Modality.APPLICATION_MODAL,null,"Szinkronizálási folyamat");
        getStage().show();
    }

    public void closeWindow() {
        getStage().close();
    }
}
