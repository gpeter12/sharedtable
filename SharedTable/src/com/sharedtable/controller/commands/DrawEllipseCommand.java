package com.sharedtable.controller.commands;

import com.sharedtable.controller.CanvasController;
import javafx.scene.shape.Rectangle;

import java.util.UUID;

public class DrawEllipseCommand extends Command {

    public DrawEllipseCommand(CanvasController canvasController, UUID creatorID,
                              Rectangle rectangle)
    {
        super(canvasController,creatorID);

    }

    @Override
    public void execute() {

    }
}
