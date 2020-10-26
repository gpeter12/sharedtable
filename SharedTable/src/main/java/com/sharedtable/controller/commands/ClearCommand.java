package com.sharedtable.controller.commands;

import com.sharedtable.controller.CanvasController;

import java.util.ArrayList;
import java.util.UUID;

public class ClearCommand extends Command {

    private UUID blankMementoID;
    private boolean isRemote = false;
    private UUID prevMementoID;
    private UUID nextMementoID;

    public ClearCommand(CanvasController canvasController, UUID creatorID, UUID blankMementoID,UUID prevMementoID, UUID nextMementoID) {
        super(canvasController,creatorID);
        this.blankMementoID = blankMementoID;
        this.prevMementoID = prevMementoID;
        this.nextMementoID = nextMementoID;
    }

    public ClearCommand(String[] input) {
        super(input);
        this.blankMementoID = UUID.fromString(input[3]);
        this.prevMementoID = UUID.fromString(input[4]);
        this.nextMementoID = UUID.fromString(input[5]);
    }

    @Override
    public void execute() {
        if(isRemote) {
            canvasController.insertRemoteMementoAfter(
                    blankMementoID,new ArrayList<>(),false,creatorID, prevMementoID,nextMementoID);
        }
        canvasController.getSTCanvas().clear();
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
                .append(blankMementoID).append(";")
                .append(prevMementoID).append(";")
                .append(nextMementoID);
        return sb.toString();
    }

    public void setRemote(boolean remote) {
        isRemote = remote;
    }

    public UUID getBlankMementoID() {
        return blankMementoID;
    }
}
