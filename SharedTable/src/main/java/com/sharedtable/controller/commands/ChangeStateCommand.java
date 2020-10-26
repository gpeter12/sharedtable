package com.sharedtable.controller.commands;

import com.sharedtable.controller.CanvasController;

import java.util.UUID;

public class ChangeStateCommand extends Command {

    private UUID targetMementoID;


    public ChangeStateCommand(CanvasController canvasController, UUID creatorID, UUID targetMementoID) {
        super(canvasController,creatorID);
        this.targetMementoID = targetMementoID;
    }

    public ChangeStateCommand(String[] input) {
        super(input);
        targetMementoID = UUID.fromString(input[3]);
    }

    @Override
    public void execute() {
        canvasController.processStateChangeCommand(targetMementoID);
    }

    public UUID getTargetMementoID() {return targetMementoID;}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(creatorID.toString()).append(";")
                .append(CommandTypeID.ChangeStateCommand.ordinal()).append(";")
                .append(canvasID).append(";")
                .append(targetMementoID.toString());
        return sb.toString();
    }


}
