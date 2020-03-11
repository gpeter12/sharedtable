package controller.commands;

import controller.Command;
import controller.Point;
import view.MainCanvas;

import java.util.UUID;

public class DrawLineCommand implements Command {

    public DrawLineCommand(MainCanvas canvas, Point x, Point y, UUID creatorID) {
        this.x = x;
        this.y = y;
        this.canvas = canvas;
        this.creatorID = creatorID;
    }

    public DrawLineCommand() {
    }

    public DrawLineCommand(MainCanvas canvas, String[] dataInput) {
        this.canvas = canvas;
        this.creatorID = UUID.fromString(dataInput[0]);
        double p1x = Double.parseDouble(dataInput[2]);
        double p1y = Double.parseDouble(dataInput[3]);
        double p2x = Double.parseDouble(dataInput[4]);
        double p2y = Double.parseDouble(dataInput[5]);

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
        stringBuilder.append(creatorID.toString()).append(";").append(CommandID.DrawLineCommand.ordinal()).append(";")
                .append(x.toString()).append(";").append(y.toString());
        System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }

    @Override
    public void execute() {
        canvas.drawLine(x, y);
    }

    private Point x;
    private Point y;
    private MainCanvas canvas;
    private UUID creatorID; //userID

}
