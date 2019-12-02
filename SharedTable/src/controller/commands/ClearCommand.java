package controller.commands;

import controller.CanvasController;
import controller.Command;
import view.MainCanvas;

import java.util.UUID;

public class ClearCommand implements Command {

    public ClearCommand(CanvasController canvasController, UUID creatorID) {
        this.creatorID = creatorID;
        this.canvasController = canvasController;
    }

    public ClearCommand(CanvasController canvasController, String[] input) {
        creatorID = UUID.fromString(input[0]);
        this.canvasController = canvasController;
    }

    @Override
    public void execute() {
        canvasController.getMainCanvas().clear();
    }

    @Override
    public UUID getCreatorID() {
        return creatorID;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(creatorID.toString()).append(";").append(CommandID.ClearCommand.ordinal());
        return sb.toString();
    }
    private CanvasController canvasController;
    private UUID creatorID;
}
