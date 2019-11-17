package controller.commands;

import controller.Command;
import controller.DrawingMode;
import controller.Point;
import view.MainCanvas;

import java.util.UUID;

public class DrawLineCommand implements Command {

    public DrawLineCommand(MainCanvas canvas, Point x, Point y) {
        this.x = x;
        this.y = y;
        this.canvas = canvas;
        this.ID = UUID.randomUUID();
    }

    public DrawLineCommand(){}

    public DrawLineCommand(MainCanvas canvas, String[] dataInput) {
        this.canvas = canvas;
        this.ID = UUID.fromString(dataInput[1]);
        double p1x = Double.parseDouble(dataInput[2]);
        double p1y = Double.parseDouble(dataInput[3]);
        double p2x = Double.parseDouble(dataInput[4]);
        double p2y = Double.parseDouble(dataInput[5]);

        x = new Point(p1x,p1y);
        y = new Point(p2x,p2y);
    }

    public void deepCopy(Command command) {
        if(command instanceof DrawLineCommand) {
            this.x = ((DrawLineCommand) command).x;
            this.y = ((DrawLineCommand) command).y;
            this.canvas = ((DrawLineCommand) command).canvas;
        }
        else
            throw new RuntimeException("Cannot cast command to DrawLineCommand");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(DrawingMode.ContinousLine.ordinal()).append(";").append(ID).append(";")
                .append(x.toString()).append(";").append(y.toString()).append("\n");
        System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }

    UUID ID;
    Point x;
    Point y;
    MainCanvas canvas;

    @Override
    public void execute() {
        canvas.drawLine(x,y);
    }



}
