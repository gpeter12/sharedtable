package com.sharedtable.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.ResourceBundle;

public class ChatWindowController implements Initializable {


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ChatService.setChatWindowController(this);
        ChatService.printAllMessages(this);
    }

    public void printMessage(String nickname, String message, boolean isMine) {
        Platform.runLater(() -> {
            chatFlow.getChildren().add(getPrefixForUser(nickname,isMine));
            chatFlow.getChildren().add(new Text(message+"\n"));
        });
    }

    private Text getPrefixForUser(String nickname, boolean isMine) {
        Text resText = new Text(nickname+": ");
        resText.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        resText.setUnderline(isMine);
        return resText;
    }

    @FXML
    public void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER) && keyEvent.isShiftDown()) {
            chatInput.setText(chatInput.getText()+"\n");
            return;
        }
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            ChatService.handleOutgoingChatMessage(chatInput.getText().replace("\n",""));
            chatInput.setText("");
        }
    }

    @FXML
    private TextFlow chatFlow;
    @FXML
    private TextArea chatInput;


}
