package com.sharedtable.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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

    private String dropLastChar(String input) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<input.length()-1; i++) {
            sb.append(input.charAt(i));
        }
        return sb.toString();
    }

    private void sendMessage(){
        ChatService.handleOutgoingChatMessage(dropLastChar(chatInput.getText()));
        chatInput.setText("");
    }

    private void placeTextToSystemClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }



    @FXML
    public void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER) && keyEvent.isShiftDown()) {
            chatInput.setText(chatInput.getText()+"\n");
            chatInput.positionCaret(chatInput.getText().length());
        } else if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            sendMessage();
        }

    }
    @FXML
    public void btnSendClicked(ActionEvent actionEvent) {
        sendMessage();
    }
    @FXML
    public void btnCopyClicked(ActionEvent actionEvent) {
        placeTextToSystemClipboard(ChatService.getAllMessageTexts());
    }

    @FXML
    private TextFlow chatFlow;
    @FXML
    private TextArea chatInput;



}
