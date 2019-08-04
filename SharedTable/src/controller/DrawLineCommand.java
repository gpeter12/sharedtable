package controller;

import view.MainCanvas;

public class DrawLineCommand implements Command {

    MainCanvas canvas;

    public DrawLineCommand(MainCanvas canvas,Point x, Point y) {
        this.x = x;
        this.y = y;
        this.canvas = canvas;
    }

    Point x;
    Point y;

    @Override
    public void execute() {
        canvas.drawLine(x,y);
    }
}
