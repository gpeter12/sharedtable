package com.sharedtable.view;

import javafx.stage.Modality;

public class UpdateNotificationView extends GeneralView {

    public UpdateNotificationView() {
        super("UpdateNotificationWindow.fxml", Modality.APPLICATION_MODAL,null,"Új Frissítés!");
        getStage().setResizable(true);
        getStage().show();
    }


}
