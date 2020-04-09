package com.sharedtable.view;

import com.sharedtable.controller.ChatService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatWindowView extends GeneralView{

    public ChatWindowView(Stage owner) {
        super("ChatWindow.fxml",Modality.NONE,owner,"Chat");

        getStage().setOnCloseRequest(event -> {
            ChatService.setChatWindowController(null);
            isOpened = false;
        });
        isOpened = true;
        getStage().show();
    }

    public boolean isOpened() {return isOpened;}

    private static boolean isOpened = false;


}
