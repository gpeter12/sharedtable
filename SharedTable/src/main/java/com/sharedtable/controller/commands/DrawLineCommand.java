package com.sharedtable.controller.commands;

import com.sharedtable.controller.CanvasController;
import com.sharedtable.controller.Point;
import javafx.scene.paint.Color;

import java.util.UUID;

public class DrawLineCommand extends Command {

    private int lineWidth;
    private Color color;
    private Point x;
    private Point y;

    public DrawLineCommand(CanvasController canvasController, Point x, Point y, UUID creatorID, Color color, int lineWidth) {
        super(canvasController,creatorID);
        this.x = x;
        this.y = y;
        this.color = color;
        this.lineWidth = lineWidth;
    }

    public DrawLineCommand(String[] dataInput) {
        super(dataInput);
        double p1x = Double.parseDouble(dataInput[3]);
        double p1y = Double.parseDouble(dataInput[4]);
        double p2x = Double.parseDouble(dataInput[5]);
        double p2y = Double.parseDouble(dataInput[6]);

        x = new Point(p1x, p1y);
        y = new Point(p2x, p2y);

        this.lineWidth = Integer.parseInt(dataInput[7]);
        this.color = Color.valueOf(dataInput[8]);

    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(creatorID.toString()).append(";")
                .append(CommandTypeID.DrawLineCommand.ordinal()).append(";")
                .append(canvasID).append(";")
                .append(x.toString()).append(";")
                .append(y.toString()).append(";")
                .append(lineWidth).append(";")
                .append(color).append(";");
        return stringBuilder.toString();
    }



    @Override
    public void execute() {
        canvasController.drawLine(x, y, color,lineWidth);
    }




}
