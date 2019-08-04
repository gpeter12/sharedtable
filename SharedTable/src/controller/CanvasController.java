package controller;

import view.MainCanvas;
import java.util.ArrayList;

public class CanvasController {

    public CanvasController(MainCanvas canvas) {
        this.canvas = canvas;
    }

    public void mouseDown(Point p) {
        lastPoint = p;
        isMouseDown = true;
    }

    public void mouseUp(Point p) {
        isMouseDown = false;
        lastPoint = p;
    }

    public void mouseMove(Point p) {
        if(isMouseDown) {
            DrawLineCommand command = new DrawLineCommand(canvas,lastPoint,p);
            createdCommands.add(command);
            command.execute();
            lastPoint = p;
        }
    }


    boolean isMouseDown = false;
    Point lastPoint;
    MainCanvas canvas;
    ArrayList<Command> createdCommands = new ArrayList<>();

}
