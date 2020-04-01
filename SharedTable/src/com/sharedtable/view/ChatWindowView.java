package com.sharedtable.view;

import com.sharedtable.controller.ChatService;
import com.sharedtable.controller.ChatWindowController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatWindowView {

    public ChatWindowView(Stage owner) {
        if(isOpened)
            return;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ChatWindow.fxml"));
        Parent parent;
        try{parent = fxmlLoader.load();}
        catch (IOException e) {
            System.out.println("failed to get resource ChatWindow.fxml");
            System.out.println(e);
            return;
        }

        Scene scene = new Scene(parent,400,600);
        Stage stage = new Stage();stage.initOwner(owner);
        stage.initModality(Modality.NONE);
        stage.setScene(scene);
        stage.setTitle("Chat");
        stage.setOnCloseRequest(event -> {
            ChatService.setChatWindowController(null);
            isOpened = false;
        });
        isOpened = true;
        stage.show();
    }

    private static boolean isOpened = false;


}
