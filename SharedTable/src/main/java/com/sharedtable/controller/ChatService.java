package com.sharedtable.controller;

import com.sharedtable.model.network.NetworkService;
import com.sharedtable.model.network.signals.ChatMessageSignal;

import java.util.ArrayList;

public class ChatService {

    private ArrayList<ChatMessageSignal> chatMessageSignals = new ArrayList<>();
    private ChatWindowController chatWindowController_unsafe;
    private static ChatService instance = new ChatService();

    private ChatService() { }
    
    public static ChatService getInstance() {
        if(instance == null) {
            instance = new ChatService();
        }
        return instance;
    }

    public void handleIncomingChatMessageSignal(ChatMessageSignal signal) {
        if(getAllChatMessageSignal().contains(signal))
            return;
        chatMessageSignals.add(signal);

        if(hasChatWindowController()){
            getChatWindowController().printMessage(signal.getNickname(), signal.getMessage(),
                    signal.getCreatorID().equals(UserID.getInstance().getUserID()));
        }
    }

    public String getAllMessageTexts() {
        StringBuilder sb = new StringBuilder();
        for(ChatMessageSignal act : chatMessageSignals) {
            sb.append(act.getNickname())
                    .append(": ")
                    .append(act.getMessage()).append("\n");
        }
        return sb.toString();
    }

    public void handleOutgoingChatMessage(String message) {
        chatMessageSignals.add(NetworkService.getInstance().sendNewChatMessage(message));
        getChatWindowController().printMessage(UserID.getInstance().getNickname(),message,true);
    }

    public ArrayList<ChatMessageSignal> getAllChatMessageSignal() {
        return chatMessageSignals;
    }

    private ChatWindowController getChatWindowController() {
        if(chatWindowController_unsafe == null)
            throw new RuntimeException("chatWindowController not registered!");
        return chatWindowController_unsafe;
    }

    private boolean hasChatWindowController() {return chatWindowController_unsafe != null;}

    public void setChatWindowController(ChatWindowController currentChatWindowController) {
        chatWindowController_unsafe = currentChatWindowController;
    }

    public void printAllMessages(ChatWindowController chatWindowController) {
        for(ChatMessageSignal act : chatMessageSignals) {
            chatWindowController.printMessage(act.getNickname(), act.getMessage(),
                    act.getCreatorID().equals(UserID.getInstance().getUserID()));
        }
    }
    


}
