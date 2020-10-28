package com.sharedtable.controller;

import com.sharedtable.model.network.NetworkService;
import com.sharedtable.model.network.signals.ChatMessageSignal;

import java.util.ArrayList;

public class ChatService {

    private ArrayList<ChatMessageSignal> chatMessageSignals = new ArrayList<>();
    private static ChatService instance = new ChatService();
    private ChatWindowControllerAccessor chatWindowControllerAccessor = new ChatWindowControllerAccessor();

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

        try {
            chatWindowControllerAccessor.getChatWindowController().printMessage(signal.getNickname(), signal.getMessage(),
                        signal.getCreatorID().equals(UserID.getInstance().getUserID()));
        } catch (ChatWindowControllerNotAvailableException e) { }

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
        try {
            chatWindowControllerAccessor.getChatWindowController().printMessage(UserID.getInstance().getNickname(),message,true);
        } catch (ChatWindowControllerNotAvailableException e) { }
    }

    public ArrayList<ChatMessageSignal> getAllChatMessageSignal() {
        return chatMessageSignals;
    }

    public void setChatWindowController(ChatWindowController currentChatWindowController) {
        chatWindowControllerAccessor.setChatWindowController(currentChatWindowController);
    }

    public void printAllMessages(ChatWindowController chatWindowController) {
        for(ChatMessageSignal act : chatMessageSignals) {
            chatWindowController.printMessage(act.getNickname(), act.getMessage(),
                    act.getCreatorID().equals(UserID.getInstance().getUserID()));
        }
    }

    private class ChatWindowControllerNotAvailableException extends Exception {}

    private class ChatWindowControllerAccessor {

        private ChatWindowController chatWindowController_unsafe;

        public ChatWindowControllerAccessor() { }

        public ChatWindowController getChatWindowController() throws ChatWindowControllerNotAvailableException {
            if(chatWindowController_unsafe == null) {
                throw new ChatWindowControllerNotAvailableException();
            } else {
                return chatWindowController_unsafe;
            }
        }

        public void setChatWindowController(ChatWindowController chatWindowController) {
            this.chatWindowController_unsafe = chatWindowController;
        }
    }

}
