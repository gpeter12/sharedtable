package com.sharedtable.view;

import com.sharedtable.controller.ChatService;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ChatWindowView extends GeneralView{

    private static boolean isOpened = false;


    public ChatWindowView(Stage owner) {
        super("ChatWindow.fxml",Modality.NONE,owner,"Chat");

        getStage().setOnCloseRequest(event -> {
            ChatService.getInstance().setChatWindowController(null);
            isOpened = false;
        });
        isOpened = true;
        getStage().setResizable(true);
        getStage().setMaxWidth(600);
        getStage().show();
    }

    public boolean isOpened() {return isOpened;}



}
