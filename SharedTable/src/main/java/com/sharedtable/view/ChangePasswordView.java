package com.sharedtable.view;

import javafx.stage.Modality;

public class ChangePasswordView extends GeneralView {

    public ChangePasswordView() {
        super("ChangePasswordView.fxml", Modality.APPLICATION_MODAL,null,"Hálózati jelszó megváltoztatása");
        getStage().showAndWait();
    }
}