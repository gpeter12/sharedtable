package view;

import controller.CanvasController;
import controller.Point;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;


public class MainCanvas extends Canvas {

    public MainCanvas() {
        graphicsContext = this.getGraphicsContext2D();
    }

    public void initEventHandlers(CanvasController canvasController) {
        this.addEventHandler(MouseEvent.MOUSE_PRESSED,
                event -> {
                    Point p = new Point(event.getX(), event.getY());
                    canvasController.mouseDown(p);
                });
        this.addEventHandler(MouseEvent.MOUSE_RELEASED,
                event -> {
                    Point p = new Point(event.getX(), event.getY());
                    canvasController.mouseUp(p);
                });
        this.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                event -> {
                    Point p = new Point(event.getX(), event.getY());
                    canvasController.mouseMove(p);
                });
    }


    public void drawLine(Point x, Point y) {
        graphicsContext.strokeLine(x.getX(), x.getY(), y.getX(), y.getY());
    }

    public void clear() {
        graphicsContext.clearRect(0.0, 0.0, graphicsContext.getCanvas().getWidth(), graphicsContext.getCanvas().getHeight());
    }

    private GraphicsContext graphicsContext;
}
