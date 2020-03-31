package com.sharedtable.controller.commands;

import com.sharedtable.controller.CanvasController;

import java.util.ArrayList;
import java.util.UUID;

public class ClearCommand extends Command {

    public ClearCommand(CanvasController canvasController, UUID creatorID, UUID blankMementoID) {
        super(canvasController,creatorID);
        this.blankMementoID = blankMementoID;
    }

    public ClearCommand(String[] input) {
        super(input);
        this.blankMementoID = UUID.fromString(input[3]);
    }

    @Override
    public void execute() {
        canvasController.getSTCanvas().clear();
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
        sb.append(creatorID.toString()).append(";")
                .append(CommandTypeID.ClearCommand.ordinal()).append(";")
                .append(canvasID).append(";")
            .append(blankMementoID);
        return sb.toString();
    }

    public void setRemote(boolean remote) {
        isRemote = remote;
    }

    private UUID blankMementoID;
    private boolean isRemote = false;
}
