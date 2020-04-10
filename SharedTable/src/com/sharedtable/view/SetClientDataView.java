package com.sharedtable.view;

import javafx.stage.Modality;

public class SetClientDataView extends GeneralView {

    public SetClientDataView() {
        super("SetClientDataWindow.fxml", Modality.APPLICATION_MODAL,null,"Falhasználói adatok megadása");
        getStage().setResizable(false);
        getStage().showAndWait();
    }
}
