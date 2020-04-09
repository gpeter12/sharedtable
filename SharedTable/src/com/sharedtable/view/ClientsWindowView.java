package com.sharedtable.view;

import com.sharedtable.controller.ClientsWindowController;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ClientsWindowView extends GeneralView {

    public ClientsWindowView(Stage owner) {
        super("ClientsWindow.fxml",Modality.NONE,owner,"Hálózati kliens fa");

        getStage().setOnCloseRequest(event -> {
            isOpened = false;
            ((ClientsWindowController)getController()).onClose();
        });
        ((ClientsWindowController)getController()).setView(this);
        isOpened = true;

        getStage().showAndWait();
    }

    public static boolean isOpened() {return isOpened;}

    private static boolean isOpened=false;
}
