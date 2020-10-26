package com.sharedtable.controller.commands;

import com.sharedtable.controller.CanvasController;

import java.util.UUID;

public abstract class Command {

    CanvasController canvasController;
    UUID creatorID;
    UUID canvasID;

    public Command(CanvasController canvasController, UUID creatorID) {
        this.canvasController = canvasController;
        this.creatorID = creatorID;
        this.canvasID = canvasController.getCanvasID();
    }



    public abstract void execute();

    public void setCanvasController(CanvasController canvasController) {
        this.canvasController = canvasController;
        this.canvasID = canvasController.getCanvasID();
    }

    public Command(String[] input) {
        creatorID = UUID.fromString(input[0]);
        canvasID = UUID.fromString(input[2]);
    }

    public UUID getCreatorID(){
        return creatorID;
    }

    public UUID getCanvasID() {
        return canvasID;
    }

    public void setCreatorID(UUID creatorID) {
        this.creatorID = creatorID;
    }
}
