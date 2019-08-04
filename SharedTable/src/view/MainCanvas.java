package view;

import controller.CanvasController;
import controller.DrawLineCommand;
import controller.Point;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;


public class MainCanvas extends Canvas {



    public MainCanvas() {
        gCont = this.getGraphicsContext2D();
        cController = new CanvasController(this);
        this.addEventHandler(MouseEvent.MOUSE_PRESSED,
                event -> {
                    Point p = new Point(event.getX(),event.getY());
                    cController.mouseDown(p);
                });
        this.addEventHandler(MouseEvent.MOUSE_RELEASED,
                event -> {
                    Point p = new Point(event.getX(),event.getY());
                    cController.mouseUp(p);
                });
        this.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                event -> {
                    Point p = new Point(event.getX(),event.getY());
                    cController.mouseMove(p);
                });
    }

    public void drawLine(Point x, Point y) {
        System.out.println(x.toString()+"   -   "+y.toString());
        gCont.strokeLine(x.getX(),x.getY(),y.getX(),y.getY());
    }

    /*public void drawLine(DrawLineCommand command) {

    }*/

    private CanvasController cController;
    private GraphicsContext gCont;
}
