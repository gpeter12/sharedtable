package controller.commands;

import controller.Command;
import controller.DrawingMode;
import controller.Point;
import view.MainCanvas;

import java.util.Objects;
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

    @Override
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
    public UUID getCreatorID() {
        return creatorID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DrawLineCommand that = (DrawLineCommand) o;
        return ID.equals(that.ID) &&
                x.equals(that.x) &&
                y.equals(that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, x, y);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(DrawingMode.ContinousLine.ordinal()).append(";").append(ID).append(";")
                .append(x.toString()).append(";").append(y.toString()).append("\n");
        System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }

    @Override
    public void execute() {
        canvas.drawLine(x,y);
    }

    private UUID ID;
    private Point x;
    private Point y;
    private MainCanvas canvas;
    private UUID creatorID;





}
