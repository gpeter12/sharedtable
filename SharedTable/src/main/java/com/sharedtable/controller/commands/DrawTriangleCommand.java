package com.sharedtable.controller.commands;

import com.sharedtable.controller.CanvasController;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.UUID;

public class DrawTriangleCommand extends DrawRectangleCommand {

    public DrawTriangleCommand(CanvasController canvasController, UUID creatorID,
                               Rectangle rectangle,
                               Color color,
                               int lineWidth)
    {
        super(canvasController, creatorID, rectangle, color, lineWidth);

    }

    public DrawTriangleCommand(String[] input) {
        super(input);

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(creatorID.toString()).append(";")
                .append(CommandTypeID.DrawTriangleCommand.ordinal()).append(";")
                .append(canvasID).append(";")
                .append(rectangle.getX()).append(";")
                .append(rectangle.getY()).append(";")
                .append(rectangle.getWidth()).append(";")
                .append(rectangle.getHeight()).append(";")
                .append(lineWidth).append(";")
                .append(color).append(";");
        return stringBuilder.toString();
    }

    @Override
    public void execute() {
        canvasController.drawTriangle(rectangle,color,lineWidth);
    }
}
