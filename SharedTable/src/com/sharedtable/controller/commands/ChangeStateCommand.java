package com.sharedtable.controller.commands;

import com.sharedtable.controller.controllers.CanvasController;
import com.sharedtable.controller.Command;

import java.util.UUID;

public class ChangeStateCommand implements Command {

    public ChangeStateCommand(CanvasController canvasController, UUID creatorID, UUID targetMementoID) {
        this.creatorID = creatorID;
        this.targetMementoID = targetMementoID;
        this.canvasController = canvasController;
    }

    public ChangeStateCommand(CanvasController canvasController, String[] input) {
        creatorID = UUID.fromString(input[0]);
        targetMementoID = UUID.fromString(input[2]);
        this.canvasController = canvasController;
    }

    @Override
    public void execute() {
        canvasController.processSateChangeCommand(targetMementoID);
    }

    @Override
    public UUID getCreatorID() {
        return creatorID;
    }

    public UUID getTargetMementoID() {return targetMementoID;}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(creatorID.toString()).append(";").append(CommandID.ChangeStateCommand.ordinal()).append(";").append(targetMementoID.toString());
        return sb.toString();
    }

    private UUID creatorID;
    private UUID targetMementoID;
    private CanvasController canvasController;

}
