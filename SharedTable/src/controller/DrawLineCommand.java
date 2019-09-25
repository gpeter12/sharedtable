package controller;

import view.MainCanvas;

public class DrawLineCommand implements Command {

    public DrawLineCommand(MainCanvas canvas,Point x, Point y) {
        this.x = x;
        this.y = y;
        this.canvas = canvas;
    }

    public DrawLineCommand(){}

    public void deepCopy(Command command) {
        if(command instanceof DrawLineCommand) {
            this.x = ((DrawLineCommand) command).x;
            this.y = ((DrawLineCommand) command).y;
            this.canvas = ((DrawLineCommand) command).canvas;
        }
        else
            throw new RuntimeException("Cannot cast command to DrawLineCommand");

    }

    Point x;
    Point y;
    MainCanvas canvas;

    @Override
    public void execute() {
        canvas.drawLine(x,y);
    }



}
