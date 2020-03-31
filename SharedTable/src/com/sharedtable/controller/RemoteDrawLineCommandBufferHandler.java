package com.sharedtable.controller;

import com.sharedtable.controller.commands.Command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Ha nem választjuk szét userenként és lezárulási idő szerint a commandokat, akkor összemosnak egy state-be
 * itt különválasztva tároljuk, a készülő state mementókat creator userek szerint szétbontva
 * amíg le nem zárulnak. Az számít időben későbbi mementónak amelyik később zárul le.
 */

public class RemoteDrawLineCommandBufferHandler {

    public RemoteDrawLineCommandBufferHandler(CanvasController canvasController) {
        this.canvasController = canvasController;
    }


    public void addCommand(Command command) {
        if (!commandBuffers.containsKey(command.getCreatorID())) {
            throw new RuntimeException("user related command buffer does not exists!\nbuffer not exists for: "+command.getCreatorID());
        }
        commandBuffers.get(command.getCreatorID()).add(command);
    }

    public void closeMemento(UUID userID, UUID mementoID,boolean isLinked) {
        canvasController.insertRemoteMementoAfterActual(mementoID,commandBuffers.get(userID),isLinked,userID);
        //printAllCommands(commandBuffers.get(userID));
        commandBuffers.remove(userID);
        if(!isLinked)
            canvasController.processSateChangeCommand(mementoID); //a láncolási szakadásokat figyelembe véve újra rajzol
    }

    public void openNewMemento(UUID userID) {
        if (commandBuffers.containsKey(userID)) {
            throw new RuntimeException("user related command buffer already exists!");
        }
        commandBuffers.put(userID, new ArrayList<Command>());
    }

    private void printAllCommands(ArrayList<Command> input) {
        System.out.println("BufferPrint-------------------");
        for(Command act : input) {
            System.out.println(act.toString());
        }
        System.out.println("-------------------");
    }

    private HashMap<UUID, ArrayList<Command>> commandBuffers = new HashMap<>();
    private CanvasController canvasController;
}
