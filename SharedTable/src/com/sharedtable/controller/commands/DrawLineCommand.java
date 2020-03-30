package com.sharedtable.controller.commands;

import com.sharedtable.controller.Point;
import com.sharedtable.controller.controllers.CanvasController;

import java.util.UUID;

public class DrawLineCommand extends Command {

    public DrawLineCommand(CanvasController canvasController, Point x, Point y, UUID creatorID) {
        super(canvasController,creatorID);
        this.x = x;
        this.y = y;

    }

    public DrawLineCommand(String[] dataInput) {
        super(dataInput);
        double p1x = Double.parseDouble(dataInput[3]);
        double p1y = Double.parseDouble(dataInput[4]);
        double p2x = Double.parseDouble(dataInput[5]);
        double p2y = Double.parseDouble(dataInput[6]);

        x = new Point(p1x, p1y);
        y = new Point(p2x, p2y);
    }

    @Override
    public UUID getCreatorID() {
        return creatorID;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(creatorID.toString()).append(";").
                append(CommandTypeID.DrawLineCommand.ordinal()).append(";").
                append(canvasID).append(";")
                .append(x.toString()).append(";")
                .append(y.toString());
        return stringBuilder.toString();
    }

    @Override
    public void execute() {
        canvasController.drawLine(x, y);
    }

    private Point x;
    private Point y;


}
