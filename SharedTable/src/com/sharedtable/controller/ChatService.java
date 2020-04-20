package com.sharedtable.controller;

import com.sharedtable.model.network.NetworkService;
import com.sharedtable.model.signals.ChatMessageSignal;

import java.util.ArrayList;

public class ChatService {

    private ChatService() { }

    public static void handleIncomingChatMessageSignal(ChatMessageSignal signal) {
        if(getAllChatMessageSignal().contains(signal))
            return;
        chatMessageSignals.add(signal);

        if(hasChatWindowController()){
            getChatWindowController().printMessage(signal.getNickname(), signal.getMessage(),
                    signal.getCreatorID().equals(UserID.getUserID()));
        }
    }

    public static String getAllMessageTexts() {
        StringBuilder sb = new StringBuilder();
        for(ChatMessageSignal act : chatMessageSignals) {
            sb.append(act.getNickname())
                    .append(": ")
                    .append(act.getMessage()).append("\n");
        }
        return sb.toString();
    }

    public static void handleOutgoingChatMessage(String message) {
        chatMessageSignals.add(NetworkService.sendNewChatMessage(message));
        getChatWindowController().printMessage(UserID.getNickname(),message,true);
    }

    public static ArrayList<ChatMessageSignal> getAllChatMessageSignal() {
        return chatMessageSignals;
    }

    private static ChatWindowController getChatWindowController() {
        if(chatWindowController_unsafe == null)
            throw new RuntimeException("chatWindowController not registered!");
        return chatWindowController_unsafe;
    }

    private static boolean hasChatWindowController() {return chatWindowController_unsafe != null;}

    public static void setChatWindowController(ChatWindowController currentChatWindowController) {
        chatWindowController_unsafe = currentChatWindowController;
    }
    public static void printAllMessages(ChatWindowController chatWindowController) {
        for(ChatMessageSignal act : chatMessageSignals) {
            chatWindowController.printMessage(act.getNickname(), act.getMessage(),
                    act.getCreatorID().equals(UserID.getUserID()));
        }
    }



    private static ArrayList<ChatMessageSignal> chatMessageSignals = new ArrayList<>();
    private static ChatWindowController chatWindowController_unsafe;


}
