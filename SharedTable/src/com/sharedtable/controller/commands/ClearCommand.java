package com.sharedtable.controller.commands;

import com.sharedtable.controller.controllers.CanvasController;
import com.sharedtable.controller.Command;

import java.util.ArrayList;
import java.util.UUID;

public class ClearCommand implements Command {

    public ClearCommand(CanvasController canvasController, UUID creatorID, UUID blankMementoID) {
        this.blankMementoID = blankMementoID;
        this.creatorID = creatorID;
        this.canvasController = canvasController;
    }

    public ClearCommand(CanvasController canvasController, String[] input) {
        this.creatorID = UUID.fromString(input[0]);
        this.canvasController = canvasController;
        this.blankMementoID = UUID.fromString(input[2]);
    }

    @Override
    public void execute() {
        canvasController.getMainCanvas().clear();
        if(isRemote) {
            canvasController.insertRemoteMementoAfterActual(
                    blankMementoID,new ArrayList<Command>(),false,creatorID);
        }
    }

    @Override
    public UUID getCreatorID() {
        return creatorID;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(creatorID.toString()).append(";").append(CommandID.ClearCommand.ordinal())
            .append(";").append(blankMementoID);
        return sb.toString();
    }

    public void setRemote(boolean remote) {
        isRemote = remote;
    }

    private CanvasController canvasController;
    private UUID creatorID;
    private UUID blankMementoID;
    private boolean isRemote = false;
}
