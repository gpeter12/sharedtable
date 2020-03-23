package com.sharedtable.controller;

import com.sharedtable.controller.controllers.CanvasController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Ha nem választjuk szét userenként és lezárulási idő szerint a commandokat, akkor összemosnak egy state-be
 * itt különválasztva tároljuk, a készülő state mementókat creator userek szerint szétbontva
 * amíg le nem zárulnak. Az számít időben későbbi mementónak amelyik később zárul le.
 */


public class RemoteDrawLineCommandBufferHandler {

    public static void setCanvasController(CanvasController cc) {
        canvasController = cc;
    }

    public static void addCommand(Command command) {
        if (!commandBuffers.containsKey(command.getCreatorID())) {
            System.out.println("buffer not exists for: "+command.getCreatorID());
            throw new RuntimeException("user related command buffer does not exists!");
        }
        commandBuffers.get(command.getCreatorID()).add(command);
    }

    public static void closeMemento(UUID userID, UUID mementoID,boolean isLinked) {
        canvasController.insertRemoteMementoAfterActual(mementoID,commandBuffers.get(userID),isLinked,userID);
        //printAllCommands(commandBuffers.get(userID));
        commandBuffers.remove(userID);
        if(!isLinked)
            canvasController.processSateChangeCommand(mementoID); //a láncolási szakadásokat figyelembe véve újra rajzol
    }

    public static void openNewMemento(UUID userID) {
        System.out.println("opening new buffer to: "+userID.toString());
        if (commandBuffers.containsKey(userID)) {
            throw new RuntimeException("user related command buffer already exists!");
        }
        commandBuffers.put(userID, new ArrayList<Command>());
    }

    private static void printAllCommands(ArrayList<Command> input) {
        System.out.println("BufferPrint-------------------");
        for(Command act : input) {
            System.out.println(act.toString());
        }
        System.out.println("-------------------");
    }

    private static HashMap<UUID, ArrayList<Command>> commandBuffers = new HashMap<>();
    private static CanvasController canvasController;
}
