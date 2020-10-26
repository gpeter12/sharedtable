package com.sharedtable.controller.commands;

import com.sharedtable.controller.CanvasController;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.UUID;

public class DrawRectangleCommand extends Command {

    Rectangle rectangle;
    Color color;
    int lineWidth;

    public DrawRectangleCommand(CanvasController canvasController, UUID creatorID,
                                Rectangle rectangle,
                                Color color,
                                int lineWidth)
    {
        super(canvasController, creatorID);
        this.color =color;
        this.lineWidth = lineWidth;
        this.rectangle = rectangle;
    }

    public DrawRectangleCommand(String[] input) {
        super(input);
        double rectX = Double.parseDouble(input[3]);
        double rectY = Double.parseDouble(input[4]);
        double rectW = Double.parseDouble(input[5]);
        double rectH = Double.parseDouble(input[6]);
        this.lineWidth = Integer.parseInt(input[7]);
        this.color = Color.valueOf(input[8]);
        this.rectangle = new Rectangle(rectX,rectY,rectW,rectH);
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(creatorID.toString()).append(";")
                .append(CommandTypeID.DrawRectangleCommand.ordinal()).append(";")
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
        canvasController.drawRectangle(rectangle,color,lineWidth);
    }


}
