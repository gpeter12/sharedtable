package controller.commands;

import controller.Command;
import view.MainCanvas;

import java.util.UUID;

public class ClearCommand implements Command {

    public ClearCommand(MainCanvas mainCanvas, UUID creatorID) {
        this.creatorID = creatorID;
        this.mainCanvas = mainCanvas;
    }

    MainCanvas mainCanvas;

    @Override
    public void execute() {
        mainCanvas.clear();
    }

    @Override
    public UUID getCreatorID() {
        return creatorID;
    }

    private UUID creatorID;
}
